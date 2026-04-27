package com.careerfit.backend.infrastructure.external.ai.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * FastAPI 정성 분석 요청 DTO.
 *
 * POST http://localhost:8000/api/analysis/qualitative 로 전송.
 *
 * ── snake_case인 이유 ────────────────────────────────────────────────────
 * FastAPI(Python)의 Pydantic 모델은 기본적으로 snake_case를 사용한다.
 * Spring의 Jackson은 @JsonProperty로 필드명을 매핑하거나
 * application.yml에서 snake_case 직렬화 설정을 하면 된다.
 * 여기서는 필드명 자체를 snake_case로 선언해서 별도 설정 없이 동작하도록 함.
 */
@Getter
@Builder
public class QualitativeRequest {

    /** 기업명. null이면 FastAPI에서 생략 처리 */
    private String company_name;

    /** 직무명. null이면 FastAPI에서 생략 처리 */
    private String job_title;

    /** 기업 인재상 (company.talent_image). null이면 FastAPI에서 생략 처리 */
    private String talent_image;

    /** 공고 직무 내용 (job_posting.raw_text). null이면 FastAPI에서 생략 처리 */
    private String job_description;

    /** 자소서 문항 + 답변 목록 */
    private List<CoverLetterItem> cover_letters;

    @Getter
    @Builder
    public static class CoverLetterItem {
        private String question;  // 문항 내용
        private String content;   // 사용자 답변
    }
}
