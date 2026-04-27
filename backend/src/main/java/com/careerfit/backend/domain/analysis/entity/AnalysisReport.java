package com.careerfit.backend.domain.analysis.entity;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class AnalysisReport {

    private Long id;
    private Long userId;
    private Long companyId;
    private Long jobPostingId;
    private Long specVersionId;

    private boolean requiredAllMet;  // 필수 게이트 통과 여부
    private int baseScore;           // 베이스 0 or 10점
    private int quantitativeBonus;   // 우대 정량 가점 (자격증+어학+기술스택, 최대 40점)
    private int qualitativeBonus;    // 정성 가점 (FastAPI 연동 전까지 항상 0)
    private int totalScore;          // 최종 합산 (최대 100점)

    private String status;           // PENDING / COMPLETED / FAILED
    private LocalDateTime createdAt;

    // AI 정성 평가 결과 (JSON String으로 DB 저장, 앱 레이어에서 List<String>으로 파싱)
    private String strengths;    // 강점 목록 JSON. 예: ["강점1","강점2"]
    private String weaknesses;   // 약점/개선점 목록 JSON
    private String aiFeedback;   // GPT 종합 피드백 텍스트
}
