package com.careerfit.backend.domain.coverletter.entity;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 채용공고 자소서 문항 엔티티.
 *
 * 문항은 관리자가 등록하고, 사용자는 문항을 선택하여 자소서를 작성한다.
 * cover_letter.question_id → job_posting_question.id 참조.
 */
@Getter
@Builder
public class JobPostingQuestion {
    private Long id;
    private Long postingId;
    private String question;
    private Integer sortOrder;
    private LocalDateTime createdAt;
}
