package com.careerfit.backend.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "스펙 기본 정보 등록 응답")
public class SpecBasicResponse {

    @Schema(description = "생성된 스펙 버전 ID", example = "1")
    private Long versionId;

    @Schema(description = "버전 번호", example = "1")
    private Integer versionNo;
}