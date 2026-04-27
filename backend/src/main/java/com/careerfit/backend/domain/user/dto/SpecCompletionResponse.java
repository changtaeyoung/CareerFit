package com.careerfit.backend.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Schema(description = "스펙 완성도 응답 DTO")
@Getter
@Builder
public class SpecCompletionResponse {

    @Schema(description = "스펙 버전 ID", example = "1")
    private Long versionId;

    @Schema(description = "전체 완성도 (0~100)", example = "67")
    private int totalScore;

    // ── 필수 항목 (등록 여부만 체크) ─────────────────

    @Schema(description = "기본 정보 등록 여부 (학력·학교명)", example = "true")
    private boolean hasBasicInfo;

    @Schema(description = "희망직무 등록 여부", example = "true")
    private boolean hasWantedJob;

    @Schema(description = "기술스택 등록 여부", example = "true")
    private boolean hasSkill;

    // ── 선택 항목 (count 노출) ────────────────────────

    @Schema(description = "자격증 등록 건수", example = "2")
    private int certificateCount;

    @Schema(description = "어학 등록 건수", example = "1")
    private int languageCount;

    @Schema(description = "경력/인턴 등록 건수", example = "1")
    private int internCount;

    @Schema(description = "프로젝트 등록 건수", example = "2")
    private int projectCount;

    @Schema(description = "수상 등록 건수", example = "0")
    private int awardCount;
}
