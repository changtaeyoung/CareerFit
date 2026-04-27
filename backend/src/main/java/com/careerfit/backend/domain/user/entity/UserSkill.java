package com.careerfit.backend.domain.user.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "사용자 기술스택")
public class UserSkill {

    @Schema(description = "기술스택 ID")
    private Long id;

    @Schema(description = "스펙 버전 ID")
    private Long specVersionId;

    @Schema(description = "기술 사전 ID")
    private Long skillId;

    @Schema(description = "기술명 (조회 시 skill_dictionary JOIN 결과)", example = "Spring Boot")
    private String skillName;

    @Schema(description = "카테고리 (조회 시 skill_dictionary JOIN 결과)", example = "BACKEND")
    private String category;

    @Schema(description = "숙련도 (BEGINNER | INTERMEDIATE | ADVANCED)", example = "INTERMEDIATE")
    private String proficiency;
}

