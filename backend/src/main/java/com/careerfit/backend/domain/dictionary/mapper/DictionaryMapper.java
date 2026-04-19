package com.careerfit.backend.domain.dictionary.mapper;

import com.careerfit.backend.domain.dictionary.dto.CertDictionaryResponse;
import com.careerfit.backend.domain.dictionary.dto.SkillDictionaryResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DictionaryMapper {

    // 자격증 사전 조회 (카테고리 필터 선택)
    // category null이면 어학 제외한 전체, 값 있으면 해당 카테고리만
    List<CertDictionaryResponse> selectCerts(@Param("category") String category);

    // 어학 사전 조회 (cert_dictionary에서 category='어학' 필터)
    List<CertDictionaryResponse> selectLanguages();

    // 기술스택 사전 조회 (카테고리 필터 선택)
    List<SkillDictionaryResponse> selectSkills(@Param("category") String category);
}
