package com.droidevs.safety_gear_tracker.service;

import com.droidevs.safety_gear_tracker.dto.RecordingResponseDto;
import com.droidevs.safety_gear_tracker.handler.exception.ResourceNotFoundException;
import com.droidevs.safety_gear_tracker.handler.exception.S3OperationException;
import com.droidevs.safety_gear_tracker.handler.exception.VideoRecordingException;
import com.droidevs.safety_gear_tracker.mappers.RecordingMapper;
import com.droidevs.safety_gear_tracker.model.Camera;
import com.droidevs.safety_gear_tracker.model.Recording;
import com.droidevs.safety_gear_tracker.model.User;
import com.droidevs.safety_gear_tracker.repository.CameraRepository;
import com.droidevs.safety_gear_tracker.repository.RecordingRepository;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

@Service
public class RecordingServiceImpl implements RecordingService {

    private final CameraRepository cameraRepository;
    private final S3Service s3Service;
    private final RecordingRepository recordingRepository;
    private final RecordingMapper recordingMapper;
    private final Executor recordingExecutor;

    @Value("${recording.duration.minutes:10}")
    private int recordingDurationMinutes;

    private final Map<Long, Future<?>> activeRecordings = new ConcurrentHashMap<>();
    private final Map<Long, Process> activeFFmpegProcesses = new ConcurrentHashMap<>();

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("HH-mm-ss");

    public RecordingServiceImpl(
            CameraRepository cameraRepository,
            S3Service s3Service,
            RecordingRepository recordingRepository,
            RecordingMapper recordingMapper,
            @Qualifier("recordingExecutor") Executor recordingExecutor) {
        this.cameraRepository = cameraRepository;
        this.s3Service = s3Service;
        this.recordingRepository = recordingRepository;
        this.recordingMapper = recordingMapper;
        this.recordingExecutor = recordingExecutor;
    }

    @PostConstruct
    public void init() {
        cameraRepository.findByActiveTrueAndRecordingActiveTrue().forEach(this::startRecording);
    }

    @Scheduled(cron = "0 */10 * * * *")
    public void recordAllActiveCameras() {
        System.out.println("Scheduled recording check started.");
        cameraRepository.findByActiveTrueAndRecordingActiveTrue().forEach(camera -> {
            if (!activeRecordings.containsKey(camera.getId())) {
                System.out.println("Starting new recording for camera: " + camera.getId());
                startRecording(camera);
            }
        });
        activeRecordings.keySet().forEach(cameraId -> {
            Camera camera = cameraRepository.findById(cameraId).orElse(null);
            if (camera == null || !camera.isActive() || !camera.isRecordingActive()) {
                System.out.println("Stopping recording for camera (deactivated/deleted): " + cameraId);
                stopRecording(cameraId);
            }
        });
    }

