package com.careerfit.backend.domain.analysis.entity;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AnalysisRecommendation {

    private Long id;
    private Long reportId;
    private String priority;           // P1(즉시) / P2(단기) / P3(장기)
    private String category;           // CERT / SKILL / LANGUAGE / EXPERIENCE / ETC
    private String content;            // 액션 플랜 내용
    private int expectedScoreGain;     // 이행 시 예상 점수 상승
    private int sortOrder;             // 표시 우선순위 정렬
}
