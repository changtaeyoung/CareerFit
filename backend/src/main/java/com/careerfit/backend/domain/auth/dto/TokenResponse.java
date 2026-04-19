package com.careerfit.backend.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "로그인 응답 — JWT 토큰 + 사용자 식별 정보")
public class TokenResponse {

    @Schema(description = "Access Token (JWT)")
    private String accessToken;

    @Schema(description = "Refresh Token (JWT)")
    private String refreshToken;

    @Builder.Default
    @Schema(description = "토큰 타입", example = "Bearer")
    private String tokenType = "Bearer";

    // ── 사용자 식별 정보 (프론트 authStore 초기화용) ──────────

    @Schema(description = "사용자 ID", example = "1")
    private Long userId;

    @Schema(description = "이메일", example = "taeyoung@careerfit.com")
    private String email;

    @Schema(description = "사용자 이름", example = "창태영")
    private String name;
}
