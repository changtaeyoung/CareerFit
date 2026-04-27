package com.careerfit.backend.domain.bookmark.entity;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class Bookmark {

    private Long id;

    private Long userId;

    // 'COMPANY' | 'POSTING'
    private String targetType;

    private Long companyId;

    private Long postingId;

    private LocalDateTime createdAt;
}
