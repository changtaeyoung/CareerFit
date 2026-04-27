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

export interface QuestionResponse {
    id: number;
    postingId: number;
    question: string;
    sortOrder: number;
    maxLength?: number;
    lengthType?: 'CHAR' | 'BYTE';
}

// ── API 호출 ──────────────────────────────────────────

export const saveCoverLetter = async (data: CoverLetterRequest): Promise<CoverLetterResponse> => {
    const response = await api.post('/cover-letters', data);
    return response.data.data;
};

export const getCoverLettersByPosting = async (postingId: number): Promise<CoverLetterResponse[]> => {
    const response = await api.get('/cover-letters', { params: { postingId } });
    return response.data.data;
};

export const getCoverLetterById = async (id: number): Promise<CoverLetterResponse> => {
    const response = await api.get(`/cover-letters/${id}`);
    return response.data.data;
};

export const deleteCoverLetter = async (id: number): Promise<void> => {
    await api.delete(`/cover-letters/${id}`);
};

// 공고별 자소서 문항 목록 조회
export const getQuestionsByPosting = async (postingId: number): Promise<QuestionResponse[]> => {
    const response = await api.get('/cover-letters/questions', { params: { postingId } });
    return response.data.data;
};
