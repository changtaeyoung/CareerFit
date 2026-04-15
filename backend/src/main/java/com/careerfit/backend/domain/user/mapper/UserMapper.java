package com.careerfit.backend.domain.user.mapper;

import com.careerfit.backend.domain.user.entity.*;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserMapper {

    // 스펙 버전
    // 현재 활성 버전 번호 조회 (새 버전 생성 시 +1 하기 위해)
    Integer selectMaxVersionNo(@Param("userId") Long userId);

    // 이전 버전 비활성화
    void updateVersionInactive(@Param("userId") Long userId);

    // 스펙 기본 정보 INSERT → useGeneratedKeys로 id 반환
    void insertSpecVersion(UserSpecVersion specVersion);

    // 희망 직무
    void insertWantedJob(UserWantedJob wantedJob);

    // 기술스택
    void insertSkill(UserSkill skill);

    // 자격증
    void insertCertificate(UserCertificate certificate);

    // 어학
    void insertLanguageScore(UserLanguageScore languageScore);

    // 경력
    void insertIntern(UserIntern intern);

    // 프로젝트
    void insertProject(UserProject project);

    // 수상 내역
    void insertAward(UserAward award);

    // 스펙 버전 삭제
    void deleteSpecVersion(@Param("specVersionId") Long specVersionId);

    // 조회
    UserSpecVersion selectCurrentSpec(@Param("userId") Long userId);

    // 비밀번호 조회 (현재 비번 확인용)
    String selectPasswordById(@Param("userId") Long userId);

    // 비밀번호 변경
    void updatePassword(@Param("userId") Long userId,
                        @Param("password") String password);

    // 이름 수정
    void updateName(@Param("userId") Long userId,
                    @Param("name") String name);

    // 회원 탈퇴
    void deleteUser(@Param("userId") Long userId);

    List<UserWantedJob>      selectWantedJobs(@Param("specVersionId") Long specVersionId);
    List<UserSkill>          selectSkills(@Param("specVersionId") Long specVersionId);
    List<UserCertificate>    selectCertificates(@Param("specVersionId") Long specVersionId);
    List<UserLanguageScore>  selectLanguageScores(@Param("specVersionId") Long specVersionId);
    List<UserIntern>         selectInterns(@Param("specVersionId") Long specVersionId);
    List<UserProject>        selectProjects(@Param("specVersionId") Long specVersionId);
    List<UserAward>          selectAwards(@Param("specVersionId") Long specVersionId);

    // 전체 버전 목록 조회
    List<UserSpecVersion> selectAllVersions(@Param("userId") Long userId);

    // 특정 버전 상세 조회
    UserSpecVersion selectSpecById(@Param("specVersionId") Long specVersionId);

}