package com.careerfit.backend.domain.company.entity;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 기업 연봉 정보 엔티티 (company_salary 테이블 매핑)
 *
 * 한 기업에 대해 (salary_type, year) 조합으로 여러 row가 쌓이는 구조.
 * 프론트에는 보통 가장 최근 연도의 ENTRY 1건 + AVERAGE 1건만 보여준다.
 *
 * 단위: 만원
 * salary_type: ENTRY (신입 초봉) | AVERAGE (전체 평균)
 * source: ALIO_API | ALIO_CRAWL | DART_API | POSTING | MANUAL
 */
@Getter
@Builder
public class CompanySalary {

    private Long id;
    private Long companyId;
    private String salaryType;   // ENTRY | AVERAGE
    private Integer amount;      // 단위: 만원
    private Integer year;        // 데이터 기준 연도
    private String source;       // 출처 (5가지 enum 중 하나)
    private String sourceUrl;    // 원본 URL (크롤링·API 디버깅용)
    private LocalDateTime collectedAt;  // 원본 공시 시점
    private LocalDateTime createdAt;    // DB 삽입 시점
}
