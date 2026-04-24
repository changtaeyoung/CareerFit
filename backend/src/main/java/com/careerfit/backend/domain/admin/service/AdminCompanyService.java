package com.careerfit.backend.domain.admin.service;

import com.careerfit.backend.common.exception.CustomException;
import com.careerfit.backend.common.exception.ErrorCode;
import com.careerfit.backend.domain.admin.dto.AdminCompanyUpdateRequest;
import com.careerfit.backend.domain.admin.dto.AdminQuestionRequest;
import com.careerfit.backend.domain.admin.dto.AdminSalaryRequest;
import com.careerfit.backend.domain.company.entity.Company;
import com.careerfit.backend.domain.company.entity.CompanySalary;
import com.careerfit.backend.domain.company.mapper.CompanyMapper;
import com.careerfit.backend.domain.company.mapper.CompanySalaryMapper;
import com.careerfit.backend.domain.coverletter.dto.JobPostingQuestionResponse;
import com.careerfit.backend.domain.coverletter.entity.JobPostingQuestion;
import com.careerfit.backend.domain.coverletter.mapper.JobPostingQuestionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminCompanyService {

    private final CompanyMapper companyMapper;
    private final CompanySalaryMapper companySalaryMapper;
    private final JobPostingQuestionMapper questionMapper;

    /**
     * 기업 정보 수동 업데이트 (PATCH 방식).
     *
     * ── PATCH 방식인 이유 ────────────────────────────────────────────────
     * PUT은 전체 필드를 교체하는데, 인재상만 업데이트하려고 다른 필드까지
     * 전부 보내야 하는 불편함이 있다. PATCH는 보낸 필드만 업데이트하고
     * null은 기존 값을 유지하므로 부분 업데이트에 적합하다.
     * SQL에서 COALESCE(#{newValue}, existing_value) 패턴으로 구현.
     */
    @Transactional
    public void updateCompanyInfo(Long companyId, AdminCompanyUpdateRequest request) {
        // 존재 여부 확인
        Company company = companyMapper.selectCompanyById(companyId);
        if (company == null) {
            throw new CustomException(ErrorCode.COMPANY_NOT_FOUND);
        }

        companyMapper.updateCompanyInfo(
                companyId,
                request.getVision(),
                request.getTalentImage(),
                request.getBusinessOverview(),
                request.getEmployeeCount(),
                request.getFoundedYear(),
                request.getWebsite(),
                request.getLocation()
        );

        log.info("[AdminCompanyService] 기업 정보 업데이트 완료 — companyId: {}, name: {}",
                companyId, company.getName());
    }

    /**
     * 연봉 수동 입력.
     *
     * ── source=MANUAL 의미 ───────────────────────────────────────────────
     * company_salary 테이블의 source 컬럼으로 데이터 출처를 추적한다.
     * DART_API, ALIO_API, ALIO_CRAWL은 자동 수집, MANUAL은 관리자 직접 입력.
     * 프론트에서 출처 표시 시 "관리자 입력"으로 표시됨.
     */
    @Transactional
    public void addSalary(Long companyId, AdminSalaryRequest request) {
        Company company = companyMapper.selectCompanyById(companyId);
        if (company == null) {
            throw new CustomException(ErrorCode.COMPANY_NOT_FOUND);
        }

        CompanySalary salary = CompanySalary.builder()
                .companyId(companyId)
                .salaryType(request.getSalaryType())
                .amount(request.getAmount())
                .year(request.getYear())
                .source("MANUAL")
                .sourceUrl(request.getSourceUrl())
                .collectedAt(LocalDateTime.now())
                .build();

        companySalaryMapper.upsertSalary(salary);

        log.info("[AdminCompanyService] 연봉 입력 완료 — {} {} {}년 {}만원 (MANUAL)",
                company.getName(), request.getSalaryType(), request.getYear(), request.getAmount());
    }

    // ── 자소서 문항 ───────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<JobPostingQuestionResponse> getQuestions(Long postingId) {
        return questionMapper.selectByPostingId(postingId);
    }

    /**
     * 공고 자소서 문항 등록.
     * 문항은 관리자가 직접 입력. 크롤링 불가 항목.
     */
    @Transactional
    public void addQuestion(Long postingId, AdminQuestionRequest request) {
        JobPostingQuestion question = JobPostingQuestion.builder()
                .postingId(postingId)
                .question(request.getQuestion())
                .sortOrder(request.getSortOrder())
                .build();
        questionMapper.insert(question);
        log.info("[AdminCompanyService] 문항 등록 완료 — postingId: {}, 순서: {}", postingId, request.getSortOrder());
    }

    @Transactional
    public void deleteQuestion(Long questionId) {
        questionMapper.delete(questionId);
        log.info("[AdminCompanyService] 문항 삭제 완료 — questionId: {}", questionId);
    }
}
