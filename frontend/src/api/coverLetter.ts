import api from './axios';

// ── 타입 정의 ──────────────────────────────────────────

export interface CoverLetterRequest {
    postingId: number;
    questionId: number;
    content: string;
}

export interface CoverLetterResponse {
    id: number;
    postingId: number;
    questionId: number;
    questionContent: string;
    content: string;
    createdAt: string;
    updatedAt: string;
}

// ── API 호출 ──────────────────────────────────────────

// 자소서 저장/수정 (upsert)
export const saveCoverLetter = async (data: CoverLetterRequest): Promise<CoverLetterResponse> => {
    const response = await api.post('/cover-letters', data);
    return response.data.data;
};

// 공고별 내 자소서 목록 조회
export const getCoverLettersByPosting = async (postingId: number): Promise<CoverLetterResponse[]> => {
    const response = await api.get('/cover-letters', { params: { postingId } });
    return response.data.data;
};

// 자소서 단건 조회
export const getCoverLetterById = async (id: number): Promise<CoverLetterResponse> => {
    const response = await api.get(`/cover-letters/${id}`);
    return response.data.data;
};

// 자소서 삭제
export const deleteCoverLetter = async (id: number): Promise<void> => {
    await api.delete(`/cover-letters/${id}`);
};
