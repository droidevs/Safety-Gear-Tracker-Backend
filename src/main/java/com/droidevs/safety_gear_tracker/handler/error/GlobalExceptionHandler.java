package com.droidevs.safety_gear_tracker.handler.error;

import com.droidevs.safety_gear_tracker.handler.exception.*;
import jakarta.mail.MessagingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashSet;
import java.util.Set;

import static com.droidevs.safety_gear_tracker.handler.error.BusinessErrorCode.*;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(LockedException.class)
    public ResponseEntity<ExceptionResponse> handleException(LockedException exp) {
        log.error("Account Locked: {}", exp.getMessage());
        return ResponseEntity
                .status(ACCOUNT_LOCKED.getHttpStatus())
                .body(
                        ExceptionResponse.builder()
                                .businessErrorCode(ACCOUNT_LOCKED.getCode())
                                .businessErrorDescription(ACCOUNT_LOCKED.getDescription())
                                .error(exp.getMessage())
                                .build()
                );
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<ExceptionResponse> handleException(DisabledException exp) {
        log.error("Account Disabled: {}", exp.getMessage());
        return ResponseEntity
                .status(ACCOUNT_DISABLED.getHttpStatus())
                .body(
                        ExceptionResponse.builder()
                                .businessErrorCode(ACCOUNT_DISABLED.getCode())
                                .businessErrorDescription(ACCOUNT_DISABLED.getDescription())
                                .error(exp.getMessage())
                                .build()
                );
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ExceptionResponse> handleException(BadCredentialsException exp) {
        log.error("Bad Credentials: {}", exp.getMessage());
        return ResponseEntity
                .status(BAD_CREDENTIALS.getHttpStatus())
                .body(
                        ExceptionResponse.builder()
                                .businessErrorCode(BAD_CREDENTIALS.getCode())
                                .businessErrorDescription(BAD_CREDENTIALS.getDescription())
                                .error(BAD_CREDENTIALS.getDescription())
                                .build()
                );
    }

    @ExceptionHandler(MessagingException.class)
    public ResponseEntity<ExceptionResponse> handleException(MessagingException exp) {
        log.error("Messaging Exception: {}", exp.getMessage());
        return ResponseEntity
                .status(EMAIL_SEND_FAILED.getHttpStatus())
                .body(
                        ExceptionResponse.builder()
                                .businessErrorCode(EMAIL_SEND_FAILED.getCode())
                                .businessErrorDescription(EMAIL_SEND_FAILED.getDescription())
                                .error(exp.getMessage())
                                .build()
                );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ExceptionResponse> handleException(MethodArgumentNotValidException exp) {
        log.error("Validation Error: {}", exp.getMessage());
        Set<String> errors = new HashSet<>();
        exp.getBindingResult().getAllErrors()
                .forEach(error -> {
                            var errorMessage = error.getDefaultMessage();
                            errors.add(errorMessage);
                        }
                );
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST) // Use general BAD_REQUEST for validation errors
                .body(
                        ExceptionResponse.builder()
                                .businessErrorCode(INVALID_INPUT.getCode())
                                .businessErrorDescription(INVALID_INPUT.getDescription())
                                .validationErrors(errors)
                                .build()
                );
    }

    // Custom Exception Handlers

    @ExceptionHandler(AccountAlreadyVerifiedException.class)
    public ResponseEntity<ExceptionResponse> handleException(AccountAlreadyVerifiedException exp) {
        log.error("AccountAlreadyVerifiedException: {}", exp.getMessage());
        return ResponseEntity
                .status(ACCOUNT_ALREADY_VERIFIED.getHttpStatus())
                .body(
                        ExceptionResponse.builder()
                                .businessErrorCode(ACCOUNT_ALREADY_VERIFIED.getCode())
                                .businessErrorDescription(ACCOUNT_ALREADY_VERIFIED.getDescription())
                                .error(exp.getMessage())
                                .build()
                );
    }

    @ExceptionHandler(AlreadyUsedOtpException.class)
    public ResponseEntity<ExceptionResponse> handleException(AlreadyUsedOtpException exp) {
        log.error("AlreadyUsedOtpException: {}", exp.getMessage());
        return ResponseEntity
                .status(OTP_ALREADY_USED.getHttpStatus())
                .body(
                        ExceptionResponse.builder()
                                .businessErrorCode(OTP_ALREADY_USED.getCode())
                                .businessErrorDescription(OTP_ALREADY_USED.getDescription())
                                .error(exp.getMessage())
                                .build()
                );
    }

    @ExceptionHandler(InvalidOtpException.class)
    public ResponseEntity<ExceptionResponse> handleException(InvalidOtpException exp) {
        log.error("InvalidOtpException: {}", exp.getMessage());
        return ResponseEntity
                .status(INVALID_OTP.getHttpStatus())
                .body(
                        ExceptionResponse.builder()
                                .businessErrorCode(INVALID_OTP.getCode())
                                .businessErrorDescription(INVALID_OTP.getDescription())
                                .error(exp.getMessage())
                                .build()
                );
    }

    @ExceptionHandler(InvalidPasswordException.class)
    public ResponseEntity<ExceptionResponse> handleException(InvalidPasswordException exp) {
        log.error("InvalidPasswordException: {}", exp.getMessage());
        return ResponseEntity
                .status(INCORRECT_CURRENT_PASSWORD.getHttpStatus())
                .body(
                        ExceptionResponse.builder()
                                .businessErrorCode(INCORRECT_CURRENT_PASSWORD.getCode())
                                .businessErrorDescription(INCORRECT_CURRENT_PASSWORD.getDescription())
                                .error(exp.getMessage())
                                .build()
                );
    }

    @ExceptionHandler(InvalidVerificationCodeException.class)
    public ResponseEntity<ExceptionResponse> handleException(InvalidVerificationCodeException exp) {
        log.error("InvalidVerificationCodeException: {}", exp.getMessage());
        return ResponseEntity
                .status(INVALID_VERIFICATION_CODE.getHttpStatus())
                .body(
                        ExceptionResponse.builder()
                                .businessErrorCode(INVALID_VERIFICATION_CODE.getCode())
                                .businessErrorDescription(INVALID_VERIFICATION_CODE.getDescription())
                                .error(exp.getMessage())
                                .build()
                );
    }

    @ExceptionHandler(OtpExpiredException.class)
    public ResponseEntity<ExceptionResponse> handleException(OtpExpiredException exp) {
        log.error("OtpExpiredException: {}", exp.getMessage());
        return ResponseEntity
                .status(OTP_EXPIRED.getHttpStatus())
                .body(
                        ExceptionResponse.builder()
                                .businessErrorCode(OTP_EXPIRED.getCode())
                                .businessErrorDescription(OTP_EXPIRED.getDescription())
                                .error(exp.getMessage())
                                .build()
                );
    }

    @ExceptionHandler(PasswordChangeTooFrequentException.class)
    public ResponseEntity<ExceptionResponse> handleException(PasswordChangeTooFrequentException exp) {
        log.error("PasswordChangeTooFrequentException: {}", exp.getMessage());
        return ResponseEntity
                .status(PASSWORD_CHANGE_TOO_FREQUENT.getHttpStatus())
                .body(
                        ExceptionResponse.builder()
                                .businessErrorCode(PASSWORD_CHANGE_TOO_FREQUENT.getCode())
                                .businessErrorDescription(PASSWORD_CHANGE_TOO_FREQUENT.getDescription())
                                .error(exp.getMessage())
                                .build()
                );
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ExceptionResponse> handleException(ResourceNotFoundException exp) {
        log.error("ResourceNotFoundException: {}", exp.getMessage());
        return ResponseEntity
                .status(RESOURCE_NOT_FOUND.getHttpStatus())
                .body(
                        ExceptionResponse.builder()
                                .businessErrorCode(RESOURCE_NOT_FOUND.getCode())
                                .businessErrorDescription(RESOURCE_NOT_FOUND.getDescription())
                                .error(exp.getMessage())
                                .build()
                );
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ExceptionResponse> handleException(UserAlreadyExistsException exp) {
        log.error("UserAlreadyExistsException: {}", exp.getMessage());
        return ResponseEntity
                .status(USER_ALREADY_EXISTS.getHttpStatus())
                .body(
                        ExceptionResponse.builder()
                                .businessErrorCode(USER_ALREADY_EXISTS.getCode())
                                .businessErrorDescription(USER_ALREADY_EXISTS.getDescription())
                                .error(exp.getMessage())
                                .build()
                );
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ExceptionResponse> handleException(UserNotFoundException exp) {
        log.error("UserNotFoundException: {}", exp.getMessage());
        return ResponseEntity
                .status(USER_NOT_FOUND.getHttpStatus())
                .body(
                        ExceptionResponse.builder()
                                .businessErrorCode(USER_NOT_FOUND.getCode())
                                .businessErrorDescription(USER_NOT_FOUND.getDescription())
                                .error(exp.getMessage())
                                .build()
                );
    }

    @ExceptionHandler(SelfDeactivationException.class)
    public ResponseEntity<ExceptionResponse> handleException(SelfDeactivationException exp) {
        log.error("SelfDeactivationException: {}", exp.getMessage());
        return ResponseEntity
                .status(SELF_DEACTIVATION_NOT_ALLOWED.getHttpStatus())
                .body(
                        ExceptionResponse.builder()
                                .businessErrorCode(SELF_DEACTIVATION_NOT_ALLOWED.getCode())
                                .businessErrorDescription(SELF_DEACTIVATION_NOT_ALLOWED.getDescription())
                                .error(exp.getMessage())
                                .build()
                );
    }

    @ExceptionHandler(SelfDeletionException.class)
    public ResponseEntity<ExceptionResponse> handleException(SelfDeletionException exp) {
        log.error("SelfDeletionException: {}", exp.getMessage());
        return ResponseEntity
                .status(SELF_DELETION_NOT_ALLOWED.getHttpStatus())
                .body(
                        ExceptionResponse.builder()
                                .businessErrorCode(SELF_DELETION_NOT_ALLOWED.getCode())
                                .businessErrorDescription(SELF_DELETION_NOT_ALLOWED.getDescription())
                                .error(exp.getMessage())
                                .build()
                );
    }

    @ExceptionHandler(VerificationCodeExpiredException.class)
    public ResponseEntity<ExceptionResponse> handleException(VerificationCodeExpiredException exp) {
        log.error("VerificationCodeExpiredException: {}", exp.getMessage());
        return ResponseEntity
                .status(VERIFICATION_CODE_EXPIRED.getHttpStatus())
                .body(
                        ExceptionResponse.builder()
                                .businessErrorCode(VERIFICATION_CODE_EXPIRED.getCode())
                                .businessErrorDescription(VERIFICATION_CODE_EXPIRED.getDescription())
                                .error(exp.getMessage())
                                .build()
                );
    }

    @ExceptionHandler(S3OperationException.class)
    public ResponseEntity<ExceptionResponse> handleException(S3OperationException exp) {
        log.error("S3OperationException: {}", exp.getMessage());
        return ResponseEntity
                .status(S3_OPERATION_FAILED.getHttpStatus())
                .body(
                        ExceptionResponse.builder()
                                .businessErrorCode(S3_OPERATION_FAILED.getCode())
                                .businessErrorDescription(S3_OPERATION_FAILED.getDescription())
                                .error(exp.getMessage())
                                .build()
                );
    }

    @ExceptionHandler(JsonParsingException.class)
    public ResponseEntity<ExceptionResponse> handleException(JsonParsingException exp) {
        log.error("JsonParsingException: {}", exp.getMessage());
        return ResponseEntity
                .status(JSON_PARSING_ERROR.getHttpStatus())
                .body(
                        ExceptionResponse.builder()
                                .businessErrorCode(JSON_PARSING_ERROR.getCode())
                                .businessErrorDescription(JSON_PARSING_ERROR.getDescription())
                                .error(exp.getMessage())
                                .build()
                );
    }

    @ExceptionHandler(CameraManagementException.class)
    public ResponseEntity<ExceptionResponse> handleException(CameraManagementException exp) {
        log.error("CameraManagementException: {}", exp.getMessage());
        return ResponseEntity
                .status(CAMERA_MANAGEMENT_FAILED.getHttpStatus())
                .body(
                        ExceptionResponse.builder()
                                .businessErrorCode(CAMERA_MANAGEMENT_FAILED.getCode())
                                .businessErrorDescription(CAMERA_MANAGEMENT_FAILED.getDescription())
                                .error(exp.getMessage())
                                .build()
                );
    }

    @ExceptionHandler(ImageProcessingException.class)
    public ResponseEntity<ExceptionResponse> handleException(ImageProcessingException exp) {
        log.error("ImageProcessingException: {}", exp.getMessage());
        return ResponseEntity
                .status(IMAGE_PROCESSING_FAILED.getHttpStatus())
                .body(
                        ExceptionResponse.builder()
                                .businessErrorCode(IMAGE_PROCESSING_FAILED.getCode())
                                .businessErrorDescription(IMAGE_PROCESSING_FAILED.getDescription())
                                .error(exp.getMessage())
                                .build()
                );
    }

    @ExceptionHandler(VideoRecordingException.class)
    public ResponseEntity<ExceptionResponse> handleException(VideoRecordingException exp) {
        log.error("VideoRecordingException: {}", exp.getMessage());
        return ResponseEntity
                .status(VIDEO_RECORDING_FAILED.getHttpStatus())
                .body(
                        ExceptionResponse.builder()
                                .businessErrorCode(VIDEO_RECORDING_FAILED.getCode())
                                .businessErrorDescription(VIDEO_RECORDING_FAILED.getDescription())
                                .error(exp.getMessage())
                                .build()
                );
    }

    @ExceptionHandler(StreamingException.class)
    public ResponseEntity<ExceptionResponse> handleException(StreamingException exp) {
        log.error("StreamingException: {}", exp.getMessage());
        return ResponseEntity
                .status(STREAMING_FAILED.getHttpStatus())
                .body(
                        ExceptionResponse.builder()
                                .businessErrorCode(STREAMING_FAILED.getCode())
                                .businessErrorDescription(STREAMING_FAILED.getDescription())
                                .error(exp.getMessage())
                                .build()
                );
    }

    @ExceptionHandler(GlobalBaseException.class)
    public ResponseEntity<ExceptionResponse> handleGlobalBaseException(GlobalBaseException exp) {
        log.error("GlobalBaseException: {}", exp.getMessage(), exp);
        // Fallback for any GlobalBaseException that wasn't specifically handled
        HttpStatus status = exp.getClass().isAnnotationPresent(org.springframework.web.bind.annotation.ResponseStatus.class) ?
                        exp.getClass().getAnnotation(org.springframework.web.bind.annotation.ResponseStatus.class).value() :
                        HttpStatus.INTERNAL_SERVER_ERROR;
        return ResponseEntity
                .status(status)
                .body(
                        ExceptionResponse.builder()
                                .businessErrorCode(INTERNAL_SERVER_ERROR.getCode())
                                .businessErrorDescription(INTERNAL_SERVER_ERROR.getDescription())
                                .error(exp.getMessage())
                                .build()
                );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ExceptionResponse> handleGeneralException(Exception exp) {
        log.error("Unhandled Exception: {}", exp.getMessage(), exp);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(
                        ExceptionResponse.builder()
                                .businessErrorCode(INTERNAL_SERVER_ERROR.getCode())
                                .businessErrorDescription("An unexpected error occurred. Please try again later.")
                                .error(exp.getMessage())
                                .build()
                );
    }
}
