package com.droidevs.safety_gear_tracker.service;

import org.apache.hc.client5.http.auth.AuthScope;
import org.apache.hc.client5.http.auth.UsernamePasswordCredentials;
import org.apache.hc.client5.http.classic.methods.HttpPut;
import org.apache.hc.client5.http.impl.auth.BasicCredentialsProvider;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class HikvisionCameraManagementService implements CameraManagementService {

    @Override
    public boolean changeCredentials(String ipAddress, int port, String oldUsername, String oldPassword, String newUsername, String newPassword) {
        String url = "http://" + ipAddress + ":" + port + "/ISAPI/Security/users/1"; // Example ISAPI endpoint

        BasicCredentialsProvider credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(
                new AuthScope(ipAddress, port),
                new UsernamePasswordCredentials(oldUsername, oldPassword.toCharArray())
        );

        try (CloseableHttpClient httpClient = HttpClients.custom()
                .setDefaultCredentialsProvider(credsProvider)
                .build()) {

            HttpPut httpPut = new HttpPut(url);
            String xmlPayload = "<User><userName>" + newUsername + "</userName><password>" + newPassword + "</password></User>"; // Example payload
            httpPut.setEntity(new StringEntity(xmlPayload));
            httpPut.setHeader("Content-Type", "application/xml");

            return httpClient.execute(httpPut, response -> {
                return response.getCode() == 200;
            });

        } catch (IOException e) {
            // Log the exception
            return false;
        }
    }
}
