package com.careerfit.backend.domain.coverletter.entity;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class CoverLetter {

    private Long id;
    private Long userId;
    private Long postingId;
    private Long questionId;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
