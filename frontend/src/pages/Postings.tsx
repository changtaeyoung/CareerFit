import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { motion } from 'framer-motion';
import { Briefcase, Calendar, Building2, Clock, Search, Star } from 'lucide-react';
import { getPostings } from '../api/company';
import type { JobPostingResponse } from '../api/company';
import FilterBar from '../components/FilterBar';
import Pagination from '../components/Pagination';
import StatusBadge from '../components/StatusBadge';
import EmptyState from '../components/EmptyState';
import { useBookmarkStore } from '../store/bookmarkStore';
import { toast } from '../store/toastStore';

const jobTypeOptions = [
    { value: 'IT', label: 'IT' },
    { value: 'BACKEND', label: '백엔드' },
    { value: 'FRONTEND', label: '프론트엔드' },
    { value: 'FULLSTACK', label: '풀스택' },
    { value: 'DATA', label: '데이터' },
    { value: 'AI', label: 'AI' },
    { value: 'INFRA', label: '인프라' },
    { value: 'SECURITY', label: '보안' },
    { value: 'DIGITAL', label: '디지털' },
    { value: 'ETC', label: '기타' },
];

const statusOptions = [
    { value: 'ACTIVE', label: '진행중' },
    { value: 'CLOSED', label: '마감' },
    { value: 'SCHEDULED', label: '예정' },
];

export default function Postings() {
    const navigate = useNavigate();
    const { isBookmarked, toggleBookmark } = useBookmarkStore();
    const [postings, setPostings] = useState<JobPostingResponse[]>([]);
    const [loading, setLoading] = useState(true);
    const [page, setPage] = useState(1);
    const [jobType, setJobType] = useState('');
    const [status, setStatus] = useState('');
    const pageSize = 10;

    useEffect(() => {
        const fetchPostings = async () => {
            setLoading(true);
            try {
                const data = await getPostings(page, pageSize, jobType || undefined, status || undefined);
                setPostings(data);
            } catch {
                setPostings([]);
            } finally {
                setLoading(false);
            }
        };
        fetchPostings();
    }, [page, jobType, status]);

    const getDaysRemaining = (deadline: string) => {
        if (!deadline) return null;
        const diff = Math.ceil((new Date(deadline).getTime() - Date.now()) / (1000 * 60 * 60 * 24));
        return diff;
    };

    return (
        <div className="space-y-6">
            <div>
                <h1 className="section-title">채용공고</h1>
                <p className="section-subtitle mt-1">금융권 IT 채용공고를 확인하고 나의 적합도를 분석해보세요</p>
            </div>

            <FilterBar
                filters={[
                    { label: '직무 전체', value: jobType, options: jobTypeOptions, onChange: (v) => { setJobType(v); setPage(1); } },
                    { label: '상태 전체', value: status, options: statusOptions, onChange: (v) => { setStatus(v); setPage(1); } },
                ]}
            />

            {loading ? (
                <div className="space-y-3">
                    {Array.from({ length: 5 }).map((_, i) => (
                        <div key={i} className="glass-card p-6 animate-pulse">
                            <div className="h-5 bg-surface-200 dark:bg-surface-700 rounded w-2/3 mb-3" />
                            <div className="h-4 bg-surface-200 dark:bg-surface-700 rounded w-1/3" />
                        </div>
                    ))}
                </div>
            ) : postings.length === 0 ? (
                <EmptyState
                    icon={<Search className="w-8 h-8 text-surface-400" />}
                    title="채용공고를 찾을 수 없습니다"
                    description="필터 조건을 변경해보세요"
                />
            ) : (
                <div className="space-y-3">
                    {postings.map((posting, i) => {
                        const daysLeft = getDaysRemaining(posting.deadline);
                        return (
                            <motion.div
                                key={posting.id}
                                initial={{ opacity: 0, y: 16 }}
                                animate={{ opacity: 1, y: 0 }}
                                transition={{ delay: 0.04 * i }}
                                onClick={() => navigate(`/postings/${posting.id}`)}
                                className="glass-card-hover p-6"
                            >
                                <div className="flex items-start justify-between gap-4">
                                    <div className="flex-1 min-w-0">
                                        <h3 className="font-semibold text-surface-900 dark:text-surface-100 truncate">
                                            {posting.title}
                                        </h3>
                                        <div className="flex flex-wrap items-center gap-3 mt-2">
                                            <span className="flex items-center gap-1 text-sm text-surface-600 dark:text-surface-400">
                                                <Building2 className="w-4 h-4" />
                                                {posting.companyName}
                                            </span>
                                            <span className="flex items-center gap-1 text-xs text-surface-500 dark:text-surface-400">
                                                <Briefcase className="w-3.5 h-3.5" />
                                                {posting.jobType}
                                            </span>
                                            <span className="flex items-center gap-1 text-xs text-surface-500 dark:text-surface-400">
                                                <Calendar className="w-3.5 h-3.5" />
                                                {posting.season}
                                            </span>
                                        </div>
                                    </div>

                                    <div className="flex flex-col items-end gap-2 flex-shrink-0">
                                        <div className="flex items-center gap-2">
                                            <button
                                                onClick={(e) => {
                                                    e.stopPropagation();
                                                    toggleBookmark({ id: posting.id, type: 'posting', name: posting.title, subText: posting.companyName, deadline: posting.deadline });
                                                    toast.info(isBookmarked(posting.id, 'posting') ? '관심 공고에서 해제했습니다' : '관심 공고에 추가했습니다');
                                                }}
                                                className="p-1.5 rounded-lg hover:bg-amber-50 dark:hover:bg-amber-500/10 transition-colors"
                                            >
                                                <Star className={`w-4.5 h-4.5 transition-colors ${
                                                    isBookmarked(posting.id, 'posting')
                                                        ? 'fill-amber-400 text-amber-400'
                                                        : 'text-surface-300 dark:text-surface-600'
                                                }`} />
                                            </button>
                                            <StatusBadge status={posting.status} />
                                        </div>
                                        {posting.status === 'ACTIVE' && daysLeft !== null && daysLeft >= 0 && (
                                            <span className={`flex items-center gap-1 text-xs font-medium ${
                                                daysLeft <= 3
                                                    ? 'text-red-600 dark:text-red-400'
                                                    : daysLeft <= 7
                                                        ? 'text-amber-600 dark:text-amber-400'
                                                        : 'text-surface-500 dark:text-surface-400'
                                            }`}>
                                                <Clock className="w-3.5 h-3.5" />
                                                D-{daysLeft}
                                            </span>
                                        )}
                                    </div>
                                </div>
                            </motion.div>
                        );
                    })}
                </div>
            )}

            <Pagination currentPage={page} totalPages={10} onPageChange={setPage} />
        </div>
    );
}
