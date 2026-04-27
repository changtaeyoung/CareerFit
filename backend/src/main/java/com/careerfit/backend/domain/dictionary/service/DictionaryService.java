package com.careerfit.backend.domain.dictionary.service;

import com.careerfit.backend.domain.dictionary.dto.CertDictionaryResponse;
import com.careerfit.backend.domain.dictionary.dto.SkillDictionaryResponse;
import com.careerfit.backend.domain.dictionary.mapper.DictionaryMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DictionaryService {

    private final DictionaryMapper dictionaryMapper;

    // 자격증 사전 목록 조회 (카테고리 필터 선택)
    @Transactional(readOnly = true)
    public List<CertDictionaryResponse> getCerts(String category) {
        log.info("[DictionaryService] 자격증 사전 조회 - category: {}", category);
        return dictionaryMapper.selectCerts(category);
    }

    // 어학 사전 목록 조회
    @Transactional(readOnly = true)
    public List<CertDictionaryResponse> getLanguages() {
        log.info("[DictionaryService] 어학 사전 조회");
        return dictionaryMapper.selectLanguages();
    }

    // 기술스택 사전 목록 조회 (카테고리 필터 선택)
    @Transactional(readOnly = true)
    public List<SkillDictionaryResponse> getSkills(String category) {
        log.info("[DictionaryService] 기술스택 사전 조회 - category: {}", category);
        return dictionaryMapper.selectSkills(category);
    }
}
