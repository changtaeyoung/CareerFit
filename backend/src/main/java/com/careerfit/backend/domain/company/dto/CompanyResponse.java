package com.careerfit.backend.domain.company.dto;

import com.careerfit.backend.domain.company.entity.Company;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Schema(description = "기업 목록 응답 DTO — 카드형 목록 표시용 핵심 정보만 포함")
@Getter
@Builder
public class CompanyResponse {

    @Schema(description = "기업 ID", example = "1")
    private Long id;

    @Schema(description = "기업명", example = "IBK기업은행")
    private String name;

    @Schema(description = "업종 (은행/보험/카드/증권/공기업/핀테크/기타)", example = "은행")
    private String industry;

    @Schema(description = "기업 유형 (대기업/공기업/금융지주/중견기업/스타트업)", example = "공기업")
    private String companyType;

    @Schema(description = "소재지", example = "서울특별시 중구")
    private String location;

    @Schema(description = "임직원 수", example = "13000")
    private Integer employeeCount;

    // Company 엔티티를 목록용 응답 DTO로 변환 (website·foundedYear·isPublic은 상세에서만 노출)
    public static CompanyResponse from(Company company) {
        return CompanyResponse.builder()
                .id(company.getId())
                .name(company.getName())
                .industry(company.getIndustry())
                .companyType(company.getCompanyType())
                .location(company.getLocation())
                .employeeCount(company.getEmployeeCount())
                .build();
    }
}
