package com.droidevs.safety_gear_tracker.service;

import com.droidevs.safety_gear_tracker.handler.exception.S3OperationException;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import java.io.File;
import java.io.InputStream;

public interface S3Service {
    void uploadFile(String key, InputStream inputStream) throws S3OperationException;

    void uploadFile(String key, File file) throws S3OperationException;

    byte[] downloadFile(String key) throws S3OperationException;

    ResponseInputStream<GetObjectResponse> downloadFileAsStream(String key) throws S3OperationException;

    void deleteFile(String key) throws S3OperationException;
}
