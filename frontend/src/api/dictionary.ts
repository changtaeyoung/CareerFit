import api from './axios';

// ── 타입 정의 ──────────────────────────────────────────

export interface CertDictionaryItem {
    id: number;
    name: string;
    issuer: string;
    category: string;  // 'IT' | '금융' | '어학'
}

export interface SkillDictionaryItem {
    id: number;
    name: string;
    category: string;  // 'BACKEND' | 'FRONTEND' | 'DATABASE' | ...
}

// ── API 호출 ──────────────────────────────────────────

// 자격증 사전 조회 (카테고리 필터 선택)
// category: 'IT' | '금융' — 미전달 시 어학 제외한 전체(IT+금융) 반환
export const getCertDictionary = async (category?: string): Promise<CertDictionaryItem[]> => {
    const response = await api.get('/dictionary/certs', {
        params: category ? { category } : undefined,
    });
    return response.data.data;
};

// 어학 사전 조회 (TOEIC, TOEFL, OPIC 등)
export const getLanguageDictionary = async (): Promise<CertDictionaryItem[]> => {
    const response = await api.get('/dictionary/languages');
    return response.data.data;
};

// 기술스택 사전 조회
export const getSkillDictionary = async (category?: string): Promise<SkillDictionaryItem[]> => {
    const response = await api.get('/dictionary/skills', {
        params: category ? { category } : undefined,
    });
    return response.data.data;
};
