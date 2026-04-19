package com.careerfit.backend.common.exception;

import com.careerfit.backend.common.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
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

    // UNIQUE 제약 위반 전용 — 중복 등록 시도 시 서비스 선체크를 뚫고 들어온 경우 (race condition 등)
    // PostgreSQL SQLSTATE 23505가 Spring에서 DuplicateKeyException으로 래핑됨
    @ExceptionHandler(DuplicateKeyException.class)
    public ResponseEntity<ApiResponse<?>> handleDuplicateKey(DuplicateKeyException e) {
        log.warn("[DuplicateKey] 중복 키 위반: {}", e.getMostSpecificCause().getMessage());
        return new ResponseEntity<>(
                ApiResponse.fail(ErrorCode.DATA_INTEGRITY_VIOLATION),
                ErrorCode.DATA_INTEGRITY_VIOLATION.getStatus()
        );
    }

    // CHECK 제약, NOT NULL 제약, FK 제약 등 모든 데이터 정합성 위반
    // DuplicateKeyException이 아닌 나머지 DataIntegrityViolationException 케이스 커버
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<?>> handleDataIntegrityViolation(DataIntegrityViolationException e) {
        log.warn("[DataIntegrity] 데이터 정합성 위반: {}", e.getMostSpecificCause().getMessage());
        return new ResponseEntity<>(
                ApiResponse.fail(ErrorCode.DATA_INTEGRITY_VIOLATION),
                ErrorCode.DATA_INTEGRITY_VIOLATION.getStatus()
        );
    }

    // DTO에 적어준 메시지를 그대로 반환시켜주기 위함
    // @Valid를 통해 DTO에 적은 메시지 반환시켜준다는 것을 의미함
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<?>> handleValidException(
            MethodArgumentNotValidException e) {

        // 여러 필드 에러 중 첫 번째 메시지만 꺼냄
        String message = e.getBindingResult()
                .getFieldErrors()
                .get(0)
                .getDefaultMessage();


        log.error("[Validation] message: {}", message);

        return new ResponseEntity<>(
                ApiResponse.fail(ErrorCode.INVALID_INPUT),
                HttpStatus.BAD_REQUEST
        );
    }

    // 최후의 안전망 — 위에서 처리 못한 모든 예외
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleException (Exception e) {
        log.error("[GlobalException] 예상치 못한 오류 발생 : {}", e.getMessage(), e);
        return new ResponseEntity<>(
                ApiResponse.fail(ErrorCode.INTERNAL_SERVER_ERROR),
                ErrorCode.INTERNAL_SERVER_ERROR.getStatus()
        );
    }
}

