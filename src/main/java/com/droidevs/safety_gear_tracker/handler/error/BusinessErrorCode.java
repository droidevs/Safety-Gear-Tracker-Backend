package com.droidevs.safety_gear_tracker.handler.error;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum BusinessErrorCode {

    // General Errors
    NO_CODE(0, HttpStatus.NOT_IMPLEMENTED, "No Code"),
    INTERNAL_SERVER_ERROR(1, HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected internal error occurred."),
    INVALID_INPUT(2, HttpStatus.BAD_REQUEST, "Invalid input provided."),

    // Authentication & User Management Errors (300-399)
    INCORRECT_CURRENT_PASSWORD(300, HttpStatus.BAD_REQUEST, "Current password is incorrect"),
    NEW_PASSWORD_DOES_NOT_MATCH(301, HttpStatus.BAD_REQUEST, "The new password does not match"),
    ACCOUNT_LOCKED(302, HttpStatus.FORBIDDEN, "User account is locked"),
    ACCOUNT_DISABLED(303, HttpStatus.FORBIDDEN, "User account is disabled"),
    BAD_CREDENTIALS(304, HttpStatus.UNAUTHORIZED, "Login and / or password is incorrect"),
    ACCOUNT_ALREADY_VERIFIED(305, HttpStatus.CONFLICT, "This account has already been verified."),
    OTP_ALREADY_USED(306, HttpStatus.CONFLICT, "This OTP has already been used."),
    INVALID_OTP(307, HttpStatus.BAD_REQUEST, "The OTP provided is invalid."),
    OTP_EXPIRED(308, HttpStatus.GONE, "The OTP has expired. Please request a new one."),
    INVALID_VERIFICATION_CODE(309, HttpStatus.BAD_REQUEST, "Invalid verification code."),
    VERIFICATION_CODE_EXPIRED(310, HttpStatus.GONE, "The verification code has expired. Please request a new one."),
    PASSWORD_CHANGE_TOO_FREQUENT(311, HttpStatus.TOO_MANY_REQUESTS, "You can only change your password once a week."),
    USER_ALREADY_EXISTS(312, HttpStatus.CONFLICT, "User with this email already exists."),
    USER_NOT_FOUND(313, HttpStatus.NOT_FOUND, "User not found."),
    SELF_DEACTIVATION_NOT_ALLOWED(314, HttpStatus.BAD_REQUEST, "You cannot deactivate your own account."),
    SELF_DELETION_NOT_ALLOWED(315, HttpStatus.BAD_REQUEST, "You cannot delete your own account."),

    // Resource Management Errors (400-499)
    RESOURCE_NOT_FOUND(400, HttpStatus.NOT_FOUND, "The requested resource was not found."),
    CAMERA_NOT_FOUND(401, HttpStatus.NOT_FOUND, "Camera not found."),
    ZONE_NOT_FOUND(402, HttpStatus.NOT_FOUND, "Zone not found."),
    ALERT_NOT_FOUND(403, HttpStatus.NOT_FOUND, "Alert not found."),
    RECORDING_NOT_FOUND(404, HttpStatus.NOT_FOUND, "Recording not found."),

    // External Service / Integration Errors (500-599)
    S3_OPERATION_FAILED(500, HttpStatus.INTERNAL_SERVER_ERROR, "Failed to perform S3 operation."),
    JSON_PARSING_ERROR(501, HttpStatus.INTERNAL_SERVER_ERROR, "Error parsing JSON response."),
    EMAIL_SEND_FAILED(502, HttpStatus.INTERNAL_SERVER_ERROR, "Failed to send email."),
    CAMERA_MANAGEMENT_FAILED(503, HttpStatus.INTERNAL_SERVER_ERROR, "Failed to manage camera device."),
    IMAGE_PROCESSING_FAILED(504, HttpStatus.INTERNAL_SERVER_ERROR, "Failed to process image."),
    VIDEO_RECORDING_FAILED(505, HttpStatus.INTERNAL_SERVER_ERROR, "Failed during video recording process."),
    STREAMING_FAILED(506, HttpStatus.INTERNAL_SERVER_ERROR, "Failed during video streaming process.");


    private final int code;
    private final String description;
    private final HttpStatus httpStatus;

    BusinessErrorCode(int code, HttpStatus httpStatus, String description){
        this.code = code;
        this.description = description;
        this.httpStatus = httpStatus;
    }
}
