package com.careerfit.backend.common.response;

import com.careerfit.backend.common.exception.ErrorCode;
import lombok.Getter;

@Getter
public class ApiResponse<T> {

    private final boolean success;
    private final String message;
    private final T data;

    private ApiResponse(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    public static <T> ApiResponse<T> success (T data) {
        return new ApiResponse<>(true, "성공", data);
    }

    public static <T> ApiResponse<T> success (String message, T data) {
        return new ApiResponse<>(true, message, data);
    }

    public static <T> ApiResponse<T> fail (ErrorCode errorCode) {
        return new ApiResponse<>(false, errorCode.getMessage(), null);
    }
}
