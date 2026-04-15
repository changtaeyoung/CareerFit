package com.careerfit.backend.domain.user.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
@Schema(description = "사용자 스펙 버전")
public class UserSpecVersion {

    @Schema(description = "스펙 버전 ID")
    private Long id;

    @Schema(description = "사용자 ID")
    private Long userId;

    @Schema(description = "버전 번호", example = "1")
    private Integer versionNo;

    @Schema(description = "학력", example = "졸업")
    private String education;

    @Schema(description = "학교명", example = "한국대학교")
    private String university;

    @Schema(description = "학점", example = "3.8")
    private BigDecimal gpa;

    @Schema(description = "현재 활성 버전 여부", example = "true")
    private Boolean isCurrent;

    private LocalDateTime createdAt;
}