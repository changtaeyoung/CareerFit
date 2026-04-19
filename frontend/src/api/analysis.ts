import api from './axios';

// ── 타입 정의 ──────────────────────────────────────────

export interface AnalysisRequest {
    jobPostingId: number;
    specVersionId?: number;
}

export interface AnalysisGap {
    id: number;
    category: string;       // CERT, LANGUAGE, SKILL
    itemName: string;
    requiredLevel: string;
    userLevel: string;
    met: boolean;
    score: number;
}

export interface AnalysisRecommendation {
    id: number;
    priority: string;       // P1, P2, P3
    category: string;
    title: string;
    description: string;
}

export interface AnalysisReportResponse {
    reportId: number;
    companyName: string;
    postingTitle: string;
    specVersionNo: number;
    requiredAllMet: boolean;
    baseScore: number;
    quantitativeBonus: number;
    qualitativeBonus: number;
    totalScore: number;
    status: string;
    createdAt: string;
    gaps: AnalysisGap[];
    recommendations: AnalysisRecommendation[];
}

export interface AnalysisHistoryResponse {
    reportId: number;
    companyName: string;
    postingTitle: string;
    requiredAllMet: boolean;
    totalScore: number;
    status: string;
    createdAt: string;
}

export interface PrerequisiteCheckResponse {
    postingId: number;
    eligible: boolean;
    failedRequirements: {
        type: string;
        itemName: string;
        requiredValue: string;
        userValue: string;
        message: string;
    }[];
}

// ── API 호출 ──────────────────────────────────────────

// 핏 분석 실행
export const analyze = async (data: AnalysisRequest): Promise<AnalysisReportResponse> => {
    const response = await api.post('/analysis', data);
    return response.data.data;
};

// 분석 히스토리 조회
export const getAnalysisHistory = async (): Promise<AnalysisHistoryResponse[]> => {
    const response = await api.get('/analysis/history');
    return response.data.data;
};

// 분석 리포트 상세 조회
export const getReportDetail = async (reportId: number): Promise<AnalysisReportResponse> => {
    const response = await api.get(`/analysis/${reportId}`);
    return response.data.data;
};

// 분석 리포트 삭제
export const deleteReport = async (reportId: number): Promise<void> => {
    await api.delete(`/analysis/${reportId}`);
};

// 공고 지원 자격 사전 체크
export const checkPrerequisite = async (postingId: number): Promise<PrerequisiteCheckResponse> => {
    const response = await api.get(`/analysis/postings/${postingId}/prerequisite-check`);
    return response.data.data;
};
