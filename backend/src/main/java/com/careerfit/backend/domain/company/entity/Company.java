package com.careerfit.backend.domain.company.entity;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class Company {

    private Long id;
    private String dartCode;
    private String alioCode;
    private String name;
    private String industry;
    private String companyType;
    private String location;
    private String website;
    private Integer employeeCount;
    private Integer foundedYear;
    private boolean isPublic;
    private String vision;
    private String talentImage;
    private String businessOverview;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
