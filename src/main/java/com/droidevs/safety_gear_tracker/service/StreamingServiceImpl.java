package com.droidevs.safety_gear_tracker.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import com.droidevs.safety_gear_tracker.model.Camera;
import com.droidevs.safety_gear_tracker.repository.CameraRepository;
import reactor.core.publisher.Mono;
import com.droidevs.safety_gear_tracker.handler.exception.ResourceNotFoundException;
import com.droidevs.safety_gear_tracker.handler.exception.StreamingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@RequiredArgsConstructor
public class StreamingServiceImpl implements StreamingService {

    private static final Logger logger = LoggerFactory.getLogger(StreamingServiceImpl.class);

    private final CameraRepository cameraRepository;

    private final ConcurrentHashMap<Long, Process> streamingProcesses = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Long, Long> lastAccessTimes = new ConcurrentHashMap<>();

    @Override
    public Mono<Resource> getManifest(Long cameraId) {
        lastAccessTimes.put(cameraId, System.currentTimeMillis());
        return startStreaming(cameraId).then(getResource(getManifestPath(cameraId)));
    }

    @Override
    public Mono<Resource> getSegment(Long cameraId, String segment) {
        lastAccessTimes.put(cameraId, System.currentTimeMillis());
        return getResource(getSegmentPath(cameraId, segment));
    }

    private Mono<Void> startStreaming(Long cameraId) {
        return Mono.fromRunnable(() -> {
            if (streamingProcesses.containsKey(cameraId)) {
                return;
            }

            Camera camera = cameraRepository.findById(cameraId)
                    .orElseThrow(() -> new ResourceNotFoundException("Camera not found with ID: " + cameraId));

            String rtspUrl = camera.getStreamUrl();
            Path outputDir = getOutputDir(cameraId);

            try {
                Files.createDirectories(outputDir);

                ProcessBuilder processBuilder = new ProcessBuilder(
                        "ffmpeg",
                        "-i", rtspUrl,
                        "-c:v", "libx264",
                        "-c:a", "aac",
                        "-f", "hls",
                        "-hls_time", "10",
                        "-hls_list_size", "6",
                        "-hls_flags", "delete_segments",
                        outputDir.resolve("index.m3u8").toString()
                );

                processBuilder.redirectOutput(ProcessBuilder.Redirect.DISCARD);
                processBuilder.redirectError(ProcessBuilder.Redirect.DISCARD);

                Process process = processBuilder.start();
                streamingProcesses.put(cameraId, process);
                lastAccessTimes.put(cameraId, System.currentTimeMillis());

                logger.info("Started streaming for camera ID: {} on path {}", cameraId, outputDir);

            } catch (IOException e) {
                throw new StreamingException("Failed to start streaming for camera " + cameraId, e);
            }
        });
    }

    private Mono<Resource> getResource(Path path) {
        try {
            Resource resource = new UrlResource(path.toUri());
            if (resource.exists() || resource.isReadable()) {
                return Mono.just(resource);
            } else {
                throw new StreamingException("Could not read the file or file does not exist: " + path);
            }
        } catch (IOException e) {
            throw new StreamingException("Error accessing stream resource at path: " + path, e);
        }
    }

    private void stopStreaming(Long cameraId) {
        Process process = streamingProcesses.remove(cameraId);
        if (process != null) {
            logger.info("Stopping stream for camera ID: {}", cameraId);
            process.destroy();
            try {
                if (!process.waitFor(5, TimeUnit.SECONDS)) {
                    process.destroyForcibly();
                }
            } catch (InterruptedException e) {
                process.destroyForcibly();
                Thread.currentThread().interrupt();
            }
        }
        lastAccessTimes.remove(cameraId);
        Path outputDir = getOutputDir(cameraId);
        if (Files.exists(outputDir)) {
            try (var stream = Files.walk(outputDir)) {
                stream.sorted(java.util.Comparator.reverseOrder())
                        .map(Path::toFile)
                        .forEach(File::delete);
            } catch (IOException e) {
                logger.error("Error cleaning up streaming directory for camera {}: {}", cameraId, e.getMessage());
            }
        }
    }

    @Scheduled(fixedDelay = 10000)
    public void cleanupInactiveStreams() {
        long now = System.currentTimeMillis();
        lastAccessTimes.forEach((cameraId, lastAccessTime) -> {
            if (now - lastAccessTime > 60000) {
                logger.info("Camera ID: {} has been inactive for > 60s. Terminating stream.", cameraId);
                stopStreaming(cameraId);
            }
        });
    }

    @PreDestroy
    public void shutdownAllStreams() {
        logger.info("Shutting down all active streams due to context destruction.");
        streamingProcesses.keySet().forEach(this::stopStreaming);
    }

    private Path getOutputDir(Long cameraId) {
        return Paths.get("temp", "hls", String.valueOf(cameraId));
    }

    private Path getManifestPath(Long cameraId) {
        return getOutputDir(cameraId).resolve("index.m3u8");
    }

    private Path getSegmentPath(Long cameraId, String segment) {
        return getOutputDir(cameraId).resolve(segment);
    }
}
