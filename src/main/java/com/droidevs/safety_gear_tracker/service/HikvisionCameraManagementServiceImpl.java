package com.droidevs.safety_gear_tracker.service;

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
    public boolean changeCredentials(String ipAddress, int port, String oldUsername, String oldPassword, String newUsername, String newPassword) {
        if (!StringUtils.hasText(ipAddress) || port <= 0 ||
            !StringUtils.hasText(oldUsername) || !StringUtils.hasText(oldPassword) ||
            !StringUtils.hasText(newUsername) || !StringUtils.hasText(newPassword)) {
            logger.error("Invalid arguments provided for changing camera credentials.");
            return false;
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

            return httpClient.execute(httpPut, response -> {
                int statusCode = response.getCode();
                if (statusCode >= 200 && statusCode < 300) {
                    logger.info("Successfully changed credentials for camera at {}", ipAddress);
                    return true;
                } else {
                    logger.error("Failed to change credentials for camera at {}. Status code: {}", ipAddress, statusCode);
                    return false;
                }
            });

        } catch (IOException e) {
            logger.error("Error changing credentials for camera at {}", ipAddress, e);
            return false;
        }
    }
}
