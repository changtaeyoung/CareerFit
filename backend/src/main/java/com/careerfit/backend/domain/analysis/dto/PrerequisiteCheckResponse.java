package com.careerfit.backend.domain.analysis.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Schema(description = "공고 지원 자격 사전 체크 응답 DTO")
@Getter
@Builder
public class PrerequisiteCheckResponse {

    @Schema(description = "채용공고 ID", example = "1")
    private Long postingId;

    @Schema(description = "지원 자격 충족 여부 — false면 failedRequirements 확인 필요", example = "false")
    private boolean eligible;

    @Schema(description = "미충족 필수 조건 목록 — eligible=true이면 빈 리스트")
    private List<FailedRequirement> failedRequirements;

    @Schema(description = "미충족 필수 조건 항목")
    @Getter
    @Builder
    public static class FailedRequirement {

        @Schema(description = "조건 유형 (CERT / LANGUAGE)", example = "CERT")
        private String type;

        @Schema(description = "조건 항목명", example = "정보처리기사")
        private String itemName;

        @Schema(description = "기업 요구 기준값 (어학인 경우)", example = "700")
        private String requiredValue;

        @Schema(description = "사용자 현재 보유값 (없으면 null)", example = "없음")
        private String userValue;

        @Schema(description = "사용자에게 보여줄 안내 메시지", example = "정보처리기사 자격증이 없습니다. 지원 자격 조건입니다.")
        private String message;
    }
}
