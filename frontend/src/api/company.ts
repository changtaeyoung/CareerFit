import api from './axios';

// ── 타입 정의 ──────────────────────────────────────────

export interface CompanyResponse {
    id: number;
    name: string;
    industry: string;
    companyType: string;
    location: string;
    employeeCount: number;
}

export interface CompanySalaryResponse {
    entrySalary: number | null;
    entryYear: number | null;
    entrySource: string | null;
    averageSalary: number | null;
    averageYear: number | null;
    averageSource: string | null;
}

export interface CompanyDetailResponse {
    id: number;
    name: string;
    industry: string;
    companyType: string;
    location: string;
    website: string;
    employeeCount: number;
    foundedYear: number;
    isPublic: boolean;
    vision: string;
    talentImage: string;
    businessOverview: string;
    salary: CompanySalaryResponse | null;  // 연봉 데이터 없으면 null
}

export interface JobPostingResponse {
    id: number;
    companyId: number;
    companyName: string;
    title: string;
    url: string;
    status: string;
    jobType: string;
    season: string;
    startedAt: string;
    deadline: string;
}

export interface PageResponse<T> {
    content: T[];
    totalCount: number;
    totalPages: number;
    currentPage: number;
    pageSize: number;
}

// ── API 호출 ──────────────────────────────────────────

// 기업 목록 조회 (필터 + 페이징) → PageResponse로 totalPages 포함
export const getCompanyList = async (
    page = 1,
    size = 12,
    industry?: string,
    companyType?: string,
    keyword?: string,
): Promise<PageResponse<CompanyResponse>> => {
    const params: Record<string, string | number> = { page, size };
    if (industry) params.industry = industry;
    if (companyType) params.companyType = companyType;
    if (keyword) params.keyword = keyword;
    const response = await api.get('/companies', { params });
    return response.data.data;
};

// 기업 상세 조회
export const getCompanyDetail = async (id: number): Promise<CompanyDetailResponse> => {
    const response = await api.get(`/companies/${id}`);
    return response.data.data;
};

// 기업별 채용공고 목록
export const getPostingsByCompany = async (companyId: number): Promise<JobPostingResponse[]> => {
    const response = await api.get(`/companies/${companyId}/postings`);
    return response.data.data;
};

// 전체 채용공고 목록 (필터 + 페이징) → PageResponse로 totalPages 포함
export const getPostings = async (
    page = 1,
    size = 10,
    jobType?: string,
    status?: string,
    keyword?: string,
): Promise<PageResponse<JobPostingResponse>> => {
    const params: Record<string, string | number> = { page, size };
    if (jobType) params.jobType = jobType;
    if (status) params.status = status;
    if (keyword) params.keyword = keyword;
    const response = await api.get('/postings', { params });
    return response.data.data;
};

// 채용공고 상세 조회
export const getPostingDetail = async (id: number): Promise<JobPostingResponse> => {
    const response = await api.get(`/postings/${id}`);
    return response.data.data;
};
