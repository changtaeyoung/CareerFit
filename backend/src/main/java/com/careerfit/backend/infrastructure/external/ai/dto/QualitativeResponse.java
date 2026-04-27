package com.careerfit.backend.infrastructure.external.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * FastAPI 정성 분석 응답 DTO.
 *
 * FastAPI가 반환하는 JSON 구조:
 * {
 *   "total_score": 35,
 *   "breakdown": {"talent_match": 12, "job_understanding": 13, ...},
 *   "strengths": ["강점1", "강점2"],
 *   "weaknesses": ["개선점1"],
 *   "feedback": "전반적으로 잘 작성되었습니다..."
 * }
 */
@Getter
@NoArgsConstructor
public class QualitativeResponse {

    /** 정성 가점 총점 (0~50) */
    @JsonProperty("total_score")
    private int totalScore;

    /** 항목별 점수 */
    @JsonProperty("breakdown")
    private Map<String, Integer> breakdown;

    /** 강점 목록 */
    @JsonProperty("strengths")
    private List<String> strengths;

    /** 개선점 목록 */
    @JsonProperty("weaknesses")
    private List<String> weaknesses;

    /** 종합 피드백 */
    @JsonProperty("feedback")
    private String feedback;

    /** FastAPI 호출 실패 시 기본값 반환용 팩토리 메서드 */
    public static QualitativeResponse empty(String reason) {
        QualitativeResponse r = new QualitativeResponse();
        r.totalScore = 0;
        r.feedback = reason;
        r.strengths = List.of();
        r.weaknesses = List.of();
        r.breakdown = Map.of();
        return r;
    }
}
