package com.careerfit.backend.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "비밀번호 변경 요청")
public class PasswordChangeRequest {

    @NotBlank(message = "현재 비밀번호를 입력해주세요")
    @Schema(description = "현재 비밀번호", example = "Current1!")
    private String currentPassword;

    @NotBlank(message = "새 비밀번호를 입력해주세요")
    @Pattern(
            regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,}$",
            message = "비밀번호는 영문, 숫자, 특수문자(@, $, !, %, *, #, ?, &)를 포함해야 합니다"
    )
    @Schema(description = "새 비밀번호", example = "NewPass1!")
    private String newPassword;

    @NotBlank(message = "새 비밀번호를 재입력해주세요")
    @Schema(description = "새 비밀번호 확인", example = "NewPass1!")
    private String newPasswordConfirm;
}