import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { motion } from 'framer-motion';
import {
    ArrowLeft, Globe, MapPin, Users, Calendar,
    Eye, Target, Briefcase, Building2, ExternalLink, Wallet
} from 'lucide-react';
import { getCompanyDetail, getPostingsByCompany } from '../api/company';
import type { CompanyDetailResponse, JobPostingResponse } from '../api/company';
import StatusBadge from '../components/StatusBadge';

export default function CompanyDetail() {
    const { id } = useParams<{ id: string }>();
    const navigate = useNavigate();
    const [company, setCompany] = useState<CompanyDetailResponse | null>(null);
    const [postings, setPostings] = useState<JobPostingResponse[]>([]);
    const [tab, setTab] = useState<'overview' | 'postings'>('overview');
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        if (!id) return;
        const fetchData = async () => {
            setLoading(true);
            try {
                const [compData, postData] = await Promise.all([
                    getCompanyDetail(Number(id)),
                    getPostingsByCompany(Number(id)),
                ]);
                setCompany(compData);
                setPostings(postData);
            } catch {
                navigate('/companies');
            } finally {
                setLoading(false);
            }
        };
        fetchData();
    }, [id]);

    if (loading || !company) {
        return (
            <div className="space-y-6 animate-pulse">
                <div className="h-8 bg-surface-200 dark:bg-surface-700 rounded w-48" />
                <div className="glass-card p-8">
                    <div className="h-10 bg-surface-200 dark:bg-surface-700 rounded w-1/3 mb-4" />
                    <div className="h-6 bg-surface-200 dark:bg-surface-700 rounded w-2/3" />
                </div>
            </div>
        );
    }

    const infoItems = [
        { icon: Building2, label: '업종', value: `${company.industry} · ${company.companyType}` },
        { icon: MapPin, label: '소재지', value: company.location },
        { icon: Users, label: '임직원 수', value: company.employeeCount ? `${company.employeeCount.toLocaleString()}명` : '—' },
        { icon: Calendar, label: '설립 연도', value: company.foundedYear ? `${company.foundedYear}년` : '—' },
    ];

    return (
        <div className="space-y-6">
            {/* 뒤로가기 */}
            <button
                onClick={() => navigate('/companies')}
                className="btn-ghost flex items-center gap-2 -ml-2"
            >
                <ArrowLeft className="w-4 h-4" />
                기업 목록
            </button>

            {/* 기업 헤더 */}
            <motion.div
                initial={{ opacity: 0, y: 20 }}
                animate={{ opacity: 1, y: 0 }}
                className="glass-card p-8"
            >
                <div className="flex flex-col md:flex-row md:items-center gap-6">
                    <div className="w-16 h-16 rounded-2xl bg-gradient-to-br from-brand-500 to-accent-500
                                    flex items-center justify-center shadow-lg shadow-brand-500/20 flex-shrink-0">
                        <span className="text-white text-xl font-bold">
                            {company.name.charAt(0)}
                        </span>
                    </div>
                    <div className="flex-1">
                        <h1 className="text-2xl font-bold text-surface-900 dark:text-surface-50">
                            {company.name}
                        </h1>
                        <div className="flex flex-wrap items-center gap-3 mt-2">
                            <span className="text-sm font-medium px-3 py-1 rounded-full
                                             bg-brand-100 dark:bg-brand-500/15
                                             text-brand-700 dark:text-brand-400">
                                {company.industry}
                            </span>
                            <span className="text-sm text-surface-500 dark:text-surface-400">{company.companyType}</span>
                            {company.isPublic && (
                                <span className="text-xs font-medium px-2 py-0.5 rounded-full bg-accent-100 dark:bg-accent-500/15 text-accent-700 dark:text-accent-400">
                                    상장기업
                                </span>
                            )}
                        </div>
                    </div>
                    {company.website && (
                        <a
                            href={company.website}
                            target="_blank"
                            rel="noopener noreferrer"
                            className="btn-secondary flex items-center gap-2 text-sm self-start"
                        >
                            <Globe className="w-4 h-4" />
                            홈페이지
                            <ExternalLink className="w-3.5 h-3.5" />
                        </a>
                    )}
                </div>

                <div className="grid grid-cols-2 md:grid-cols-4 gap-4 mt-6 pt-6 border-t border-surface-200 dark:border-surface-700">
                    {infoItems.map((item) => (
                        <div key={item.label} className="flex items-center gap-3">
                            <div className="w-9 h-9 rounded-lg bg-surface-100 dark:bg-surface-700 flex items-center justify-center">
                                <item.icon className="w-4 h-4 text-surface-500 dark:text-surface-400" />
                            </div>
                            <div>
                                <p className="text-xs text-surface-400 dark:text-surface-500">{item.label}</p>
                                <p className="text-sm font-medium text-surface-800 dark:text-surface-200">{item.value}</p>
                            </div>
                        </div>
                    ))}
                </div>
            </motion.div>

            {/* 탭 */}
            <div className="flex gap-1 bg-surface-100 dark:bg-surface-800 rounded-xl p-1">
                {(['overview', 'postings'] as const).map((t) => (
                    <button
                        key={t}
                        onClick={() => setTab(t)}
                        className={`flex-1 py-2.5 text-sm font-medium rounded-lg transition-all
                            ${tab === t
                                ? 'bg-white dark:bg-surface-700 text-surface-900 dark:text-surface-100 shadow-sm'
                                : 'text-surface-500 dark:text-surface-400 hover:text-surface-700 dark:hover:text-surface-300'
                            }`}
                    >
                        {t === 'overview' ? '기업 개요' : `채용공고 (${postings.length})`}
                    </button>
                ))}
            </div>

            {/* 탭 콘텐츠 */}
            {tab === 'overview' ? (
                <motion.div
                    initial={{ opacity: 0 }}
                    animate={{ opacity: 1 }}
                    className="grid grid-cols-1 md:grid-cols-2 gap-6"
                >
                    {/* 연봉 카드 — 항상 노출, 데이터 없으면 '-' 표시 */}
                    <div className="glass-card p-6 md:col-span-2">
                        <div className="flex items-center gap-2 mb-4">
                            <Wallet className="w-5 h-5 text-emerald-500" />
                            <h3 className="font-semibold text-surface-800 dark:text-surface-200">연봉 정보</h3>
                        </div>
                        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                            <SalaryBlock
                                label="신입 초봉"
                                amount={company.salary?.entrySalary ?? null}
                                year={company.salary?.entryYear ?? null}
                                source={company.salary?.entrySource ?? null}
                            />
                            <SalaryBlock
                                label="평균 연봉"
                                amount={company.salary?.averageSalary ?? null}
                                year={company.salary?.averageYear ?? null}
                                source={company.salary?.averageSource ?? null}
                            />
                        </div>
                        {!company.salary && (
                            <p className="text-xs text-surface-400 dark:text-surface-500 mt-3">
                                ※ 연봉 데이터가 아직 수집되지 않았습니다
                            </p>
                        )}
                    </div>

                    {company.vision && (
                        <div className="glass-card p-6">
                            <div className="flex items-center gap-2 mb-3">
                                <Eye className="w-5 h-5 text-brand-500" />
                                <h3 className="font-semibold text-surface-800 dark:text-surface-200">비전</h3>
                            </div>
                            <p className="text-sm text-surface-600 dark:text-surface-400 leading-relaxed">
                                {company.vision}
                            </p>
                        </div>
                    )}

                    {company.talentImage && (
                        <div className="glass-card p-6">
                            <div className="flex items-center gap-2 mb-3">
                                <Target className="w-5 h-5 text-accent-500" />
                                <h3 className="font-semibold text-surface-800 dark:text-surface-200">인재상</h3>
                            </div>
                            <p className="text-sm text-surface-600 dark:text-surface-400 leading-relaxed">
                                {company.talentImage}
                            </p>
                        </div>
                    )}

                    {company.businessOverview && (
                        <div className="glass-card p-6 md:col-span-2">
                            <div className="flex items-center gap-2 mb-3">
                                <Briefcase className="w-5 h-5 text-purple-500" />
                                <h3 className="font-semibold text-surface-800 dark:text-surface-200">사업 개요</h3>
                            </div>
                            <p className="text-sm text-surface-600 dark:text-surface-400 leading-relaxed">
                                {company.businessOverview}
                            </p>
                        </div>
                    )}

                    {!company.vision && !company.talentImage && !company.businessOverview && (
                        <div className="md:col-span-2 text-center py-12">
                            <p className="text-surface-500 dark:text-surface-400">기업 상세 정보가 아직 등록되지 않았습니다</p>
                        </div>
                    )}
                </motion.div>
            ) : (
                <motion.div
                    initial={{ opacity: 0 }}
                    animate={{ opacity: 1 }}
                    className="space-y-3"
                >
                    {postings.length === 0 ? (
                        <div className="text-center py-12">
                            <p className="text-surface-500 dark:text-surface-400">등록된 채용공고가 없습니다</p>
                        </div>
                    ) : (
                        postings.map((posting) => (
                            <div
                                key={posting.id}
                                onClick={() => navigate(`/postings/${posting.id}`)}
                                className="glass-card-hover p-5 flex items-center gap-4"
                            >
                                <div className="flex-1 min-w-0">
                                    <p className="font-semibold text-surface-900 dark:text-surface-100 truncate">
                                        {posting.title}
                                    </p>
                                    <div className="flex items-center gap-3 mt-1.5 text-xs text-surface-500 dark:text-surface-400">
                                        <span>{posting.jobType}</span>
                                        <span>{posting.season}</span>
                                        {posting.deadline && <span>~ {posting.deadline}</span>}
                                    </div>
                                </div>
                                <StatusBadge status={posting.status} />
                            </div>
                        ))
                    )}
                </motion.div>
            )}
        </div>
    );
}

