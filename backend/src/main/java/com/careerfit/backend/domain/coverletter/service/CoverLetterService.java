package com.careerfit.backend.domain.coverletter.service;

import com.careerfit.backend.common.exception.CustomException;
import com.careerfit.backend.common.exception.ErrorCode;
import com.careerfit.backend.domain.coverletter.dto.CoverLetterRequest;
import com.careerfit.backend.domain.coverletter.dto.CoverLetterResponse;
import com.careerfit.backend.domain.coverletter.entity.CoverLetter;
import com.careerfit.backend.domain.coverletter.mapper.CoverLetterMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CoverLetterService {

    private final CoverLetterMapper coverLetterMapper;

    // 자소서 저장, 동일 문항에 이미 작성된 자소서가 있으면 본문을 수정 - upsert 방식 이용
    @Transactional
    public CoverLetterResponse save(Long userId, CoverLetterRequest request) {
        log.info("[CoverLetterService] 자소서 저장 - userId: {}, questionId: {}", userId, request.getQuestionId());

        Long existingId = coverLetterMapper.selectIdByUserAndQuestion(userId, request.getQuestionId());

        if (existingId != null) {
            // 이미 있으면 본문만 수정
            coverLetterMapper.updateContent(existingId, userId, request.getContent());
            log.info("[CoverLetterService] 자소서 수정 완료 - id: {}", existingId);
            return coverLetterMapper.selectById(existingId, userId);
        }

        // 새로 저장
        CoverLetter coverLetter = CoverLetter.builder()
                .userId(userId)
                .postingId(request.getPostingId())
                .questionId(request.getQuestionId())
                .content(request.getContent())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        coverLetterMapper.insert(coverLetter);
        log.info("[CoverLetterService] 자소서 저장 완료 - id: {}", coverLetter.getId());
        return coverLetterMapper.selectById(coverLetter.getId(), userId);
    }

    // 공고 ID 기준으로 내가 작성한 자소서 목록을 문항 내용과 함께 반환
    @Transactional(readOnly = true)
    public List<CoverLetterResponse> getByPosting(Long userId, Long postingId) {
        log.info("[CoverLetterService] 공고별 자소서 조회 - userId: {}, postingId: {}", userId, postingId);
        return coverLetterMapper.selectByPostingId(userId, postingId);
    }

    // 자소서 ID로 단건 조회. 본인 소유가 아니면 NOT_FOUND 처리
    @Transactional(readOnly = true)
    public CoverLetterResponse getById(Long userId, Long coverLetterId) {
        log.info("[CoverLetterService] 자소서 단건 조회 - id: {}", coverLetterId);
        CoverLetterResponse response = coverLetterMapper.selectById(coverLetterId, userId);
        if (response == null) {
            throw new CustomException(ErrorCode.COVER_LETTER_NOT_FOUND);
        }
        return response;
    }

    // 자소서를 삭제. 본인 소유가 아닌 경우 삭제되지 않음 (userId 조건 포함)
    @Transactional
    public void delete(Long userId, Long coverLetterId) {
        log.info("[CoverLetterService] 자소서 삭제 - id: {}", coverLetterId);
        if (coverLetterMapper.selectById(coverLetterId, userId) == null) {
            throw new CustomException(ErrorCode.COVER_LETTER_NOT_FOUND);
        }
        coverLetterMapper.delete(coverLetterId, userId);
    }
}
