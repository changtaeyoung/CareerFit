package com.careerfit.backend.domain.bookmark.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;

@Schema(description = "북마크 등록 요청 DTO")
@Getter
public class BookmarkRequest {

    @Schema(description = "북마크 대상 타입 (COMPANY | POSTING)", example = "POSTING")
    @NotBlank
    @Pattern(regexp = "^(COMPANY|POSTING)$", message = "targetType은 COMPANY 또는 POSTING이어야 합니다")
    private String targetType;

    @Schema(description = "북마크 대상 ID (기업 ID 또는 채용공고 ID)", example = "1")
    @NotNull
    private Long targetId;
}
