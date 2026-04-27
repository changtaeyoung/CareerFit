package com.careerfit.backend.domain.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "관리자 자소서 문항 등록 요청")
@Getter
@NoArgsConstructor
public class AdminQuestionRequest {

    @Schema(description = "문항 내용", example = "지원동기 및 입사 후 목표를 작성해주세요. (500자)")
    @NotBlank
    private String question;

    @Schema(description = "문항 순서", example = "1")
    @NotNull
    @Min(1)
    private Integer sortOrder;
}
