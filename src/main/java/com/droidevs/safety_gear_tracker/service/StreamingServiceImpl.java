package com.droidevs.safety_gear_tracker.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import com.droidevs.safety_gear_tracker.model.Camera;
import com.droidevs.safety_gear_tracker.repository.CameraRepository;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class StreamingServiceImpl implements StreamingService {

    private final CameraRepository cameraRepository;

    private final ConcurrentHashMap<Long, Process> streamingProcesses = new ConcurrentHashMap<>();

    @Override
    public Mono<Resource> getManifest(Long cameraId) {
        return startStreaming(cameraId).then(getResource(getManifestPath(cameraId)));
    }

    @Override
    public Mono<Resource> getSegment(Long cameraId, String segment) {
        return getResource(getSegmentPath(cameraId, segment));
    }

    private Mono<Void> startStreaming(Long cameraId) {
        return Mono.fromRunnable(() -> {
            if (streamingProcesses.containsKey(cameraId)) {
                return;
            }

            Camera camera = cameraRepository.findById(cameraId)
                    .orElseThrow(() -> new RuntimeException("Camera not found"));

            String rtspUrl = camera.getRtspUrl();
            Path outputDir = getOutputDir(cameraId);

            try {
                Files.createDirectories(outputDir);

                ProcessBuilder processBuilder = new ProcessBuilder(
                        "ffmpeg",
                        "-i", rtspUrl,
                        "-c:v", "libx24",
                        "-c:a", "aac",
                        "-f", "hls",
                        "-hls_time", "10",
                        "-hls_list_size", "6",
                        "-hls_flags", "delete_segments",
                        outputDir.resolve("index.m3u8").toString()
                );

                Process process = processBuilder.start();
                streamingProcesses.put(cameraId, process);

                Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                    process.destroy();
                    try {
                        Files.walk(outputDir)
                                .map(Path::toFile)
                                .forEach(File::delete);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }));

            } catch (IOException e) {
                throw new RuntimeException("Failed to start streaming", e);
            }
        });
    }

    private Mono<Resource> getResource(Path path) {
        try {
            Resource resource = new UrlResource(path.toUri());
            if (resource.exists() || resource.isReadable()) {
                return Mono.just(resource);
            } else {
                return Mono.error(new RuntimeException("Could not read the file!"));
            }
        } catch (Exception e) {
            return Mono.error(new RuntimeException("Error: " + e.getMessage()));
        }
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
