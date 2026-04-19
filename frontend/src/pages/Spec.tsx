import { useEffect, useState } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import {
    FileText, Plus, ChevronDown, ChevronRight, Trash2,
    GraduationCap, Award, Code, Briefcase, Languages, Clock
} from 'lucide-react';
import {
    registerBasicSpec, registerQualification, registerExperience,
    getSpecHistory, getSpecDetail, deleteSpecVersion,
} from '../api/user';
import type {
    SpecBasicRequest, SpecQualificationRequest, SpecExperienceRequest,
    SpecVersionSummary, SpecDetailResponse
} from '../api/user';
import { getCertDictionary, getLanguageDictionary, getSkillDictionary } from '../api/dictionary';
import type { CertDictionaryItem, SkillDictionaryItem } from '../api/dictionary';
import EmptyState from '../components/EmptyState';
import ConfirmModal from '../components/ConfirmModal';
import { toast } from '../store/toastStore';

type Step = 'basic' | 'qualification' | 'experience';

export default function Spec() {
    const [history, setHistory] = useState<SpecVersionSummary[]>([]);
    const [expandedId, setExpandedId] = useState<number | null>(null);
    const [expandedDetail, setExpandedDetail] = useState<SpecDetailResponse | null>(null);
    const [showForm, setShowForm] = useState(false);
    const [step, setStep] = useState<Step>('basic');
    const [currentVersionId, setCurrentVersionId] = useState<number | null>(null);
    const [deleteTarget, setDeleteTarget] = useState<number | null>(null);
    const [loading, setLoading] = useState(true);
    const [submitting, setSubmitting] = useState(false);
    const [deleting, setDeleting] = useState(false);

    // 기본 정보 폼
    const [education, setEducation] = useState('');
    const [university, setUniversity] = useState('');
    const [gpa, setGpa] = useState('');
    const [wantedJobs, setWantedJobs] = useState('');
    // 기술스택 폼 (여러 개 추가 가능)
    const [skills, setSkills] = useState<{ skillId: number | ''; proficiency: string }[]>([]);
    // 자격증 폼 (여러 개 추가 가능) — cert_dictionary의 id 참조
    const [certs, setCerts] = useState<{ certId: number | ''; status: string; acquiredAt: string }[]>([]);
    // 어학 폼 (여러 개 추가 가능) — dictionary의 name을 langType으로 사용
    const [langs, setLangs] = useState<{ langType: string; score: string; grade: string }[]>([]);
    // 사전 데이터
    const [skillDict, setSkillDict] = useState<SkillDictionaryItem[]>([]);
    const [certDict, setCertDict] = useState<CertDictionaryItem[]>([]);
    const [langDict, setLangDict] = useState<CertDictionaryItem[]>([]);

    // 경력 폼 (여러 건 추가 가능) — 현재 재직중이면 endedAt 비움
    const [interns, setInterns] = useState<{
        companyName: string;
        role: string;
        employmentType: string;  // INTERN | FULL_TIME | CONTRACT
        description: string;
        startedAt: string;
        endedAt: string;
        isCurrent: boolean;
    }[]>([]);

    // 프로젝트 폼 (여러 건 추가 가능)
    const [projects, setProjects] = useState<{
        title: string;
        description: string;
        githubUrl: string;
        startedAt: string;
        endedAt: string;
        status: string;  // 진행중 | 완료
    }[]>([]);

    // 수상 폼 (여러 건 추가 가능)
    const [awards, setAwards] = useState<{
        title: string;
        institution: string;
        grade: string;
        awardedAt: string;
    }[]>([]);

    const fetchHistory = async () => {
        setLoading(true);
        try {
            const data = await getSpecHistory();
            setHistory(data);
        } catch {
            setHistory([]);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchHistory();
        // 사전 데이터는 한 번만 로드 (거의 변하지 않음)
        getSkillDictionary().then(setSkillDict).catch(() => setSkillDict([]));
        getCertDictionary().then(setCertDict).catch(() => setCertDict([]));
        getLanguageDictionary().then(setLangDict).catch(() => setLangDict([]));
    }, []);

    const handleExpand = async (versionId: number) => {
        if (expandedId === versionId) {
            setExpandedId(null);
            setExpandedDetail(null);
            return;
        }
        try {
            const detail = await getSpecDetail(versionId);
            setExpandedDetail(detail);
            setExpandedId(versionId);
        } catch {
            toast.error('상세 조회에 실패했습니다');
        }
    };

    const handleBasicSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setSubmitting(true);
        try {
            const data: SpecBasicRequest = {
                education,
                university,
                gpa: parseFloat(gpa),
                wantedJobs: wantedJobs.split(',').map(j => j.trim()).filter(Boolean),
                skills: skills
                    .filter(s => typeof s.skillId === 'number' && s.skillId > 0)
                    .map(s => ({ skillId: s.skillId as number, proficiency: s.proficiency })),
            };
            const result = await registerBasicSpec(data);
            setCurrentVersionId(result.versionId);
            setStep('qualification');
        } catch (err: any) {
            toast.error(err.response?.data?.message || '등록에 실패했습니다');
        } finally {
            setSubmitting(false);
        }
    };

    const handleQualificationSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        if (!currentVersionId) return;
        setSubmitting(true);
        try {
            const data: SpecQualificationRequest = {
                // certId가 선택된(숫자) 항목만 필터링
                certificates: certs
                    .filter(c => typeof c.certId === 'number' && c.certId > 0)
                    .map(c => ({
                        certId: c.certId as number,
                        status: '취득',
                        acquiredAt: c.acquiredAt || undefined,
                    })),
                // 어학은 langType(문자열) 선택된 것만
                languages: langs
                    .filter(l => l.langType.trim())
                    .map(l => {
                        const base: { langType: string; score?: number; grade?: string } = {
                            langType: l.langType,
                        };
                        if (l.score) base.score = parseInt(l.score);
                        if (l.grade?.trim()) base.grade = l.grade.trim();
                        return base;
                    }),
            };
            await registerQualification(currentVersionId, data);
            setStep('experience');
        } catch (err: any) {
            toast.error(err.response?.data?.message || '등록에 실패했습니다');
        } finally {
            setSubmitting(false);
        }
    };

    const handleExperienceSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        if (!currentVersionId) return;
        setSubmitting(true);
        try {
            const data: SpecExperienceRequest = {
                // 회사명 + 시작일이 있는 것만 유효한 경력으로 간주
                interns: interns
                    .filter(i => i.companyName.trim() && i.startedAt)
                    .map(i => {
                        const base: any = {
                            companyName: i.companyName.trim(),
                            employmentType: i.employmentType,
                            role: i.role.trim(),
                            description: i.description.trim(),
                            startedAt: i.startedAt,
                        };
                        // 재직중이 아니고 종료일이 있을 때만 endedAt 포함
                        if (!i.isCurrent && i.endedAt) base.endedAt = i.endedAt;
                        return base;
                    }),
                // 프로젝트명 + 시작일이 있는 것만 유효
                projects: projects
                    .filter(p => p.title.trim() && p.startedAt)
                    .map(p => {
                        const base: any = {
                            title: p.title.trim(),
                            description: p.description.trim(),
                            startedAt: p.startedAt,
                            status: p.status,
                        };
                        if (p.githubUrl.trim()) base.githubUrl = p.githubUrl.trim();
                        if (p.endedAt) base.endedAt = p.endedAt;
                        return base;
                    }),
                // 수상명 + 수상일이 있는 것만 유효
                awards: awards
                    .filter(a => a.title.trim() && a.awardedAt)
                    .map(a => ({
                        title: a.title.trim(),
                        institution: a.institution.trim(),
                        grade: a.grade.trim(),
                        awardedAt: a.awardedAt,
                    })),
            };
            await registerExperience(currentVersionId, data);
            setShowForm(false);
            setStep('basic');
            setCurrentVersionId(null);
            resetForm();
            fetchHistory();
            toast.success('스펙이 등록되었습니다');
        } catch (err: any) {
            toast.error(err.response?.data?.message || '등록에 실패했습니다');
        } finally {
            setSubmitting(false);
        }
    };

    const resetForm = () => {
        setEducation(''); setUniversity(''); setGpa(''); setWantedJobs('');
        setSkills([]); setCerts([]); setLangs([]);
        setInterns([]); setProjects([]); setAwards([]);
    };

    // 기술스택을 카테고리별로 그룹핑
    const skillsByCategory = skillDict.reduce<Record<string, SkillDictionaryItem[]>>((acc, s) => {
        (acc[s.category] = acc[s.category] || []).push(s);
        return acc;
    }, {});
    const categoryLabels: Record<string, string> = {
        BACKEND: '백엔드', FRONTEND: '프론트엔드', DATA: '데이터',
        DEVOPS: 'DevOps', AI: 'AI/ML', ETC: '기타',
    };

    const handleDelete = async () => {
        if (!deleteTarget) return;
        setDeleting(true);
        try {
            await deleteSpecVersion(deleteTarget);
            setHistory(prev => prev.filter(h => h.id !== deleteTarget));
            if (expandedId === deleteTarget) { setExpandedId(null); setExpandedDetail(null); }
            toast.success('스펙이 삭제되었습니다');
        } catch {
            toast.error('삭제에 실패했습니다');
        } finally {
            setDeleting(false);
            setDeleteTarget(null);
        }
    };

    const stepLabels = { basic: '기본 정보', qualification: '자격증 / 어학', experience: '경력 / 프로젝트' };
    const steps: Step[] = ['basic', 'qualification', 'experience'];

    return (
        <div className="space-y-6">
            <div className="flex items-center justify-between">
                <div>
                    <h1 className="section-title">내 스펙 관리</h1>
                    <p className="section-subtitle mt-1">스펙을 등록하고 버전별로 관리하세요</p>
                </div>
                {!showForm && (
                    <motion.button
                        whileHover={{ scale: 1.03 }}
                        whileTap={{ scale: 0.97 }}
                        onClick={() => setShowForm(true)}
                        className="btn-primary flex items-center gap-2 text-sm"
                    >
                        <Plus className="w-4 h-4" />
                        새 스펙 등록
                    </motion.button>
                )}
            </div>

            {/* 등록 폼 위자드 */}
            <AnimatePresence>
                {showForm && (
                    <motion.div
                        initial={{ opacity: 0, height: 0 }}
                        animate={{ opacity: 1, height: 'auto' }}
                        exit={{ opacity: 0, height: 0 }}
                        className="glass-card p-6 overflow-hidden"
                    >
                        {/* 스텝 인디케이터 */}
                        <div className="flex items-center gap-2 mb-6">
                            {steps.map((s, i) => (
                                <div key={s} className="flex items-center gap-2">
                                    <div className={`w-8 h-8 rounded-full flex items-center justify-center text-sm font-bold transition-colors
                                        ${steps.indexOf(step) >= i
                                            ? 'bg-brand-500 text-white'
                                            : 'bg-surface-200 dark:bg-surface-700 text-surface-500 dark:text-surface-400'
                                        }`}>
                                        {i + 1}
                                    </div>
                                    <span className={`text-sm font-medium hidden sm:block ${
                                        step === s ? 'text-surface-900 dark:text-surface-100' : 'text-surface-400 dark:text-surface-500'
                                    }`}>
                                        {stepLabels[s]}
                                    </span>
                                    {i < steps.length - 1 && (
                                        <div className={`w-8 h-0.5 ${
                                            steps.indexOf(step) > i ? 'bg-brand-500' : 'bg-surface-200 dark:bg-surface-700'
                                        }`} />
                                    )}
                                </div>
                            ))}
                        </div>

                        {step === 'basic' && (
                            <form onSubmit={handleBasicSubmit} className="space-y-4">
                                <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                                    <div>
                                        <label className="label-text">학력</label>
                                        <select value={education} onChange={e => setEducation(e.target.value)} className="input-field" required>
                                            <option value="">선택</option>
                                            <option value="재학">재학</option>
                                            <option value="휴학">휴학</option>
                                            <option value="졸업예정">졸업예정</option>
                                            <option value="졸업">졸업</option>
                                        </select>
                                    </div>
                                    <div>
                                        <label className="label-text">학교명</label>
                                        <input value={university} onChange={e => setUniversity(e.target.value)} className="input-field" placeholder="한국대학교" required />
                                    </div>
                                    <div>
                                        <label className="label-text">학점 (4.5 만점)</label>
                                        <input type="number" step="0.1" min="0" max="4.5" value={gpa} onChange={e => setGpa(e.target.value)} className="input-field" placeholder="3.8" />
                                    </div>
                                    <div>
                                        <label className="label-text">희망 직무 (콤마 구분)</label>
                                        <input value={wantedJobs} onChange={e => setWantedJobs(e.target.value)} className="input-field" placeholder="BACKEND, FULLSTACK" required />
                                    </div>
                                </div>

                                {/* ── 기술스택 섹션 ── */}
                                <div className="rounded-xl border border-surface-200 dark:border-surface-700 p-5">
                                    <div className="flex items-center justify-between mb-4">
                                        <div className="flex items-center gap-2">
                                            <Code className="w-5 h-5 text-accent-500" />
                                            <h3 className="font-semibold text-surface-800 dark:text-surface-200">기술스택</h3>
                                        </div>
                                        <button
                                            type="button"
                                            onClick={() => setSkills(prev => [...prev, { skillId: '', proficiency: '중급' }])}
                                            className="flex items-center gap-1.5 text-sm font-medium text-brand-600 dark:text-brand-400 hover:text-brand-700 dark:hover:text-brand-300 transition-colors"
                                        >
                                            <Plus className="w-4 h-4" />
                                            추가하기
                                        </button>
                                    </div>

                                    {skills.length === 0 ? (
                                        <p className="text-sm text-surface-400 dark:text-surface-500 text-center py-4">
                                            기술스택을 추가하려면 위의 "추가하기" 버튼을 클릭하세요
                                        </p>
                                    ) : (
                                        <div className="space-y-3">
                                            {skills.map((skill, idx) => (
                                                <motion.div
                                                    key={idx}
                                                    initial={{ opacity: 0, y: -8 }}
                                                    animate={{ opacity: 1, y: 0 }}
                                                    className="flex items-end gap-3"
                                                >
                                                    <div className="flex-1">
                                                        {idx === 0 && <label className="label-text">기술명</label>}
                                                        <select
                                                            value={skill.skillId}
                                                            onChange={e => {
                                                                const updated = [...skills];
                                                                updated[idx].skillId = e.target.value ? parseInt(e.target.value) : '';
                                                                setSkills(updated);
                                                            }}
                                                            className="input-field"
                                                            required
                                                        >
                                                            <option value="">선택</option>
                                                            {Object.entries(skillsByCategory).map(([cat, items]) => (
                                                                <optgroup key={cat} label={categoryLabels[cat] || cat}>
                                                                    {items.map(s => (
                                                                        <option key={s.id} value={s.id}>{s.name}</option>
                                                                    ))}
                                                                </optgroup>
                                                            ))}
                                                        </select>
                                                    </div>
                                                    <div className="w-32 flex-shrink-0">
                                                        {idx === 0 && <label className="label-text">숙련도</label>}
                                                        <select
                                                            value={skill.proficiency}
                                                            onChange={e => {
                                                                const updated = [...skills];
                                                                updated[idx].proficiency = e.target.value;
                                                                setSkills(updated);
                                                            }}
                                                            className="input-field"
                                                        >
                                                            <option value="초급">초급</option>
                                                            <option value="중급">중급</option>
                                                            <option value="고급">고급</option>
                                                        </select>
                                                    </div>
                                                    <button
                                                        type="button"
                                                        onClick={() => setSkills(prev => prev.filter((_, i) => i !== idx))}
                                                        className="p-2.5 rounded-lg hover:bg-red-50 dark:hover:bg-red-500/10 transition-colors flex-shrink-0 mb-0.5"
                                                    >
                                                        <Trash2 className="w-4 h-4 text-red-500" />
                                                    </button>
                                                </motion.div>
                                            ))}
                                        </div>
                                    )}
                                </div>

                                <div className="flex gap-3 justify-end">
                                    <button type="button" onClick={() => { setShowForm(false); resetForm(); }} className="btn-secondary text-sm">취소</button>
                                    <button type="submit" disabled={submitting} className="btn-primary text-sm">{submitting ? '저장중...' : '다음 →'}</button>
                                </div>
                            </form>
                        )}

                        {step === 'qualification' && (
                            <form onSubmit={handleQualificationSubmit} className="space-y-6">
                                <p className="text-sm text-surface-500 dark:text-surface-400">없으면 비워두고 다음으로 넘어가세요</p>

                                {/* ── 자격증 섹션 ── */}
                                <div className="rounded-xl border border-surface-200 dark:border-surface-700 p-5">
                                    <div className="flex items-center justify-between mb-4">
                                        <div className="flex items-center gap-2">
                                            <Award className="w-5 h-5 text-accent-500" />
                                            <h3 className="font-semibold text-surface-800 dark:text-surface-200">자격증</h3>
                                        </div>
                                        <button
                                            type="button"
                                            onClick={() => setCerts(prev => [...prev, { certId: '', status: '취득', acquiredAt: '' }])}
                                            className="flex items-center gap-1.5 text-sm font-medium text-brand-600 dark:text-brand-400 hover:text-brand-700 dark:hover:text-brand-300 transition-colors"
                                        >
                                            <Plus className="w-4 h-4" />
                                            추가하기
                                        </button>
                                    </div>

                                    {certs.length === 0 ? (
                                        <p className="text-sm text-surface-400 dark:text-surface-500 text-center py-4">
                                            자격증을 추가하려면 위의 "추가하기" 버튼을 클릭하세요
                                        </p>
                                    ) : (
                                        <div className="space-y-3">
                                            {certs.map((cert, idx) => (
                                                <motion.div
                                                    key={idx}
                                                    initial={{ opacity: 0, y: -8 }}
                                                    animate={{ opacity: 1, y: 0 }}
                                                    className="flex items-end gap-3"
                                                >
                                                    <div className="flex-1">
                                                        {idx === 0 && <label className="label-text">자격증명</label>}
                                                        <select
                                                            value={cert.certId}
                                                            onChange={e => {
                                                                const updated = [...certs];
                                                                updated[idx].certId = e.target.value ? parseInt(e.target.value) : '';
                                                                setCerts(updated);
                                                            }}
                                                            className="input-field"
                                                            required
                                                        >
                                                            <option value="">선택</option>
                                                            {certDict.map(c => (
                                                                <option key={c.id} value={c.id}>
                                                                    {c.name} ({c.category})
                                                                </option>
                                                            ))}
                                                        </select>
                                                    </div>
                                                    <div className="w-24 flex-shrink-0">
                                                        {idx === 0 && <label className="label-text">상태</label>}
                                                        <input
                                                            value="취득"
                                                            disabled
                                                            className="input-field bg-surface-100 dark:bg-surface-700 text-surface-500 dark:text-surface-400 cursor-not-allowed"
                                                        />
                                                    </div>
                                                    <div className="w-40 flex-shrink-0">
                                                        {idx === 0 && <label className="label-text">취득일</label>}
                                                        <input
                                                            type="date"
                                                            value={cert.acquiredAt}
                                                            onChange={e => {
                                                                const updated = [...certs];
                                                                updated[idx].acquiredAt = e.target.value;
                                                                setCerts(updated);
                                                            }}
                                                            className="input-field"
                                                            required
                                                        />
                                                    </div>
                                                    <button
                                                        type="button"
                                                        onClick={() => setCerts(prev => prev.filter((_, i) => i !== idx))}
                                                        className="p-2.5 rounded-lg hover:bg-red-50 dark:hover:bg-red-500/10 transition-colors flex-shrink-0 mb-0.5"
                                                    >
                                                        <Trash2 className="w-4 h-4 text-red-500" />
                                                    </button>
                                                </motion.div>
                                            ))}
                                        </div>
                                    )}
                                </div>

                                {/* ── 어학 섹션 ── */}
                                <div className="rounded-xl border border-surface-200 dark:border-surface-700 p-5">
                                    <div className="flex items-center justify-between mb-4">
                                        <div className="flex items-center gap-2">
                                            <Languages className="w-5 h-5 text-brand-500" />
                                            <h3 className="font-semibold text-surface-800 dark:text-surface-200">어학</h3>
                                        </div>
                                        <button
                                            type="button"
                                            onClick={() => setLangs(prev => [...prev, { langType: '', score: '', grade: '' }])}
                                            className="flex items-center gap-1.5 text-sm font-medium text-brand-600 dark:text-brand-400 hover:text-brand-700 dark:hover:text-brand-300 transition-colors"
                                        >
                                            <Plus className="w-4 h-4" />
                                            추가하기
                                        </button>
                                    </div>

                                    {langs.length === 0 ? (
                                        <p className="text-sm text-surface-400 dark:text-surface-500 text-center py-4">
                                            어학 성적을 추가하려면 위의 "추가하기" 버튼을 클릭하세요
                                        </p>
                                    ) : (
                                        <div className="space-y-3">
                                            {langs.map((lang, idx) => (
                                                <motion.div
                                                    key={idx}
                                                    initial={{ opacity: 0, y: -8 }}
                                                    animate={{ opacity: 1, y: 0 }}
                                                    className="flex items-end gap-3"
                                                >
                                                    <div className="flex-1">
                                                        {idx === 0 && <label className="label-text">어학 종류</label>}
                                                        <select
                                                            value={lang.langType}
                                                            onChange={e => {
                                                                const updated = [...langs];
                                                                updated[idx].langType = e.target.value;
                                                                setLangs(updated);
                                                            }}
                                                            className="input-field"
                                                            required
                                                        >
                                                            <option value="">선택</option>
                                                            {langDict.map(l => (
                                                                <option key={l.id} value={l.name}>
                                                                    {l.name}
                                                                </option>
                                                            ))}
                                                        </select>
                                                    </div>
                                                    <div className="w-32 flex-shrink-0">
                                                        {idx === 0 && <label className="label-text">점수</label>}
                                                        <input
                                                            type="number"
                                                            value={lang.score}
                                                            onChange={e => {
                                                                const updated = [...langs];
                                                                updated[idx].score = e.target.value;
                                                                setLangs(updated);
                                                            }}
                                                            className="input-field"
                                                            placeholder="850"
                                                        />
                                                    </div>
                                                    <div className="w-32 flex-shrink-0">
                                                        {idx === 0 && <label className="label-text">등급</label>}
                                                        <input
                                                            value={lang.grade}
                                                            onChange={e => {
                                                                const updated = [...langs];
                                                                updated[idx].grade = e.target.value;
                                                                setLangs(updated);
                                                            }}
                                                            className="input-field"
                                                            placeholder="IM2, AL..."
                                                        />
                                                    </div>
                                                    <button
                                                        type="button"
                                                        onClick={() => setLangs(prev => prev.filter((_, i) => i !== idx))}
                                                        className="p-2.5 rounded-lg hover:bg-red-50 dark:hover:bg-red-500/10 transition-colors flex-shrink-0 mb-0.5"
                                                    >
                                                        <Trash2 className="w-4 h-4 text-red-500" />
                                                    </button>
                                                </motion.div>
                                            ))}
                                        </div>
                                    )}
                                </div>

                                <div className="flex gap-3 justify-end">
                                    <button type="button" onClick={() => setStep('basic')} className="btn-secondary text-sm">← 이전</button>
                                    <button type="submit" disabled={submitting} className="btn-primary text-sm">{submitting ? '저장중...' : '다음 →'}</button>
                                </div>
                            </form>
                        )}

                        {step === 'experience' && (
                            <form onSubmit={handleExperienceSubmit} className="space-y-6">
                                <p className="text-sm text-surface-500 dark:text-surface-400">없으면 비워두고 완료하세요</p>

                                {/* ── 경력 · 인턴 섹션 ── */}
                                <div className="rounded-xl border border-surface-200 dark:border-surface-700 p-5">
                                    <div className="flex items-center justify-between mb-4">
                                        <div className="flex items-center gap-2">
                                            <Briefcase className="w-5 h-5 text-brand-500" />
                                            <h3 className="font-semibold text-surface-800 dark:text-surface-200">경력 / 인턴</h3>
                                        </div>
                                        <button
                                            type="button"
                                            onClick={() => setInterns(prev => [...prev, {
                                                companyName: '', role: '', employmentType: 'INTERN',
                                                description: '', startedAt: '', endedAt: '', isCurrent: false,
                                            }])}
                                            className="flex items-center gap-1.5 text-sm font-medium text-brand-600 dark:text-brand-400 hover:text-brand-700 dark:hover:text-brand-300 transition-colors"
                                        >
                                            <Plus className="w-4 h-4" />
                                            추가하기
                                        </button>
                                    </div>

                                    {interns.length === 0 ? (
                                        <p className="text-sm text-surface-400 dark:text-surface-500 text-center py-4">
                                            경력이나 인턴 경험을 추가하려면 위의 "추가하기" 버튼을 클릭하세요
                                        </p>
                                    ) : (
                                        <div className="space-y-4">
                                            {interns.map((intern, idx) => (
                                                <motion.div
                                                    key={idx}
                                                    initial={{ opacity: 0, y: -8 }}
                                                    animate={{ opacity: 1, y: 0 }}
                                                    className="rounded-lg bg-surface-50 dark:bg-surface-800/40 p-4 space-y-3 relative"
                                                >
                                                    <button
                                                        type="button"
                                                        onClick={() => setInterns(prev => prev.filter((_, i) => i !== idx))}
                                                        className="absolute top-3 right-3 p-1.5 rounded-lg hover:bg-red-50 dark:hover:bg-red-500/10 transition-colors"
                                                    >
                                                        <Trash2 className="w-4 h-4 text-red-500" />
                                                    </button>

                                                    <div className="grid grid-cols-1 sm:grid-cols-2 gap-3 pr-8">
                                                        <div>
                                                            <label className="label-text">회사명</label>
                                                            <input
                                                                value={intern.companyName}
                                                                onChange={e => {
                                                                    const updated = [...interns];
                                                                    updated[idx].companyName = e.target.value;
                                                                    setInterns(updated);
                                                                }}
                                                                className="input-field"
                                                                placeholder="카카오"
                                                            />
                                                        </div>
                                                        <div>
                                                            <label className="label-text">고용 형태</label>
                                                            <select
                                                                value={intern.employmentType}
                                                                onChange={e => {
                                                                    const updated = [...interns];
                                                                    updated[idx].employmentType = e.target.value;
                                                                    setInterns(updated);
                                                                }}
                                                                className="input-field"
                                                            >
                                                                <option value="INTERN">인턴</option>
                                                                <option value="FULL_TIME">정규직</option>
                                                                <option value="CONTRACT">계약직</option>
                                                            </select>
                                                        </div>
                                                        <div>
                                                            <label className="label-text">담당 직무</label>
                                                            <input
                                                                value={intern.role}
                                                                onChange={e => {
                                                                    const updated = [...interns];
                                                                    updated[idx].role = e.target.value;
                                                                    setInterns(updated);
                                                                }}
                                                                className="input-field"
                                                                placeholder="백엔드 개발 · 사원"
                                                            />
                                                        </div>
                                                        <div className="grid grid-cols-2 gap-2">
                                                            <div>
                                                                <label className="label-text">시작일</label>
                                                                <input
                                                                    type="date"
                                                                    value={intern.startedAt}
                                                                    onChange={e => {
                                                                        const updated = [...interns];
                                                                        updated[idx].startedAt = e.target.value;
                                                                        setInterns(updated);
                                                                    }}
                                                                    className="input-field"
                                                                />
                                                            </div>
                                                            <div>
                                                                <label className="label-text">종료일</label>
                                                                <input
                                                                    type="date"
                                                                    value={intern.endedAt}
                                                                    disabled={intern.isCurrent}
                                                                    onChange={e => {
                                                                        const updated = [...interns];
                                                                        updated[idx].endedAt = e.target.value;
                                                                        setInterns(updated);
                                                                    }}
                                                                    className="input-field disabled:opacity-40 disabled:cursor-not-allowed"
                                                                />
                                                            </div>
                                                        </div>
                                                    </div>

                                                    <label className="flex items-center gap-2 text-sm text-surface-700 dark:text-surface-300 cursor-pointer">
                                                        <input
                                                            type="checkbox"
                                                            checked={intern.isCurrent}
                                                            onChange={e => {
                                                                const updated = [...interns];
                                                                updated[idx].isCurrent = e.target.checked;
                                                                if (e.target.checked) updated[idx].endedAt = '';
                                                                setInterns(updated);
                                                            }}
                                                            className="rounded border-surface-300 dark:border-surface-600"
                                                        />
                                                        현재 재직중
                                                    </label>

                                                    <div>
                                                        <label className="label-text">업무 내용</label>
                                                        <textarea
                                                            value={intern.description}
                                                            onChange={e => {
                                                                const updated = [...interns];
                                                                updated[idx].description = e.target.value;
                                                                setInterns(updated);
                                                            }}
                                                            rows={3}
                                                            className="input-field resize-y min-h-[80px]"
                                                            placeholder="Spring Boot 기반 백엔드 API 개발, EDI 연동 성능 개선..."
                                                        />
                                                    </div>
                                                </motion.div>
                                            ))}
                                        </div>
                                    )}
                                </div>

                                {/* ── 프로젝트 섹션 ── */}
                                <div className="rounded-xl border border-surface-200 dark:border-surface-700 p-5">
                                    <div className="flex items-center justify-between mb-4">
                                        <div className="flex items-center gap-2">
                                            <Code className="w-5 h-5 text-accent-500" />
                                            <h3 className="font-semibold text-surface-800 dark:text-surface-200">프로젝트</h3>
                                        </div>
                                        <button
                                            type="button"
                                            onClick={() => setProjects(prev => [...prev, {
                                                title: '', description: '', githubUrl: '',
                                                startedAt: '', endedAt: '', status: '진행중',
                                            }])}
                                            className="flex items-center gap-1.5 text-sm font-medium text-brand-600 dark:text-brand-400 hover:text-brand-700 dark:hover:text-brand-300 transition-colors"
                                        >
                                            <Plus className="w-4 h-4" />
                                            추가하기
                                        </button>
                                    </div>

                                    {projects.length === 0 ? (
                                        <p className="text-sm text-surface-400 dark:text-surface-500 text-center py-4">
                                            프로젝트를 추가하려면 위의 "추가하기" 버튼을 클릭하세요
                                        </p>
                                    ) : (
                                        <div className="space-y-4">
                                            {projects.map((proj, idx) => (
                                                <motion.div
                                                    key={idx}
                                                    initial={{ opacity: 0, y: -8 }}
                                                    animate={{ opacity: 1, y: 0 }}
                                                    className="rounded-lg bg-surface-50 dark:bg-surface-800/40 p-4 space-y-3 relative"
                                                >
                                                    <button
                                                        type="button"
                                                        onClick={() => setProjects(prev => prev.filter((_, i) => i !== idx))}
                                                        className="absolute top-3 right-3 p-1.5 rounded-lg hover:bg-red-50 dark:hover:bg-red-500/10 transition-colors"
                                                    >
                                                        <Trash2 className="w-4 h-4 text-red-500" />
                                                    </button>

                                                    <div className="grid grid-cols-1 sm:grid-cols-2 gap-3 pr-8">
                                                        <div>
                                                            <label className="label-text">프로젝트명</label>
                                                            <input
                                                                value={proj.title}
                                                                onChange={e => {
                                                                    const updated = [...projects];
                                                                    updated[idx].title = e.target.value;
                                                                    setProjects(updated);
                                                                }}
                                                                className="input-field"
                                                                placeholder="CareerFit"
                                                            />
                                                        </div>
                                                        <div>
                                                            <label className="label-text">진행 상태</label>
                                                            <select
                                                                value={proj.status}
                                                                onChange={e => {
                                                                    const updated = [...projects];
                                                                    updated[idx].status = e.target.value;
                                                                    // 진행중으로 변경 시 종료일 자동 비우기
                                                                    if (e.target.value === '진행중') {
                                                                        updated[idx].endedAt = '';
                                                                    }
                                                                    setProjects(updated);
                                                                }}
                                                                className="input-field"
                                                            >
                                                                <option value="진행중">진행중</option>
                                                                <option value="완료">완료</option>
                                                            </select>
                                                        </div>
                                                        <div>
                                                            <label className="label-text">시작일</label>
                                                            <input
                                                                type="date"
                                                                value={proj.startedAt}
                                                                onChange={e => {
                                                                    const updated = [...projects];
                                                                    updated[idx].startedAt = e.target.value;
                                                                    setProjects(updated);
                                                                }}
                                                                className="input-field"
                                                            />
                                                        </div>
                                                        <div>
                                                            <label className="label-text">종료일{proj.status === '진행중' ? ' (진행중)' : ''}</label>
                                                            <input
                                                                type="date"
                                                                value={proj.endedAt}
                                                                disabled={proj.status === '진행중'}
                                                                onChange={e => {
                                                                    const updated = [...projects];
                                                                    updated[idx].endedAt = e.target.value;
                                                                    setProjects(updated);
                                                                }}
                                                                className="input-field disabled:opacity-40 disabled:cursor-not-allowed"
                                                            />
                                                        </div>
                                                    </div>

                                                    <div>
                                                        <label className="label-text">GitHub URL (선택)</label>
                                                        <input
                                                            type="url"
                                                            value={proj.githubUrl}
                                                            onChange={e => {
                                                                const updated = [...projects];
                                                                updated[idx].githubUrl = e.target.value;
                                                                setProjects(updated);
                                                            }}
                                                            className="input-field"
                                                            placeholder="https://github.com/taeyoung/CareerFit"
                                                        />
                                                    </div>
                                                    <div>
                                                        <label className="label-text">프로젝트 설명</label>
                                                        <textarea
                                                            value={proj.description}
                                                            onChange={e => {
                                                                const updated = [...projects];
                                                                updated[idx].description = e.target.value;
                                                                setProjects(updated);
                                                            }}
                                                            rows={4}
                                                            className="input-field resize-y min-h-[100px]"
                                                            placeholder="금융권 취업 분석 서비스. Spring Boot + FastAPI + PostgreSQL + pgvector RAG..."
                                                        />
                                                    </div>
                                                </motion.div>
                                            ))}
                                        </div>
                                    )}
                                </div>

                                {/* ── 수상 섹션 ── */}
                                <div className="rounded-xl border border-surface-200 dark:border-surface-700 p-5">
                                    <div className="flex items-center justify-between mb-4">
                                        <div className="flex items-center gap-2">
                                            <Award className="w-5 h-5 text-accent-500" />
                                            <h3 className="font-semibold text-surface-800 dark:text-surface-200">수상 내역</h3>
                                        </div>
                                        <button
                                            type="button"
                                            onClick={() => setAwards(prev => [...prev, {
                                                title: '', institution: '', grade: '', awardedAt: '',
                                            }])}
                                            className="flex items-center gap-1.5 text-sm font-medium text-brand-600 dark:text-brand-400 hover:text-brand-700 dark:hover:text-brand-300 transition-colors"
                                        >
                                            <Plus className="w-4 h-4" />
                                            추가하기
                                        </button>
                                    </div>

                                    {awards.length === 0 ? (
                                        <p className="text-sm text-surface-400 dark:text-surface-500 text-center py-4">
                                            수상 내역이 있으면 위의 "추가하기" 버튼을 클릭하세요
                                        </p>
                                    ) : (
                                        <div className="space-y-3">
                                            {awards.map((award, idx) => (
                                                <motion.div
                                                    key={idx}
                                                    initial={{ opacity: 0, y: -8 }}
                                                    animate={{ opacity: 1, y: 0 }}
                                                    className="flex items-end gap-3"
                                                >
                                                    <div className="flex-1">
                                                        {idx === 0 && <label className="label-text">수상명</label>}
                                                        <input
                                                            value={award.title}
                                                            onChange={e => {
                                                                const updated = [...awards];
                                                                updated[idx].title = e.target.value;
                                                                setAwards(updated);
                                                            }}
                                                            className="input-field"
                                                            placeholder="해커톤 최우수상"
                                                        />
                                                    </div>
                                                    <div className="w-40 flex-shrink-0">
                                                        {idx === 0 && <label className="label-text">주관기관</label>}
                                                        <input
                                                            value={award.institution}
                                                            onChange={e => {
                                                                const updated = [...awards];
                                                                updated[idx].institution = e.target.value;
                                                                setAwards(updated);
                                                            }}
                                                            className="input-field"
                                                            placeholder="카카오"
                                                        />
                                                    </div>
                                                    <div className="w-28 flex-shrink-0">
                                                        {idx === 0 && <label className="label-text">등급</label>}
                                                        <input
                                                            value={award.grade}
                                                            onChange={e => {
                                                                const updated = [...awards];
                                                                updated[idx].grade = e.target.value;
                                                                setAwards(updated);
                                                            }}
                                                            className="input-field"
                                                            placeholder="최우수"
                                                        />
                                                    </div>
                                                    <div className="w-40 flex-shrink-0">
                                                        {idx === 0 && <label className="label-text">수상일</label>}
                                                        <input
                                                            type="date"
                                                            value={award.awardedAt}
                                                            onChange={e => {
                                                                const updated = [...awards];
                                                                updated[idx].awardedAt = e.target.value;
                                                                setAwards(updated);
                                                            }}
                                                            className="input-field"
                                                        />
                                                    </div>
                                                    <button
                                                        type="button"
                                                        onClick={() => setAwards(prev => prev.filter((_, i) => i !== idx))}
                                                        className="p-2.5 rounded-lg hover:bg-red-50 dark:hover:bg-red-500/10 transition-colors flex-shrink-0 mb-0.5"
                                                    >
                                                        <Trash2 className="w-4 h-4 text-red-500" />
                                                    </button>
                                                </motion.div>
                                            ))}
                                        </div>
                                    )}
                                </div>

                                <div className="flex gap-3 justify-end">
                                    <button type="button" onClick={() => setStep('qualification')} className="btn-secondary text-sm">← 이전</button>
                                    <button type="submit" disabled={submitting} className="btn-primary text-sm">{submitting ? '저장중...' : '완료 ✓'}</button>
                                </div>
                            </form>
                        )}
                    </motion.div>
                )}
            </AnimatePresence>

            {/* 히스토리 목록 */}
            {loading ? (
                <div className="space-y-3">
                    {Array.from({ length: 3 }).map((_, i) => (
                        <div key={i} className="glass-card p-5 animate-pulse">
                            <div className="h-5 bg-surface-200 dark:bg-surface-700 rounded w-1/3 mb-2" />
                            <div className="h-4 bg-surface-200 dark:bg-surface-700 rounded w-1/4" />
                        </div>
                    ))}
                </div>
            ) : history.length === 0 && !showForm ? (
                <EmptyState
                    icon={<FileText className="w-8 h-8 text-surface-400" />}
                    title="등록된 스펙이 없습니다"
                    description="새 스펙을 등록하여 핏 분석을 시작하세요"
                    action={
                        <button onClick={() => setShowForm(true)} className="btn-primary text-sm flex items-center gap-2">
                            <Plus className="w-4 h-4" /> 스펙 등록하기
                        </button>
                    }
                />
            ) : (
                <div className="space-y-3">
                    {history.map((ver) => (
                        <div key={ver.id} className="glass-card overflow-hidden">
                            <div
                                className="p-5 flex items-center justify-between cursor-pointer hover:bg-surface-50/50 dark:hover:bg-surface-700/30 transition-colors"
                                onClick={() => handleExpand(ver.id)}
                            >
                                <div className="flex items-center gap-4">
                                    <div className="w-10 h-10 rounded-lg bg-brand-100 dark:bg-brand-500/15 flex items-center justify-center">
                                        <span className="text-sm font-bold text-brand-600 dark:text-brand-400">v{ver.versionNo}</span>
                                    </div>
                                    <div>
                                        <p className="font-semibold text-surface-900 dark:text-surface-100">{ver.university}</p>
                                        <p className="text-xs text-surface-500 dark:text-surface-400 flex items-center gap-1 mt-0.5">
                                            <Clock className="w-3 h-3" />
                                            {new Date(ver.createdAt).toLocaleDateString('ko-KR')}
                                        </p>
                                    </div>
                                </div>
                                <div className="flex items-center gap-2">
                                    <button
                                        onClick={(e) => { e.stopPropagation(); setDeleteTarget(ver.id); }}
                                        className="p-1.5 rounded-lg hover:bg-red-50 dark:hover:bg-red-500/10 transition-colors"
                                    >
                                        <Trash2 className="w-4 h-4 text-red-500" />
                                    </button>
                                    {expandedId === ver.id
                                        ? <ChevronDown className="w-5 h-5 text-surface-400" />
                                        : <ChevronRight className="w-5 h-5 text-surface-400" />
                                    }
                                </div>
                            </div>

                            <AnimatePresence>
                                {expandedId === ver.id && expandedDetail && (
                                    <motion.div
                                        initial={{ height: 0, opacity: 0 }}
                                        animate={{ height: 'auto', opacity: 1 }}
                                        exit={{ height: 0, opacity: 0 }}
                                        className="border-t border-surface-200 dark:border-surface-700 overflow-hidden"
                                    >
                                        <div className="p-5 space-y-4">
                                            <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
                                                <InfoItem icon={GraduationCap} label="학력" value={`${expandedDetail.university} (${expandedDetail.education})`} />
                                                <InfoItem icon={GraduationCap} label="학점" value={expandedDetail.gpa ? `${expandedDetail.gpa}/4.5` : '—'} />
                                                <InfoItem icon={Code} label="희망 직무" value={expandedDetail.wantedJobs?.map(j => j.jobType).join(', ') || '—'} />
                                                <InfoItem icon={Code} label="기술스택" value={expandedDetail.skills?.map(s => s.skillName).join(', ') || '—'} />
                                            </div>
                                            {expandedDetail.certificates && expandedDetail.certificates.length > 0 && (
                                                <div>
                                                    <p className="text-xs font-semibold text-surface-500 dark:text-surface-400 uppercase mb-2 flex items-center gap-1">
                                                        <Award className="w-3.5 h-3.5" /> 자격증
                                                    </p>
                                                    <div className="flex flex-wrap gap-2">
                                                        {expandedDetail.certificates.map(c => (
                                                            <span key={c.id} className={`text-xs px-2.5 py-1 rounded-full font-medium ${
                                                                c.status === '취득'
                                                                    ? 'bg-accent-100 dark:bg-accent-500/15 text-accent-700 dark:text-accent-400'
                                                                    : 'bg-surface-200 dark:bg-surface-700 text-surface-500 dark:text-surface-400'
                                                            }`}>
                                                                {c.certName} {c.status === '취득' && '✅'}
                                                            </span>
                                                        ))}
                                                    </div>
                                                </div>
                                            )}
                                            {expandedDetail.languages && expandedDetail.languages.length > 0 && (
                                                <div>
                                                    <p className="text-xs font-semibold text-surface-500 dark:text-surface-400 uppercase mb-2 flex items-center gap-1">
                                                        <Languages className="w-3.5 h-3.5" /> 어학
                                                    </p>
                                                    <div className="flex flex-wrap gap-2">
                                                        {expandedDetail.languages.map(l => (
                                                            <span key={l.id} className="text-xs px-2.5 py-1 rounded-full bg-brand-100 dark:bg-brand-500/15 text-brand-700 dark:text-brand-400">
                                                                {l.langType} {l.score || l.grade}
                                                            </span>
                                                        ))}
                                                    </div>
                                                </div>
                                            )}
                                            {expandedDetail.interns && expandedDetail.interns.length > 0 && (
                                                <div>
                                                    <p className="text-xs font-semibold text-surface-500 dark:text-surface-400 uppercase mb-2 flex items-center gap-1">
                                                        <Briefcase className="w-3.5 h-3.5" /> 경력
                                                    </p>
                                                    {expandedDetail.interns.map(i => (
                                                        <p key={i.id} className="text-sm text-surface-700 dark:text-surface-300">
                                                            {i.companyName}
                                                            {i.employmentType && ` · ${employmentTypeLabel(i.employmentType)}`}
                                                            {i.role && ` — ${i.role}`}
                                                        </p>
                                                    ))}
                                                </div>
                                            )}
                                        </div>
                                    </motion.div>
                                )}
                            </AnimatePresence>
                        </div>
                    ))}
                </div>
            )}

            <ConfirmModal
                isOpen={deleteTarget !== null}
                title="스펙 버전 삭제"
                message="이 스펙 버전을 삭제하시겠습니까? 관련 분석 데이터에 영향을 줄 수 있습니다."
                confirmLabel="삭제"
                variant="danger"
                onConfirm={handleDelete}
                onCancel={() => setDeleteTarget(null)}
                isLoading={deleting}
            />
        </div>
    );
}

function InfoItem({ icon: Icon, label, value }: { icon: React.ElementType; label: string; value: string }) {
    return (
        <div className="flex items-start gap-2">
            <Icon className="w-4 h-4 text-surface-400 mt-0.5 flex-shrink-0" />
            <div>
                <p className="text-xs text-surface-400 dark:text-surface-500">{label}</p>
                <p className="text-sm font-medium text-surface-800 dark:text-surface-200">{value}</p>
            </div>
        </div>
    );
}

// 고용 형태 코드 → 한글 라벨 변환
function employmentTypeLabel(type: string): string {
    switch (type) {
        case 'INTERN':    return '인턴';
        case 'FULL_TIME': return '정규직';
        case 'CONTRACT':  return '계약직';
        default:          return type;
    }
}
