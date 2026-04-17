package com.careerfit.backend.domain.analysis.entity;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AnalysisGap {

    private Long id;
    private Long reportId;
    private String gapType;          // CERT / LANGUAGE / SKILL
    private String requirementType;  // REQUIRED / PREFERRED
    private String itemName;         // 정보처리기사 / TOEIC 등
    private String status;           // MEET / FAIL
    private String requiredValue;    // 기업 요구 기준값 (800점 / IM2 등)
    private String userValue;        // 사용자 보유값
    private int bonusScore;          // 우대 충족 시 획득 가점 (PREFERRED + MEET 인 경우)
    private String description;      // 부족 사유 설명
}
