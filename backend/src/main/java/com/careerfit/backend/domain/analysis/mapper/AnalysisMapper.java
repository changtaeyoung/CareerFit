package com.careerfit.backend.domain.analysis.mapper;

import com.careerfit.backend.domain.analysis.dto.AnalysisHistoryResponse;
import com.careerfit.backend.domain.analysis.entity.AnalysisGap;
import com.careerfit.backend.domain.analysis.entity.AnalysisReport;
import com.careerfit.backend.domain.analysis.entity.AnalysisRecommendation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AnalysisMapper {

    // ── analysis_report ────────────────────────────────────
    // 분석 리포트 저장 (useGeneratedKeys로 id 자동 주입)
    void insertReport(AnalysisReport report);

    // 분석 완료 후 status + 점수 업데이트
    void updateReportResult(@Param("reportId") Long reportId,
                            @Param("requiredAllMet") boolean requiredAllMet,
                            @Param("baseScore") int baseScore,
                            @Param("quantitativeBonus") int quantitativeBonus,
                            @Param("qualitativeBonus") int qualitativeBonus,
                            @Param("totalScore") int totalScore,
                            @Param("status") String status);

    // 히스토리 목록 조회 (기업명, 공고제목 JOIN)
    List<AnalysisHistoryResponse> selectHistoryByUserId(@Param("userId") Long userId);

    // 리포트 단건 조회
    AnalysisReport selectReportById(@Param("reportId") Long reportId);

    // 리포트 삭제
    void deleteReport(@Param("reportId") Long reportId);

    // ── analysis_gap ───────────────────────────────────────
    // 갭 분석 항목 단건 저장
    void insertGap(AnalysisGap gap);

    // 리포트에 속한 갭 전체 조회
    List<AnalysisGap> selectGapsByReportId(@Param("reportId") Long reportId);

    // ── analysis_recommendation ────────────────────────────
    // 액션 플랜 단건 저장
    void insertRecommendation(AnalysisRecommendation recommendation);

    // 리포트에 속한 액션 플랜 전체 조회 (sort_order 오름차순)
    List<AnalysisRecommendation> selectRecommendationsByReportId(@Param("reportId") Long reportId);

    // ── 분석용 조회 쿼리 ───────────────────────────────────
    // 공고의 자격증 요건 조회 (REQUIRED/PREFERRED 포함)
    List<JobRequiredCertRow> selectRequiredCerts(@Param("postingId") Long postingId);

    // 공고의 기술스택 요건 조회
    List<JobSkillRow> selectRequiredSkills(@Param("postingId") Long postingId);

    // 공고의 어학 요건 조회
    List<JobLanguageRow> selectRequiredLanguages(@Param("postingId") Long postingId);

    // 사용자 현재 활성 스펙 버전 ID 조회
    Long selectCurrentSpecVersionId(@Param("userId") Long userId);

    // 사전 체크용 — 공고 필수 자격증 중 사용자가 미보유한 항목 조회
    List<JobRequiredCertRow> selectMissingRequiredCerts(@Param("postingId") Long postingId,
                                                        @Param("specVersionId") Long specVersionId);

    // 사전 체크용 — 공고 필수 어학 요건 중 사용자가 미충족한 항목 조회
    List<JobLanguageRow> selectMissingRequiredLanguages(@Param("postingId") Long postingId,
                                                        @Param("specVersionId") Long specVersionId);

    // ── 내부 결과 row 클래스 ───────────────────────────────
    // job_required_cert + cert_dictionary JOIN 결과
    @lombok.Getter @lombok.Builder
    class JobRequiredCertRow {
        private Long certId;
        private String certName;
        private String requirementType;  // REQUIRED / PREFERRED
    }

    // job_skill + skill_dictionary JOIN 결과
    @lombok.Getter @lombok.Builder
    class JobSkillRow {
        private Long skillId;
        private String skillName;
        private String requirementType;  // REQUIRED / PREFERRED
    }

    // job_language_requirement 결과
    @lombok.Getter @lombok.Builder
    class JobLanguageRow {
        private String langType;
        private Integer minScore;
        private String minGrade;
    }
}
