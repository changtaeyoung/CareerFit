package com.careerfit.backend.domain.dictionary.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Schema(description = "기술스택 사전 응답 DTO")
@Getter
@Builder
public class SkillDictionaryResponse {

    @Schema(description = "사전 ID (user_skill.skill_id에 사용)", example = "1")
    private Long id;

    @Schema(description = "기술명", example = "Spring Boot")
    private String name;

    @Schema(description = "카테고리 (BACKEND | FRONTEND | DATABASE | INFRA | ETC 등)", example = "BACKEND")
    private String category;
}
