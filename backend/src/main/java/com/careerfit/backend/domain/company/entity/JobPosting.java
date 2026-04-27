package com.careerfit.backend.domain.company.entity;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class JobPosting {

    private Long id;
    private Long companyId;
    private String title;
    private String url;
    private String status;       // ACTIVE / CLOSED / SCHEDULED
    private String jobType;      // IT / DIGITAL / BACKEND / FULLSTACK / FRONTEND / DATA / INFRA / AI / SECURITY / ETC
    private String season;       // 예: 2025_상반기
    private String rawText;
    private LocalDate startedAt;
    private LocalDate deadline;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
