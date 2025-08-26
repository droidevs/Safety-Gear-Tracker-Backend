package com.droidevs.safety_gear_tracker.service;

import com.droidevs.safety_gear_tracker.model.Camera;
import com.droidevs.safety_gear_tracker.model.Recording;
import com.droidevs.safety_gear_tracker.model.User;
import com.droidevs.safety_gear_tracker.repository.CameraRepository;
import com.droidevs.safety_gear_tracker.repository.RecordingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecordingServiceImpl implements RecordingService {

    private final CameraRepository cameraRepository;
    private final S3Service s3Service;
    private final RecordingRepository recordingRepository;

    @Value("${recording.duration.minutes:10}")
    private int recordingDurationMinutes;

    private final Map<Long, Future<?>> activeRecordings = new ConcurrentHashMap<>();
    private final Map<Long, Process> activeFFmpegProcesses = new ConcurrentHashMap<>();
    private final ExecutorService recordingExecutor = Executors.newCachedThreadPool();

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("HH-mm-ss");

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

        Future<?> future = recordingExecutor.submit(() -> {
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
                    } finally {
                        activeFFmpegProcesses.remove(camera.getId());
                        System.out.println("FFmpeg for camera " + camera.getId() + " exited with code: " + exitCode);
                    }

                    if (exitCode == 0) {
                        Recording endRecording = recordingRepository.findByFilePath(s3Key);
                        endRecording.setEndTime(LocalDateTime.now());
                        recordingRepository.save(recording);
                        System.out.println("Uploading " + tempFile.toString() + " to S3 at " + s3Key);
                        s3Service.uploadFile(s3Key, tempFile.toFile());
                        System.out.println("Recording upload complete for: " + s3Key);
                    } else {
                        System.err.println("FFmpeg recording failed for camera " + camera.getId());
                        recordingRepository.delete(recording);
                        System.err.println("Deleted recording record from database due to failure: " + s3Key);
                    }
                    Files.deleteIfExists(tempFile);

                    Thread.sleep(5000);
                }
            } catch (Exception e) {
                System.err.println("Error during recording for camera " + camera.getId() + ": " + e.getMessage());
                e.printStackTrace();
            } finally {
                activeRecordings.remove(camera.getId());
            }
        });
        activeRecordings.put(camera.getId(), future);
    }

    private Process startFFmpegRecording(String rtspUrl, String outputPath, int durationMinutes) throws IOException {
        String duration = String.valueOf(durationMinutes * 60);
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
        builder.redirectErrorStream(true);
        Process process = builder.start();

        new Thread(() -> {
            try (InputStream is = process.getInputStream()) {
                byte[] buffer = new byte[1024];
                while (is.read(buffer) != -1) {
                    // Consume the stream but don't print to avoid excessive logging
                }
            } catch (IOException e) {
                System.err.println("Error reading FFmpeg output: " + e.getMessage());
            }
        }).start();

        return process;
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
    public Page<Recording> getAllRecordings(int page, int size) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<Long> cameraIds = user.getZones().stream()
                .flatMap(zone -> zone.getCameras().stream())
                .map(Camera::getId)
                .collect(Collectors.toList());
        return recordingRepository.findByCameraIdIn(cameraIds, PageRequest.of(page, size));
    }

    @Override
    public Optional<Recording> getRecordingById(Long id) {
        return recordingRepository.findById(id);
    }

    @Override
    public byte[] getRecordingFile(Long id) {
        Optional<Recording> recordingOptional = recordingRepository.findById(id);
        if (recordingOptional.isPresent()) {
            Recording recording = recordingOptional.get();
            try {
                return s3Service.downloadFile(recording.getFilePath());
            } catch (IOException e) {
                System.err.println("Error downloading recording file from S3: " + e.getMessage());
                return null;
            }
        }
        return null;
    }

    @Override
    public InputStreamResource getRecordingFileStream(Long id) {
        Optional<Recording> recordingOptional = recordingRepository.findById(id);
        if (recordingOptional.isPresent()) {
            Recording recording = recordingOptional.get();
            try {
                ResponseInputStream<GetObjectResponse> s3Object = s3Service.downloadFileAsStream(recording.getFilePath());
                return new InputStreamResource(s3Object);
            } catch (Exception e) {
                System.err.println("Error downloading recording file from S3: " + e.getMessage());
                return null;
            }
        }
        return null;
    }

    @Override
    public boolean deleteRecording(Long id) {
        Optional<Recording> recordingOptional = recordingRepository.findById(id);
        if (recordingOptional.isPresent()) {
            Recording recording = recordingOptional.get();
            try {
                s3Service.deleteFile(recording.getFilePath());
                recordingRepository.delete(recording);
                return true;
            } catch (IOException e) {
                System.err.println("Error deleting recording file from S3: " + e.getMessage());
                return false;
            }
        }
        return false;
    }

    @PreDestroy
    public void shutdown() {
        System.out.println("Shutting down recording service. Stopping all active recordings...");
        activeRecordings.keySet().forEach(this::stopRecording);
        recordingExecutor.shutdownNow();
    }
}
