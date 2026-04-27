package com.careerfit.backend.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "토큰 갱신 요청")
public class TokenRefreshRequest {

    @NotBlank(message = "Refresh Token은 필수입니다.")
    @Schema(description = "발급받았던 Refresh Token", example = "eyJhb...")
    private String refreshToken;

}
