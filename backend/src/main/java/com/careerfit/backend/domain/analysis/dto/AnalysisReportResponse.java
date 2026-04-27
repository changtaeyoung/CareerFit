package com.careerfit.backend.domain.analysis.dto;

import com.careerfit.backend.domain.analysis.entity.AnalysisGap;
import com.careerfit.backend.domain.analysis.entity.AnalysisRecommendation;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "분석 리포트 상세 응답 DTO — 점수 분석, 갭, 액션 플랜 포함")
@Getter
@Builder
public class AnalysisReportResponse {

    // ── 기본 정보 ──────────────────────────────────────────
    @Schema(description = "분석 리포트 ID", example = "1")
    private Long reportId;

    @Schema(description = "기업명", example = "IBK기업은행")
    private String companyName;

    @Schema(description = "채용공고 제목", example = "2025 상반기 IBK기업은행 IT 신입 공채")
    private String postingTitle;

    @Schema(description = "분석에 사용된 스펙 버전 번호", example = "2")
    private int specVersionNo;

    // ── 점수 상세 ──────────────────────────────────────────
    @Schema(description = "필수조건 전부 충족 여부", example = "true")
    private boolean requiredAllMet;

    @Schema(description = "베이스 점수 (0 or 10)", example = "10")
    private int baseScore;

    @Schema(description = "우대 정량 가점 (자격증+어학+기술스택, 최대 40점)", example = "25")
    private int quantitativeBonus;

    @Schema(description = "정성 가점 (자소서 벡터 유사도, 최대 50점 — 현재 미구현)", example = "0")
    private int qualitativeBonus;

    @Schema(description = "최종 핏 점수 (0~100)", example = "75")
    private int totalScore;

    @Schema(description = "분석 상태 (COMPLETED/FAILED/PENDING)", example = "COMPLETED")
    private String status;

    @Schema(description = "분석 실행 일시", example = "2025-03-15T10:30:00")
    private LocalDateTime createdAt;

    // ── 갭 분석 ────────────────────────────────────────────
    @Schema(description = "항목별 갭 분석 결과 (자격증/어학/기술스택)")
    private List<AnalysisGap> gaps;

    // ── 액션 플랜 ──────────────────────────────────────────
    @Schema(description = "우선순위별 액션 플랜 (P1→P2→P3 순)")
    private List<AnalysisRecommendation> recommendations;

    // ── AI 정성 평가 피드백 ────────────────────────────────
    @Schema(description = "AI가 분석한 자소서 강점 목록", example = "[\"구체적인 경험 서술\", \"높은 직무 이해도\"]")
    private List<String> strengths;

    @Schema(description = "AI가 분석한 자소서 약점/개선점 목록", example = "[\"인재상 키워드 부족\"]")
    private List<String> weaknesses;

    @Schema(description = "AI 종합 피드백", example = "전반적으로 직무 이해도가 높으나 기업 인재상 연결이 부족합니다.")
    private String aiFeedback;

    // ── DB JSON String → List<String> 파싱 유틸 ──────────────
    private static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * DB에 TEXT로 저장된 JSON 배열 문자열을 List<String>으로 변환.
     * 파싱 실패 시 빈 리스트 반환 (방어적 처리).
     */
    public static List<String> parseJsonArray(String json) {
        if (json == null || json.isBlank()) return List.of();
        try {
            return MAPPER.readValue(json, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            return List.of();
        }
    }
}
