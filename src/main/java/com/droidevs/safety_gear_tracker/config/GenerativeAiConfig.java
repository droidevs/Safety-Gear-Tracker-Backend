package com.droidevs.safety_gear_tracker.config;

import com.google.cloud.vertexai.VertexAI;
import com.google.cloud.vertexai.generativeai.GenerativeModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
public class GenerativeAiConfig {

    @Bean
    public GenerativeModel generativeModel(
            @Value("${gemini.project.id}") String projectId,
            @Value("${gemini.location}") String location,
            @Value("${gemini.model.name}") String modelName) throws IOException {
        
        try (VertexAI vertexAI = new VertexAI(projectId, location)) {
            return new GenerativeModel(modelName, vertexAI);
        }
    }
}
