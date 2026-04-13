package com.careerfit.backend.common.exception;

import com.careerfit.backend.common.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ApiResponse<?>> handleCustomException (CustomException e) {
        log.error("[CustomException] {} : {}", e.getErrorCode(), e.getMessage());
        return new ResponseEntity<>(
                ApiResponse.fail(e.getErrorCode()),
                e.getErrorCode().getStatus()
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleException (Exception e) {
        log.error("[GlobalException] 예상치 못한 오류 발생 : {}", e.getMessage(), e);
        return new ResponseEntity<>(
                ApiResponse.fail(ErrorCode.INTERNAL_SERVER_ERROR),
                ErrorCode.INTERNAL_SERVER_ERROR.getStatus()
        );
    }
}
