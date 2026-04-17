package com.careerfit.backend.domain.company.dto;

import com.careerfit.backend.domain.company.entity.Company;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Schema(description = "기업 상세 응답 DTO (vision, 인재상, 사업개요 포함)")
@Getter
@Builder
public class CompanyDetailResponse {

    @Schema(description = "기업 ID", example = "1")
    private Long id;

    @Schema(description = "기업명", example = "IBK기업은행")
    private String name;

    @Schema(description = "업종 (은행/보험/카드/증권/공기업/핀테크/기타)", example = "은행")
    private String industry;

    @Schema(description = "기업 유형 (대기업/공기업/금융지주/중견기업/스타트업)", example = "공기업")
    private String companyType;

    @Schema(description = "소재지", example = "서울특별시 중구 을지로 79")
    private String location;

    @Schema(description = "홈페이지 URL", example = "https://www.ibk.co.kr")
    private String website;

    @Schema(description = "임직원 수", example = "13000")
    private Integer employeeCount;

    @Schema(description = "설립 연도", example = "1961")
    private Integer foundedYear;

    @Schema(description = "상장 여부", example = "true")
    private boolean isPublic;

    @Schema(description = "기업 비전", example = "중소기업과 함께 성장하는 국민의 은행")
    private String vision;

    @Schema(description = "인재상", example = "도전적이고 창의적인 금융 전문가")
    private String talentImage;

    @Schema(description = "사업 개요", example = "IBK기업은행은 중소기업 전문 국책은행으로...")
    private String businessOverview;

    // Company 엔티티를 상세 응답 DTO로 변환 (vision, talentImage, businessOverview 포함)
    public static CompanyDetailResponse from(Company company) {
        return CompanyDetailResponse.builder()
                .id(company.getId())
                .name(company.getName())
                .industry(company.getIndustry())
                .companyType(company.getCompanyType())
                .location(company.getLocation())
                .website(company.getWebsite())
                .employeeCount(company.getEmployeeCount())
                .foundedYear(company.getFoundedYear())
                .isPublic(company.isPublic())
                .vision(company.getVision())
                .talentImage(company.getTalentImage())
                .businessOverview(company.getBusinessOverview())
                .build();
    }
}
