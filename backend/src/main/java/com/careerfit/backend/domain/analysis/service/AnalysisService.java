package com.careerfit.backend.domain.analysis.service;

import com.careerfit.backend.common.exception.CustomException;
import com.careerfit.backend.common.exception.ErrorCode;
import com.careerfit.backend.domain.analysis.dto.AnalysisHistoryResponse;
import com.careerfit.backend.domain.analysis.dto.AnalysisReportResponse;
import com.careerfit.backend.domain.analysis.dto.PrerequisiteCheckResponse;
import com.careerfit.backend.domain.analysis.entity.AnalysisGap;
import com.careerfit.backend.domain.analysis.entity.AnalysisRecommendation;
import com.careerfit.backend.domain.analysis.entity.AnalysisReport;
import com.careerfit.backend.domain.analysis.mapper.AnalysisMapper;
import com.careerfit.backend.domain.analysis.mapper.AnalysisMapper.JobLanguageRow;
import com.careerfit.backend.domain.analysis.mapper.AnalysisMapper.JobRequiredCertRow;
import com.careerfit.backend.domain.analysis.mapper.AnalysisMapper.JobSkillRow;
import com.careerfit.backend.domain.user.entity.UserCertificate;
import com.careerfit.backend.domain.user.entity.UserLanguageScore;
import com.careerfit.backend.domain.user.entity.UserSkill;
import com.careerfit.backend.domain.user.mapper.UserMapper;
import com.careerfit.backend.infrastructure.external.ai.AiAnalysisClient;
import com.careerfit.backend.infrastructure.external.ai.dto.QualitativeRequest;
import com.careerfit.backend.infrastructure.external.ai.dto.QualitativeResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalysisService {

    private final AnalysisMapper analysisMapper;
    private final UserMapper userMapper;
    private final AiAnalysisClient aiAnalysisClient;
    private final ObjectMapper objectMapper;  // List<String> ↔ JSON String 변환용

    // 점수 상수
    private static final int BASE_SCORE          = 10;
    private static final int MAX_CERT_BONUS      = 20;
    private static final int CERT_PER_SCORE      = 5;
    private static final int MAX_LANGUAGE_BONUS  = 10;
    private static final int MAX_SKILL_BONUS     = 10;
    private static final int SKILL_PER_SCORE     = 2;

    // 숙련도 가중치
    private static final double WEIGHT_ADVANCED      = 1.0;
    private static final double WEIGHT_INTERMEDIATE  = 0.5;
    private static final double WEIGHT_BEGINNER      = 0.2;

    // ── 공고 지원 자격 사전 체크 ────────────────────────────────────────────
    // 분석 실행 전 필수 자격증/어학 조건 충족 여부를 미리 확인해서 안내 메시지를 반환
    @Transactional(readOnly = true)
    public PrerequisiteCheckResponse checkPrerequisite(Long userId, Long postingId) {
        log.info("[AnalysisService] 사전 자격 체크 - userId: {}, postingId: {}", userId, postingId);

        Long specVersionId = analysisMapper.selectCurrentSpecVersionId(userId);
        if (specVersionId == null) {
            throw new CustomException(ErrorCode.SPEC_NOT_FOUND);
        }

        List<JobRequiredCertRow> missingCerts =
                analysisMapper.selectMissingRequiredCerts(postingId, specVersionId);
        List<JobLanguageRow> missingLanguages =
                analysisMapper.selectMissingRequiredLanguages(postingId, specVersionId);

        List<PrerequisiteCheckResponse.FailedRequirement> failed = new ArrayList<>();

        // 미보유 필수 자격증 → FailedRequirement 변환
        for (JobRequiredCertRow cert : missingCerts) {
            failed.add(PrerequisiteCheckResponse.FailedRequirement.builder()
                    .type("CERT")
                    .itemName(cert.getCertName())
                    .message(cert.getCertName() + " 자격증이 없습니다. 지원 자격 조건입니다.")
                    .build());
        }

        // 미충족 필수 어학 → FailedRequirement 변환
        for (JobLanguageRow lang : missingLanguages) {
            String reqVal = lang.getMinScore() != null
                    ? lang.getMinScore() + "점" : lang.getMinGrade();
            failed.add(PrerequisiteCheckResponse.FailedRequirement.builder()
                    .type("LANGUAGE")
                    .itemName(lang.getLangType())
                    .requiredValue(reqVal)
                    .userValue("미보유")
                    .message(lang.getLangType() + " " + reqVal + " 이상이 필요합니다.")
                    .build());
        }

        return PrerequisiteCheckResponse.builder()
                .postingId(postingId)
                .eligible(failed.isEmpty())
                .failedRequirements(failed)
                .build();
    }

    // ── 핏 분석 실행 ────────────────────────────────────────────────────────
    // 필수 게이트 → 정량 점수 계산 → 갭/추천 저장 순서로 분석 결과를 생성
    @Transactional
    public AnalysisReportResponse analyze(Long userId, Long jobPostingId, Long specVersionId) {
        log.info("[AnalysisService] 분석 시작 - userId: {}, postingId: {}", userId, jobPostingId);

        // specVersionId 미전달 시 현재 활성 버전 자동 선택
        if (specVersionId == null) {
            specVersionId = analysisMapper.selectCurrentSpecVersionId(userId);
            if (specVersionId == null) {
                throw new CustomException(ErrorCode.SPEC_NOT_FOUND);
            }
        }

        // PENDING 상태로 리포트 선저장 (id 확보)
        AnalysisReport report = AnalysisReport.builder()
                .userId(userId)
                .jobPostingId(jobPostingId)
                .specVersionId(specVersionId)
                .status("PENDING")
                .createdAt(LocalDateTime.now())
                .build();
        analysisMapper.insertReport(report);
        Long reportId = report.getId();

        try {
            // 공고 요건 조회
            List<JobRequiredCertRow> requiredCerts     = analysisMapper.selectRequiredCerts(jobPostingId);
            List<JobSkillRow>        requiredSkills     = analysisMapper.selectRequiredSkills(jobPostingId);
            List<JobLanguageRow>     requiredLanguages  = analysisMapper.selectRequiredLanguages(jobPostingId);

            // 사용자 스펙 조회 (specVersionId 기준)
            List<UserCertificate>   userCerts     = userMapper.selectCertificates(specVersionId);
            List<UserSkill>         userSkills    = userMapper.selectSkills(specVersionId);
            List<UserLanguageScore> userLanguages = userMapper.selectLanguageScores(specVersionId);

            // 사용자 보유 certId Set
            Set<Long> userCertIds = userCerts.stream()
                    .map(UserCertificate::getCertId)
                    .collect(Collectors.toSet());

            List<AnalysisGap> gaps = new ArrayList<>();
            List<AnalysisRecommendation> recommendations = new ArrayList<>();

            // ── 1단계: 필수 게이트 체크 ─────────────────────────────────────
            boolean requiredAllMet = checkRequiredGate(
                    requiredCerts, userCertIds, requiredLanguages, userLanguages, gaps, recommendations, reportId);

            if (!requiredAllMet) {
                // 필수 미충족 → 0점으로 즉시 완료
                saveGapsAndRecommendations(gaps, recommendations);
                analysisMapper.updateReportResult(
                        reportId, false, 0, 0, 0, 0, "COMPLETED", null, null, null);
                log.info("[AnalysisService] 필수 게이트 FAIL - reportId: {}", reportId);
                return buildResponse(reportId, 0, 0, 0, 0, false, gaps, recommendations,
                        List.of(), List.of(), null);
            }

            // ── 2단계: 정량 점수 계산 ────────────────────────────────────────
            int certBonus     = calcCertBonus(requiredCerts, userCertIds, gaps, recommendations, reportId);
            int languageBonus = calcLanguageBonus(requiredLanguages, userLanguages, gaps, recommendations, reportId);
            int skillBonus    = calcSkillBonus(requiredSkills, userSkills, gaps, recommendations, reportId);

            int quantitativeBonus = certBonus + languageBonus + skillBonus;

            // ── 3단계: 정성 가점 (FastAPI 연동) ─────────────────────────
            QualitativeResponse qualitativeResult = callQualitativeAnalysis(userId, jobPostingId);
            int qualitativeBonus = qualitativeResult.getTotalScore();

            // List<String> → JSON String 직렬화 (DB TEXT 저장용)
            String strengthsJson   = toJsonString(qualitativeResult.getStrengths());
            String weaknessesJson  = toJsonString(qualitativeResult.getWeaknesses());
            String aiFeedback      = qualitativeResult.getFeedback();

            int totalScore = BASE_SCORE + quantitativeBonus + qualitativeBonus;

            saveGapsAndRecommendations(gaps, recommendations);
            analysisMapper.updateReportResult(
                    reportId, true, BASE_SCORE, quantitativeBonus, qualitativeBonus,
                    totalScore, "COMPLETED", strengthsJson, weaknessesJson, aiFeedback);

            log.info("[AnalysisService] 분석 완료 - reportId: {}, totalScore: {}", reportId, totalScore);
            return buildResponse(reportId, BASE_SCORE, quantitativeBonus, qualitativeBonus, totalScore,
                    true, gaps, recommendations,
                    qualitativeResult.getStrengths(),
                    qualitativeResult.getWeaknesses(),
                    aiFeedback);

        } catch (Exception e) {
            analysisMapper.updateReportResult(reportId, false, 0, 0, 0, 0, "FAILED", null, null, null);
            log.error("[AnalysisService] 분석 실패 - reportId: {}, error: {}", reportId, e.getMessage());
            throw new CustomException(ErrorCode.ANALYSIS_FAILED);
        }
    }

    // ── 분석 히스토리 목록 조회 ─────────────────────────────────────────────
    // 로그인 사용자의 전체 분석 이력을 최신순으로 반환
    @Transactional(readOnly = true)
    public List<AnalysisHistoryResponse> getHistory(Long userId) {
        log.info("[AnalysisService] 히스토리 조회 - userId: {}", userId);
        return analysisMapper.selectHistoryByUserId(userId);
    }

    // ── 분석 리포트 상세 조회 ───────────────────────────────────────────────
    // reportId로 리포트 + 갭 + 추천 목록을 한번에 조합해서 반환
    @Transactional(readOnly = true)
    public AnalysisReportResponse getReportDetail(Long reportId) {
        log.info("[AnalysisService] 리포트 상세 조회 - reportId: {}", reportId);
        AnalysisReport report = analysisMapper.selectReportById(reportId);
        if (report == null) {
            throw new CustomException(ErrorCode.ANALYSIS_NOT_FOUND);
        }
        List<AnalysisGap> gaps = analysisMapper.selectGapsByReportId(reportId);
        List<AnalysisRecommendation> recommendations = analysisMapper.selectRecommendationsByReportId(reportId);

        return AnalysisReportResponse.builder()
                .reportId(report.getId())
                .requiredAllMet(report.isRequiredAllMet())
                .baseScore(report.getBaseScore())
                .quantitativeBonus(report.getQuantitativeBonus())
                .qualitativeBonus(report.getQualitativeBonus())
                .totalScore(report.getTotalScore())
                .status(report.getStatus())
                .createdAt(report.getCreatedAt())
                .gaps(gaps)
                .recommendations(recommendations)
                // DB JSON String → List<String> 파싱
                .strengths(AnalysisReportResponse.parseJsonArray(report.getStrengths()))
                .weaknesses(AnalysisReportResponse.parseJsonArray(report.getWeaknesses()))
                .aiFeedback(report.getAiFeedback())
                .build();
    }

    // ── 분석 리포트 삭제 ────────────────────────────────────────────────────
    // cascade로 gap, recommendation도 함께 삭제
    @Transactional
    public void deleteReport(Long reportId) {
        log.info("[AnalysisService] 리포트 삭제 - reportId: {}", reportId);
        if (analysisMapper.selectReportById(reportId) == null) {
            throw new CustomException(ErrorCode.ANALYSIS_NOT_FOUND);
        }
        analysisMapper.deleteReport(reportId);
    }

    // ────────────────────────────────────────────────────────────────────────
    // 내부 메서드
    // ────────────────────────────────────────────────────────────────────────

    // 필수 자격증 + 어학 조건 전부 충족하는지 확인. 미충족 항목은 gap으로 기록
    private boolean checkRequiredGate(
            List<JobRequiredCertRow> requiredCerts,
            java.util.Set<Long> userCertIds,
            List<JobLanguageRow> requiredLanguages,
            List<UserLanguageScore> userLanguages,
            List<AnalysisGap> gaps,
            List<AnalysisRecommendation> recommendations,
            Long reportId) {

        boolean allMet = true;

        // 필수 자격증 체크
        for (JobRequiredCertRow req : requiredCerts) {
            if (!"REQUIRED".equals(req.getRequirementType())) continue;

            boolean meet = userCertIds.contains(req.getCertId());
            gaps.add(AnalysisGap.builder()
                    .reportId(reportId)
                    .gapType("CERT")
                    .requirementType("REQUIRED")
                    .itemName(req.getCertName())
                    .status(meet ? "MEET" : "FAIL")
                    .bonusScore(0)
                    .description(meet ? null : req.getCertName() + " 자격증이 없습니다")
                    .build());

            if (!meet) {
                allMet = false;
                recommendations.add(AnalysisRecommendation.builder()
                        .reportId(reportId)
                        .priority("P1")
                        .category("CERT")
                        .content(req.getCertName() + " 취득 (필수 조건)")
                        .expectedScoreGain(BASE_SCORE)
                        .sortOrder(0)
                        .build());
            }
        }

        // 필수 어학 체크
        Map<String, UserLanguageScore> userLangMap = userLanguages.stream()
                .collect(Collectors.toMap(UserLanguageScore::getLangType, l -> l));

        for (JobLanguageRow req : requiredLanguages) {
            UserLanguageScore userLang = userLangMap.get(req.getLangType());
            boolean meet = checkLanguageMeet(userLang, req);

            String userVal = userLang != null
                    ? (userLang.getScore() != null ? String.valueOf(userLang.getScore()) : userLang.getGrade())
                    : null;
            String reqVal = req.getMinScore() != null
                    ? String.valueOf(req.getMinScore()) : req.getMinGrade();

            gaps.add(AnalysisGap.builder()
                    .reportId(reportId)
                    .gapType("LANGUAGE")
                    .requirementType("REQUIRED")
                    .itemName(req.getLangType())
                    .status(meet ? "MEET" : "FAIL")
                    .requiredValue(reqVal)
                    .userValue(userVal)
                    .bonusScore(0)
                    .description(meet ? null : req.getLangType() + " 기준 점수/등급 미달입니다")
                    .build());

            if (!meet) {
                allMet = false;
                recommendations.add(AnalysisRecommendation.builder()
                        .reportId(reportId)
                        .priority("P1")
                        .category("LANGUAGE")
                        .content(req.getLangType() + " " + reqVal + " 이상 취득 (필수 조건)")
                        .expectedScoreGain(BASE_SCORE)
                        .sortOrder(0)
                        .build());
            }
        }

        return allMet;
    }

    // 우대 자격증 충족 개수에 따라 가점을 계산하고 gap 기록 (최대 20점)
    private int calcCertBonus(
            List<JobRequiredCertRow> requiredCerts,
            java.util.Set<Long> userCertIds,
            List<AnalysisGap> gaps,
            List<AnalysisRecommendation> recommendations,
            Long reportId) {

        int bonus = 0;
        int sortOrder = 10;

        for (JobRequiredCertRow req : requiredCerts) {
            if (!"PREFERRED".equals(req.getRequirementType())) continue;

            boolean meet = userCertIds.contains(req.getCertId());
            int gained = meet ? Math.min(CERT_PER_SCORE, MAX_CERT_BONUS - bonus) : 0;
            bonus += gained;

            gaps.add(AnalysisGap.builder()
                    .reportId(reportId)
                    .gapType("CERT")
                    .requirementType("PREFERRED")
                    .itemName(req.getCertName())
                    .status(meet ? "MEET" : "FAIL")
                    .bonusScore(gained)
                    .description(meet ? null : req.getCertName() + " 취득 시 +" + CERT_PER_SCORE + "점")
                    .build());

            if (!meet) {
                recommendations.add(AnalysisRecommendation.builder()
                        .reportId(reportId)
                        .priority("P2")
                        .category("CERT")
                        .content(req.getCertName() + " 취득 (우대 +5점)")
                        .expectedScoreGain(CERT_PER_SCORE)
                        .sortOrder(sortOrder++)
                        .build());
            }
        }
        return Math.min(bonus, MAX_CERT_BONUS);
    }

    // 우대 어학 기준 충족 여부에 따라 가점을 계산하고 gap을 기록 (최대 10점)
    private int calcLanguageBonus(
            List<JobLanguageRow> requiredLanguages,
            List<UserLanguageScore> userLanguages,
            List<AnalysisGap> gaps,
            List<AnalysisRecommendation> recommendations,
            Long reportId) {

        // job_language_requirement는 REQUIRED/PREFERRED 구분이 없고 기준값만 있음
        // → 기준 충족 시 MAX_LANGUAGE_BONUS 부여, 미충족 시 0
        if (requiredLanguages.isEmpty()) return 0;

        Map<String, UserLanguageScore> userLangMap = userLanguages.stream()
                .collect(Collectors.toMap(UserLanguageScore::getLangType, l -> l));

        boolean anyMeet = false;
        for (JobLanguageRow req : requiredLanguages) {
            UserLanguageScore userLang = userLangMap.get(req.getLangType());
            if (checkLanguageMeet(userLang, req)) {
                anyMeet = true;
                break;
            }
        }

        if (!anyMeet) {
            recommendations.add(AnalysisRecommendation.builder()
                    .reportId(reportId)
                    .priority("P2")
                    .category("LANGUAGE")
                    .content("어학 기준 점수/등급 충족 시 +" + MAX_LANGUAGE_BONUS + "점")
                    .expectedScoreGain(MAX_LANGUAGE_BONUS)
                    .sortOrder(20)
                    .build());
        }
        return anyMeet ? MAX_LANGUAGE_BONUS : 0;
    }

    // 우대 기술스택 충족 여부와 숙련도 가중치에 따라 가점을 계산하고 gap을 기록 (최대 10점)
    private int calcSkillBonus(
            List<JobSkillRow> requiredSkills,
            List<UserSkill> userSkills,
            List<AnalysisGap> gaps,
            List<AnalysisRecommendation> recommendations,
            Long reportId) {

        Map<Long, UserSkill> userSkillMap = userSkills.stream()
                .collect(Collectors.toMap(UserSkill::getSkillId, s -> s));

        double rawBonus = 0;
        int sortOrder = 30;

        for (JobSkillRow req : requiredSkills) {
            if (!"PREFERRED".equals(req.getRequirementType())) continue;

            UserSkill userSkill = userSkillMap.get(req.getSkillId());
            double weight = 0;
            String userVal = null;

            if (userSkill != null) {
                weight = proficiencyWeight(userSkill.getProficiency());
                userVal = userSkill.getProficiency();
            }

            double gained = SKILL_PER_SCORE * weight;
            rawBonus += gained;

            boolean meet = weight > 0;
            gaps.add(AnalysisGap.builder()
                    .reportId(reportId)
                    .gapType("SKILL")
                    .requirementType("PREFERRED")
                    .itemName(req.getSkillName())
                    .status(meet ? "MEET" : "FAIL")
                    .userValue(userVal)
                    .bonusScore((int) gained)
                    .description(meet ? null : req.getSkillName() + " 학습 시 최대 +" + SKILL_PER_SCORE + "점")
                    .build());

            if (!meet) {
                recommendations.add(AnalysisRecommendation.builder()
                        .reportId(reportId)
                        .priority("P3")
                        .category("SKILL")
                        .content(req.getSkillName() + " 학습 (우대 최대 +" + SKILL_PER_SCORE + "점)")
                        .expectedScoreGain(SKILL_PER_SCORE)
                        .sortOrder(sortOrder++)
                        .build());
            }
        }
        return Math.min((int) rawBonus, MAX_SKILL_BONUS);
    }

    // 사용자 어학 점수/등급이 공고 요건을 충족하는지 판단
    private boolean checkLanguageMeet(UserLanguageScore userLang, JobLanguageRow req) {
        if (userLang == null) return false;
        if (req.getMinScore() != null && userLang.getScore() != null) {
            return userLang.getScore() >= req.getMinScore();
        }
        if (req.getMinGrade() != null && userLang.getGrade() != null) {
            return gradeRank(userLang.getGrade()) >= gradeRank(req.getMinGrade());
        }
        return false;
    }

    // OPIc/TOEIC-S 등급을 숫자로 변환해 비교 (숫자가 클수록 높은 등급)
    private int gradeRank(String grade) {
        return switch (grade.toUpperCase()) {
            case "AL"  -> 6;
            case "IH"  -> 5;
            case "IM3" -> 4;
            case "IM2" -> 3;
            case "IM1" -> 2;
            case "IL"  -> 1;
            default    -> 0;
        };
    }

    // 숙련도 문자열을 가중치 double로 변환
    private double proficiencyWeight(String proficiency) {
        if (proficiency == null) return 0;
        return switch (proficiency.toUpperCase()) {
            case "ADVANCED"     -> WEIGHT_ADVANCED;
            case "INTERMEDIATE" -> WEIGHT_INTERMEDIATE;
            case "BEGINNER"     -> WEIGHT_BEGINNER;
            default             -> 0;
        };
    }

    // gap 리스트와 recommendation 리스트를 DB에 일괄 저장
    private void saveGapsAndRecommendations(
            List<AnalysisGap> gaps,
            List<AnalysisRecommendation> recommendations) {
        gaps.forEach(analysisMapper::insertGap);
        recommendations.forEach(analysisMapper::insertRecommendation);
    }

    // ── 정성 분석 ────────────────────────────────────────────────────────────
    // 자소서가 있으면 FastAPI에 정성 분석 요청, 없으면 빈 응답(0점) 반환
    private QualitativeResponse callQualitativeAnalysis(Long userId, Long jobPostingId) {
        try {
            List<AnalysisMapper.CoverLetterRow> coverLetters =
                    analysisMapper.selectCoverLettersByPostingAndUser(jobPostingId, userId);

            if (coverLetters.isEmpty()) {
                log.info("[AnalysisService] 자소서 없음 — 정성 가점 0점");
                return QualitativeResponse.empty("자소서가 작성되지 않아 정성 평가를 진행할 수 없습니다.");
            }

            AnalysisMapper.PostingContextRow context = analysisMapper.selectPostingContext(jobPostingId);

            List<QualitativeRequest.CoverLetterItem> items = coverLetters.stream()
                    .map(cl -> QualitativeRequest.CoverLetterItem.builder()
                            .question(cl.getQuestion())
                            .content(cl.getContent())
                            .build())
                    .toList();

            QualitativeRequest request = QualitativeRequest.builder()
                    .talent_image(context != null ? context.getTalentImage() : null)
                    .job_description(context != null ? context.getRawText() : null)
                    .cover_letters(items)
                    .build();

            QualitativeResponse response = aiAnalysisClient.analyzeQualitative(request);
            log.info("[AnalysisService] 정성 분석 완료 — 점수: {}점", response.getTotalScore());
            return response;

        } catch (Exception e) {
            log.warn("[AnalysisService] 정성 분석 중 예외 발생 — 0점 처리: {}", e.getMessage());
            return QualitativeResponse.empty("정성 분석 중 오류가 발생했습니다.");
        }
    }

    // List<String>을 JSON String으로 직렬화 (DB TEXT 저장용)
    private String toJsonString(List<String> list) {
        if (list == null || list.isEmpty()) return null;
        try {
            return objectMapper.writeValueAsString(list);
        } catch (Exception e) {
            log.warn("[AnalysisService] JSON 직렬화 실패: {}", e.getMessage());
            return null;
        }
    }

    // AnalysisReportResponse 조립 헬퍼 — AI 피드백 포함
    private AnalysisReportResponse buildResponse(
            Long reportId, int baseScore, int quantitativeBonus, int qualitativeBonus,
            int totalScore, boolean requiredAllMet,
            List<AnalysisGap> gaps, List<AnalysisRecommendation> recommendations,
            List<String> strengths, List<String> weaknesses, String aiFeedback) {
        return AnalysisReportResponse.builder()
                .reportId(reportId)
                .requiredAllMet(requiredAllMet)
                .baseScore(baseScore)
                .quantitativeBonus(quantitativeBonus)
                .qualitativeBonus(qualitativeBonus)
                .totalScore(totalScore)
                .status("COMPLETED")
                .createdAt(LocalDateTime.now())
                .gaps(gaps)
                .recommendations(recommendations)
                .strengths(strengths != null ? strengths : List.of())
                .weaknesses(weaknesses != null ? weaknesses : List.of())
                .aiFeedback(aiFeedback)
                .build();
    }
}
