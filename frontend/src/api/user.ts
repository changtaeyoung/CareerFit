import api from './axios';

// ── 타입 정의 ──────────────────────────────────────────

export interface SpecBasicRequest {
    education: string;
    university: string;
    gpa: number;
    wantedJobs: string[];
    skills: { skillId: number; proficiency: string }[];
}

export interface SpecBasicResponse {
    versionId: number;
    versionNo: number;
}

export interface SpecQualificationRequest {
    certificates: {
        certId: number;
        status: string;
        score?: number;
        acquiredAt?: string;
    }[];
    languages: {
        langType: string;
        score?: number;
        grade?: string;
        acquiredAt?: string;
    }[];
}

export interface SpecExperienceRequest {
    interns: {
        companyName: string;
        employmentType: string;  // INTERN | FULL_TIME | CONTRACT
        role: string;
        description: string;
        startedAt: string;
        endedAt?: string;
    }[];
    projects: {
        title: string;
        description: string;
        githubUrl?: string;
        startedAt: string;
        endedAt?: string;
        status: string;
    }[];
    awards: {
        title: string;
        institution: string;
        grade: string;
        awardedAt: string;
    }[];
}

export interface SpecVersionSummary {
    id: number;
    versionNo: number;
    education: string;
    university: string;
    createdAt: string;
}

export interface SpecDetailResponse {
    versionId: number;
    versionNo: number;
    education: string;
    university: string;
    gpa: number;
    createdAt: string;
    wantedJobs: { id: number; jobType: string }[];
    skills: { id: number; skillName: string; proficiency: string; category?: string }[];
    certificates: { id: number; certName: string; status: string; score?: number; acquiredAt?: string }[];
    languages: { id: number; langType: string; score?: number; grade?: string; acquiredAt?: string }[];
    interns: { id: number; companyName: string; employmentType?: string; role: string; description: string; startedAt: string; endedAt?: string }[];
    projects: { id: number; title: string; description: string; githubUrl?: string; startedAt: string; endedAt?: string; status: string }[];
    awards: { id: number; title: string; institution: string; grade: string; awardedAt: string }[];
}

export interface PasswordChangeRequest {
    currentPassword: string;
    newPassword: string;
    newPasswordConfirm: string;
}

// ── API 호출 ──────────────────────────────────────────

// 스펙 기본 정보 등록 (1단계)
export const registerBasicSpec = async (data: SpecBasicRequest): Promise<SpecBasicResponse> => {
    const response = await api.post('/user/spec', data);
    return response.data.data;
};

// 자격증/어학 등록 (2단계)
export const registerQualification = async (versionId: number, data: SpecQualificationRequest): Promise<void> => {
    await api.post(`/user/spec/${versionId}/qualifications`, data);
};

// 경력/프로젝트/수상 등록 (3단계)
export const registerExperience = async (versionId: number, data: SpecExperienceRequest): Promise<void> => {
    await api.post(`/user/spec/${versionId}/experience`, data);
};

// 스펙 히스토리 조회
export const getSpecHistory = async (): Promise<SpecVersionSummary[]> => {
    const response = await api.get('/user/spec/history');
    return response.data.data;
};

// 스펙 상세 조회
export const getSpecDetail = async (versionId: number): Promise<SpecDetailResponse> => {
    const response = await api.get(`/user/spec/${versionId}`);
    return response.data.data;
};

// 스펙 버전 삭제
export const deleteSpecVersion = async (versionId: number): Promise<void> => {
    await api.delete(`/user/spec/${versionId}`);
};

// 비밀번호 변경
export const changePassword = async (data: PasswordChangeRequest): Promise<void> => {
    await api.patch('/user/password', data);
};

// 이름 수정
export const updateName = async (name: string): Promise<void> => {
    await api.patch('/user/name', null, { params: { name } });
};

// 회원 탈퇴
export const deleteUser = async (): Promise<void> => {
    await api.delete('/user');
};
