package com.droidevs.safety_gear_tracker.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.droidevs.safety_gear_tracker.service.StreamingService;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/stream")
@RequiredArgsConstructor
public class StreamingController {

    private final StreamingService streamingService;

    @GetMapping("/{cameraId}/index.m3u8")
    public Mono<Resource> getManifest(@PathVariable Long cameraId) {
        return streamingService.getManifest(cameraId);
    }


    @GetMapping("/{cameraId}/{segment}")
    public Mono<Resource> getSegment(@PathVariable Long cameraId, @PathVariable String segment) {
        return streamingService.getSegment(cameraId, segment);
    }
}
