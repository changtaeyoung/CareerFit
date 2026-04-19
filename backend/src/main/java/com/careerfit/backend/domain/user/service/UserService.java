package com.careerfit.backend.domain.user.service;

import com.careerfit.backend.common.exception.CustomException;
import com.careerfit.backend.common.exception.ErrorCode;
import com.careerfit.backend.domain.user.dto.*;
import com.careerfit.backend.domain.user.entity.*;
import com.careerfit.backend.domain.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    // 1단계 — 학력 + 희망직무 + 기술스택
    @Transactional
    public SpecBasicResponse registerBasicSpec(Long userId, SpecBasicRequest request) {
        log.info("[UserService] 스펙 기본 정보 등록 - userId: {}", userId);

        // 새 버전 번호 계산
        int nextVersionNo = userMapper.selectMaxVersionNo(userId) + 1;

        // 이전 버전 비활성화
        userMapper.updateVersionInactive(userId);

        // 스펙 버전 INSERT
        UserSpecVersion specVersion = UserSpecVersion.builder()
                .userId(userId)
                .versionNo(nextVersionNo)
                .education(request.getEducation())
                .university(request.getUniversity())
                .gpa(request.getGpa())
                .isCurrent(true)
                .createdAt(LocalDateTime.now())
                .build();

        userMapper.insertSpecVersion(specVersion);
        Long versionId = specVersion.getId(); // useGeneratedKeys 로 자동 주입

        // 희망직무 복수 INSERT
        if (request.getWantedJobs() != null) {
            for (String jobType : request.getWantedJobs()) {
                userMapper.insertWantedJob(UserWantedJob.builder()
                        .specVersionId(versionId)
                        .jobType(jobType)
                        .build());
            }
        }

        // 기술스택 복수 INSERT
        if (request.getSkills() != null) {
            for (SpecBasicRequest.SkillItem skill : request.getSkills()) {

                if (skill.getProficiency() != null) {
                    switch (skill.getProficiency()) {
                        case "초급" -> skill.setProficiency("하");
                        case "중급" -> skill.setProficiency("중");
                        case "상급" -> skill.setProficiency("상");
                    }

                    userMapper.insertSkill(UserSkill.builder()
                            .specVersionId(versionId)
                            .skillId(skill.getSkillId())
                            .proficiency(skill.getProficiency())
                            .build());
                }
            }
        }

        log.info("[UserService] 스펙 기본 정보 등록 완료 - versionId: {}, versionNo: {}", versionId, nextVersionNo);

        return SpecBasicResponse.builder()
                .versionId(versionId)
                .versionNo(nextVersionNo)
                .build();
    }

    // 2단계 — 자격증 + 어학
    @Transactional
    public void registerQualification(Long specVersionId, SpecQualificationRequest request) {
        log.info("[UserService] 자격증/어학 등록 - specVersionId: {}", specVersionId);

        // 자격증 복수 INSERT
        if (request.getCertificates() != null) {
            for (SpecQualificationRequest.CertItem cert : request.getCertificates()) {
                // 중복 선체크 — DB 유니크 제약보다 먼저 걸러서 명확한 에러 전달
                int dup = userMapper.countCertificateDuplicate(
                        specVersionId, cert.getCertId(), cert.getStatus(), cert.getAcquiredAt());
                if (dup > 0) {
                    throw new CustomException(ErrorCode.DUPLICATE_CERTIFICATE);
                }

                userMapper.insertCertificate(UserCertificate.builder()
                        .specVersionId(specVersionId)
                        .certId(cert.getCertId())
                        .status(cert.getStatus())
                        .score(cert.getScore())
                        .acquiredAt(cert.getAcquiredAt())
                        .build());
            }
        }

        // 어학 복수 INSERT
        if (request.getLanguages() != null) {
            for (SpecQualificationRequest.LanguageItem lang : request.getLanguages()) {

                validateLanguageItem(lang);

                // 중복 선체크 — 같은 스펙 버전에 같은 어학 종류가 이미 있으면 거부
                int dup = userMapper.countLanguageDuplicate(specVersionId, lang.getLangType());
                if (dup > 0) {
                    throw new CustomException(ErrorCode.DUPLICATE_LANGUAGE);
                }

                userMapper.insertLanguageScore(UserLanguageScore.builder()
                        .specVersionId(specVersionId)
                        .langType(lang.getLangType())
                        .score(lang.getScore())
                        .grade(lang.getGrade())
                        .acquiredAt(lang.getAcquiredAt())
                        .build());
            }
        }

        log.info("[UserService] 자격증/어학 등록 완료 - specVersionId: {}", specVersionId);
    }

    // 3단계 — 경력 + 프로젝트 + 수상
    @Transactional
    public void registerExperience(Long specVersionId, SpecExperienceRequest request) {
        log.info("[UserService] 경력/프로젝트/수상 등록 - specVersionId: {}", specVersionId);

        // 경력 복수 INSERT
        if (request.getInterns() != null) {
            for (SpecExperienceRequest.InternItem intern : request.getInterns()) {
                userMapper.insertIntern(UserIntern.builder()
                        .specVersionId(specVersionId)
                        .companyName(intern.getCompanyName())
                        .employmentType(intern.getEmploymentType())
                        .role(intern.getRole())
                        .description(intern.getDescription())
                        .startedAt(intern.getStartedAt())
                        .endedAt(intern.getEndedAt())
                        .build());
            }
        }

        // 프로젝트 복수 INSERT
        if (request.getProjects() != null) {
            for (SpecExperienceRequest.ProjectItem project : request.getProjects()) {
                userMapper.insertProject(UserProject.builder()
                        .specVersionId(specVersionId)
                        .title(project.getTitle())
                        .description(project.getDescription())
                        .githubUrl(project.getGithubUrl())
                        .startedAt(project.getStartedAt())
                        .endedAt(project.getEndedAt())
                        .status(project.getStatus())
                        .build());
            }
        }

        // 수상 복수 INSERT
        if (request.getAwards() != null) {
            for (SpecExperienceRequest.AwardItem award : request.getAwards()) {
                userMapper.insertAward(UserAward.builder()
                        .specVersionId(specVersionId)
                        .title(award.getTitle())
                        .institution(award.getInstitution())
                        .grade(award.getGrade())
                        .awardedAt(award.getAwardedAt())
                        .build());
            }
        }

        log.info("[UserService] 경력/프로젝트/수상 등록 완료 - specVersionId: {}", specVersionId);
    }

    // 스펙 버전 Soft Delete
    // 존재하지 않거나 이미 삭제된 스펙이면 SPEC_VERSION_NOT_FOUND 예외
    @Transactional
    public void deleteSpecVersion(Long specVersionId) {
        log.info("[UserService] 스펙 버전 삭제 - specVersionId: {}", specVersionId);

        // selectSpecById는 deleted_at IS NULL 조건 포함 → 이미 삭제된 것도 null 반환
        UserSpecVersion spec = userMapper.selectSpecById(specVersionId);
        if (spec == null) {
            throw new CustomException(ErrorCode.SPEC_VERSION_NOT_FOUND);
        }

        userMapper.deleteSpecVersion(specVersionId);
        log.info("[UserService] 스펙 버전 Soft Delete 완료 - specVersionId: {}", specVersionId);
    }

    // 비밀번호 변경
    @Transactional
    public void changePassword(Long userId, PasswordChangeRequest request) {
        log.info("[UserService] 비밀번호 변경 - userId: {}", userId);

        // 현재 비밀번호 확인
        String savedPassword = userMapper.selectPasswordById(userId);
        if (!passwordEncoder.matches(request.getCurrentPassword(), savedPassword)) {
            throw new CustomException(ErrorCode.INVALID_CREDENTIALS);
        }

        // 새 비밀번호 일치 확인
        if (!request.getNewPassword().equals(request.getNewPasswordConfirm())) {
            throw new CustomException(ErrorCode.PASSWORD_MISMATCH);
        }

        // 암호화 후 저장
        String encodedPassword = passwordEncoder.encode(request.getNewPassword());
        userMapper.updatePassword(userId, encodedPassword);

        log.info("[UserService] 비밀번호 변경 완료 - userId: {}", userId);
    }

    // 이름 수정
    @Transactional
    public void updateName(Long userId, String name) {
        log.info("[UserService] 이름 수정 - userId: {}", userId);
        userMapper.updateName(userId, name);
    }

    // 회원 탈퇴
    @Transactional
    public void deleteUser(Long userId) {
        log.info("[UserService] 회원 탈퇴 - userId: {}", userId);
        userMapper.deleteUser(userId);
    }

    // UserService에서 어학 INSERT 시 검증
    private void validateLanguageItem(SpecQualificationRequest.LanguageItem lang) {
        switch (lang.getLangType()) {
            case "TOEIC", "TOEFL", "TEPS" -> {
                if (lang.getScore() == null)
                    throw new CustomException(ErrorCode.INVALID_INPUT);
            }
            case "OPIC" -> {
                if (lang.getGrade() == null)
                    throw new CustomException(ErrorCode.INVALID_INPUT);
            }
            case "TOEIC-S" -> {
                // 점수랑 등급 둘 다 허용 — 제약 없음
            }
        }
    }

    // 전체 버전 목록
    public List<UserSpecVersion> getSpecHistory(Long userId) {
        log.info("[UserService] 스펙 히스토리 조회 - userId: {}", userId);
        return userMapper.selectAllVersions(userId);
    }

    // 특정 버전 상세
    public SpecDetailResponse getSpecDetail(Long specVersionId) {
        log.info("[UserService] 스펙 상세 조회 - specVersionId: {}", specVersionId);

        UserSpecVersion spec = userMapper.selectSpecById(specVersionId);
        if (spec == null) {
            throw new CustomException(ErrorCode.SPEC_NOT_FOUND);
        }

        return SpecDetailResponse.builder()
                .versionId(spec.getId())
                .versionNo(spec.getVersionNo())
                .education(spec.getEducation())
                .university(spec.getUniversity())
                .gpa(spec.getGpa())
                .createdAt(spec.getCreatedAt())
                .wantedJobs(userMapper.selectWantedJobs(spec.getId()))
                .skills(userMapper.selectSkills(spec.getId()))
                .certificates(userMapper.selectCertificates(spec.getId()))
                .languages(userMapper.selectLanguageScores(spec.getId()))
                .interns(userMapper.selectInterns(spec.getId()))
                .projects(userMapper.selectProjects(spec.getId()))
                .awards(userMapper.selectAwards(spec.getId()))
                .build();
    }

    // 스펙 완성도 조회
    // 필수 3항목(기본정보·희망직무·기술스택) 각 20점 + 선택 5항목(자격증·어학·경력·프로젝트·수상) 각 8점 = 최대 100점
    @Transactional(readOnly = true)
    public SpecCompletionResponse getSpecCompletion(Long specVersionId) {
        log.info("[UserService] 스펙 완성도 조회 - specVersionId: {}", specVersionId);

        UserSpecVersion spec = userMapper.selectSpecById(specVersionId);
        if (spec == null) {
            throw new CustomException(ErrorCode.SPEC_VERSION_NOT_FOUND);
        }

        // 필수 항목 — 기본정보는 education 또는 university 중 하나라도 있으면 등록된 것으로 판단
        boolean hasBasicInfo  = spec.getEducation() != null || spec.getUniversity() != null;
        boolean hasWantedJob  = userMapper.countWantedJobs(specVersionId) > 0;
        boolean hasSkill      = userMapper.countSkills(specVersionId) > 0;

        // 선택 항목 건수
        int certCount     = userMapper.countCertificates(specVersionId);
        int langCount     = userMapper.countLanguageScores(specVersionId);
        int internCount   = userMapper.countInterns(specVersionId);
        int projectCount  = userMapper.countProjects(specVersionId);
        int awardCount    = userMapper.countAwards(specVersionId);

        // 점수 계산
        int score = 0;
        if (hasBasicInfo) score += 20;
        if (hasWantedJob) score += 20;
        if (hasSkill)     score += 20;
        if (certCount   > 0) score += 8;
        if (langCount   > 0) score += 8;
        if (internCount > 0) score += 8;
        if (projectCount > 0) score += 8;
        if (awardCount  > 0) score += 8;

        log.info("[UserService] 스펙 완성도 계산 완료 - specVersionId: {}, score: {}", specVersionId, score);

        return SpecCompletionResponse.builder()
                .versionId(specVersionId)
                .totalScore(score)
                .hasBasicInfo(hasBasicInfo)
                .hasWantedJob(hasWantedJob)
                .hasSkill(hasSkill)
                .certificateCount(certCount)
                .languageCount(langCount)
                .internCount(internCount)
                .projectCount(projectCount)
                .awardCount(awardCount)
                .build();
    }
}