package com.droidevs.safety_gear_tracker.service;

import com.droidevs.safety_gear_tracker.handler.exception.CameraManagementException;
import lombok.RequiredArgsConstructor;
import org.apache.hc.client5.http.auth.AuthScope;
import org.apache.hc.client5.http.auth.UsernamePasswordCredentials;
import org.apache.hc.client5.http.classic.methods.HttpPut;
import org.apache.hc.client5.http.impl.auth.BasicCredentialsProvider;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class HikvisionCameraManagementServiceImpl implements CameraManagementService {

    private static final Logger logger = LoggerFactory.getLogger(HikvisionCameraManagementServiceImpl.class);

    @Override
    public void changeCredentials(String ipAddress, int port, String oldUsername, String oldPassword, String newUsername, String newPassword) {
        if (!StringUtils.hasText(ipAddress) || port <= 0 ||
            !StringUtils.hasText(oldUsername) || !StringUtils.hasText(oldPassword) ||
            !StringUtils.hasText(newUsername) || !StringUtils.hasText(newPassword)) {
            logger.error("Invalid arguments provided for changing camera credentials.");
            throw new IllegalArgumentException("Invalid arguments provided for changing camera credentials.");
        }

        String url = String.format("http://%s:%d/ISAPI/Security/users/1", ipAddress, port);

        BasicCredentialsProvider credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(
                new AuthScope(ipAddress, port),
                new UsernamePasswordCredentials(oldUsername, oldPassword.toCharArray())
        );

        try (CloseableHttpClient httpClient = HttpClients.custom()
                .setDefaultCredentialsProvider(credsProvider)
                .build()) {

            HttpPut httpPut = new HttpPut(url);
            String xmlPayload = String.format("<User><userName>%s</userName><password>%s</password></User>", newUsername, newPassword);
            httpPut.setEntity(new StringEntity(xmlPayload));
            httpPut.setHeader("Content-Type", "application/xml");

            httpClient.execute(httpPut, response -> {
                int statusCode = response.getCode();
                if (statusCode >= 200 && statusCode < 300) {
                    logger.info("Successfully changed credentials for camera at {}", ipAddress);
                    return true; // Return value for the HttpClient.execute lambda
                } else {
                    String errorMessage = String.format("Failed to change credentials for camera at %s. Status code: %d", ipAddress, statusCode);
                    logger.error(errorMessage);
                    throw new CameraManagementException(errorMessage);
                }
            });

        } catch (IOException e) {
            String errorMessage = String.format("Error changing credentials for camera at %s: %s", ipAddress, e.getMessage());
            logger.error(errorMessage, e);
            throw new CameraManagementException(errorMessage, e);
        }
    }
}
