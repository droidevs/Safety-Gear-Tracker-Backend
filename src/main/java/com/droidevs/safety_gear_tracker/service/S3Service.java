package com.droidevs.safety_gear_tracker.service;

import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public interface S3Service {
    void uploadFile(String key, InputStream inputStream) throws IOException;
    void uploadFile(String key, File file);
    byte[] downloadFile(String key) throws IOException;
    ResponseInputStream<GetObjectResponse> downloadFileAsStream(String key);
    void deleteFile(String key) throws IOException;
}
