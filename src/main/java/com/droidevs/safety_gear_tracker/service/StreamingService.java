package com.droidevs.safety_gear_tracker.service;

import org.springframework.core.io.Resource;
import reactor.core.publisher.Mono;

public interface StreamingService {
    Mono<Resource> getManifest(Long cameraId);
    Mono<Resource> getSegment(Long cameraId, String segment);
}