    @Override
    public void startRecording(Camera camera) {
        if (!camera.isActive() || !camera.isRecordingActive() || activeRecordings.containsKey(camera.getId())) {
            return;
        }
        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
            try {
                runRecordingLoop(camera);
            } catch (Exception e) {
                System.err.println("Unexpected error in recording loop for camera " + camera.getId() + ": " + e.getMessage());
            }
        }, recordingExecutor);
        activeRecordings.put(camera.getId(), future);
    }

    private void runRecordingLoop(Camera camera) {
        try {
            while (cameraRepository.findById(camera.getId()).map(c -> c.isActive() && c.isRecordingActive()).orElse(false)) {
                LocalDateTime now = LocalDateTime.now();
                String dateFolder = now.format(DATE_FORMATTER);
                String timestampFile = now.format(TIMESTAMP_FORMATTER) + ".mp4";
                String s3Key = String.format("camera_recordings/%s/%s/%s", camera.getId(), dateFolder, timestampFile);

                Recording recording = new Recording();
                recording.setCamera(camera);
                recording.setFilePath(s3Key);
                recording.setStartTime(now);
                recordingRepository.save(recording);
                System.out.println("Created recording record in database: " + s3Key);

                Path tempFile = Files.createTempFile("camera_recording_", ".mp4");
                Process process = null;
                int exitCode = -1;

                try {
                    System.out.println("Recording camera " + camera.getId() + " to " + tempFile.toString());
                    process = startFFmpegRecording(camera.getStreamUrl(), tempFile.toString(), recordingDurationMinutes);
                    activeFFmpegProcesses.put(camera.getId(), process);

                    exitCode = process.waitFor();
                } catch (VideoRecordingException e) {
                    System.err.println("FFmpeg recording failed for camera " + camera.getId() + ": " + e.getMessage());
                    throw e; 
                } finally {
                    activeFFmpegProcesses.remove(camera.getId());
                    System.out.println("FFmpeg for camera " + camera.getId() + " exited with code: " + exitCode);
                }

                if (exitCode == 0) {
                    Recording endRecording = recordingRepository.findByFilePath(s3Key);
                    if (endRecording != null) {
                        endRecording.setEndTime(LocalDateTime.now());
                        recordingRepository.save(endRecording);
                    }
                    System.out.println("Uploading " + tempFile.toString() + " to S3 at " + s3Key);
                    try {
                        s3Service.uploadFile(s3Key, tempFile.toFile());
                    } catch (S3OperationException e) {
                        throw new S3OperationException("Failed to upload recorded file to S3 for camera " + camera.getId(), e);
                    }
                    System.out.println("Recording upload complete for: " + s3Key);
                } else {
                    System.err.println("FFmpeg recording failed for camera " + camera.getId());
                    recordingRepository.delete(recording);
                    System.err.println("Deleted recording record from database due to failure: " + s3Key);
                }
                Files.deleteIfExists(tempFile);

                Thread.sleep(5000);
            }
        } catch (IOException e) {
            throw new VideoRecordingException("File system error during recording for camera " + camera.getId(), e);
        } catch (S3OperationException e) {
            throw new S3OperationException("S3 operation failed during recording for camera " + camera.getId(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); 
            throw new VideoRecordingException("Recording for camera " + camera.getId() + " was interrupted.", e);
        } catch (Exception e) { 
            throw new VideoRecordingException("An unexpected error occurred during recording for camera " + camera.getId(), e);
        } finally {
            activeRecordings.remove(camera.getId());
        }
    }

    private Process startFFmpegRecording(String rtspUrl, String outputPath, int durationMinutes) throws VideoRecordingException {
        String duration = String.valueOf(durationMinutes * 60);
        try {
            ProcessBuilder builder = new ProcessBuilder(
                    "ffmpeg",
                    "-i", rtspUrl,
                    "-t", duration,
                    "-c:v", "libx264",
                    "-preset", "veryfast",
                    "-crf", "23",
                    "-c:a", "aac",
                    "-b:a", "128k",
                    "-y",
                    outputPath
            );
            builder.redirectOutput(ProcessBuilder.Redirect.DISCARD);
            builder.redirectError(ProcessBuilder.Redirect.DISCARD);
            Process process = builder.start();
            return process;
        } catch (IOException e) {
            throw new VideoRecordingException("Failed to start FFmpeg process for RTSP stream: " + rtspUrl, e);
        }
    }

    @Override
    public void stopRecording(Long cameraId) {
        Future<?> future = activeRecordings.remove(cameraId);
        if (future != null) {
            future.cancel(true);
        }
        Process ffmpegProcess = activeFFmpegProcesses.remove(cameraId);
        if (ffmpegProcess != null) {
            ffmpegProcess.destroyForcibly();
        }
    }

    @Override
    public Page<RecordingResponseDto> getAllRecordings(int page, int size) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<Long> cameraIds = user.getZones().stream()
                .flatMap(zone -> zone.getCameras().stream())
                .map(Camera::getId)
                .collect(Collectors.toList());
        Page<Recording> recordingPage = recordingRepository.findByCameraIdIn(cameraIds, PageRequest.of(page, size));
        return recordingPage.map(recordingMapper::toDto);
    }

    @Override
    public Optional<RecordingResponseDto> getRecordingById(Long id) {
        return recordingRepository.findById(id).map(recordingMapper::toDto);
    }

    @Override
    public byte[] getRecordingFile(Long id) {
        Recording recording = recordingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Recording not found with ID: " + id));
        try {
            return s3Service.downloadFile(recording.getFilePath());
        } catch (S3OperationException e) {
            throw new S3OperationException("Failed to download recording file from S3 for ID: " + id, e);
        }
    }

    @Override
    public InputStreamResource getRecordingFileStream(Long id) {
        Recording recording = recordingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Recording not found with ID: " + id));
        try {
            ResponseInputStream<GetObjectResponse> s3Object = s3Service.downloadFileAsStream(recording.getFilePath());
            return new InputStreamResource(s3Object);
        } catch (S3OperationException e) {
            throw new S3OperationException("Failed to stream recording file from S3 for ID: " + id, e);
        }
    }

    @Override
    public void deleteRecording(Long id) {
        Recording recording = recordingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Recording not found with ID: " + id));
        try {
            s3Service.deleteFile(recording.getFilePath());
            recordingRepository.delete(recording);
        } catch (S3OperationException e) {
            throw new S3OperationException("Failed to delete recording file from S3 for ID: " + id, e);
        }
    }

    @PreDestroy
    public void shutdown() {
        System.out.println("Shutting down recording service. Stopping all active recordings...");
        activeRecordings.keySet().forEach(this::stopRecording);
        // The TaskExecutor is managed by Spring, no need to shut it down here manually.
    }
}