// 연봉 블록 한 칸 (신입 초봉 / 평균 연봉 각각 하나씩)
// amount가 null이면 '-'를 크게 표시하여 데이터 부재를 명확히 알림
function SalaryBlock({
    label,
    amount,
    year,
    source,
}: {
    label: string;
    amount: number | null;
    year: number | null;
    source: string | null;
}) {
    // 만원 단위 금액을 "5,000만원" 형태로 포매팅. null이면 '-' 표시
    const formatted = amount != null
        ? `${amount.toLocaleString()}만원`
        : '-';

    const sourceLabel = source ? sourceLabelMap[source] ?? source : null;

    return (
        <div className="rounded-xl bg-surface-50 dark:bg-surface-800/40 p-4">
            <p className="text-xs text-surface-500 dark:text-surface-400">{label}</p>
            <p className="text-2xl font-bold text-surface-900 dark:text-surface-50 mt-1">
                {formatted}
            </p>
            {amount != null && year && sourceLabel && (
                <p className="text-xs text-surface-400 dark:text-surface-500 mt-1.5">
                    {year}년 · {sourceLabel}
                </p>
            )}
        </div>
    );
}

// 출처 코드 → 한글 라벨 변환
// 사용자에게는 "ALIO_API" 같은 코드 대신 "ALIO 공시"처럼 읽기 쉬운 라벨을 노출
const sourceLabelMap: Record<string, string> = {
    ALIO_API:   'ALIO 공시',
    ALIO_CRAWL: 'ALIO 공시',
    DART_API:   'DART 공시',
    POSTING:    '채용공고',
    MANUAL:     '관리자 입력',
};
