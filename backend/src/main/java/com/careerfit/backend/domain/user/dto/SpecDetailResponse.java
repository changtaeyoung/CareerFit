package com.careerfit.backend.domain.user.dto;

import com.careerfit.backend.domain.user.entity.*;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class SpecDetailResponse {

    private Long versionId;
    private Integer versionNo;
    private String education;
    private String university;
    private BigDecimal gpa;
    private LocalDateTime createdAt;

    private List<UserWantedJob> wantedJobs;
    private List<UserSkill>         skills;
    private List<UserCertificate>   certificates;
    private List<UserLanguageScore> languages;
    private List<UserIntern>        interns;
    private List<UserProject>       projects;
    private List<UserAward>         awards;
}