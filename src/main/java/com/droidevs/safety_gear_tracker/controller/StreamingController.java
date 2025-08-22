package com.droidevs.safety_gear_tracker.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.droidevs.safety_gear_tracker.service.StreamingService;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/stream")
public class StreamingController {

    @Autowired
    private StreamingService streamingService;

    @GetMapping("/{cameraId}/index.m3u8")
    public Mono<Resource> getManifest(@PathVariable Long cameraId) {
        return streamingService.getManifest(cameraId);
    }

    @GetMapping("/{cameraId}/{segment}")
    public Mono<Resource> getSegment(@PathVariable Long cameraId, @PathVariable String segment) {
        return streamingService.getSegment(cameraId, segment);
    }
}
