import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { motion } from 'framer-motion';
import { MapPin, Users, Search, Star } from 'lucide-react';
import { getCompanyList } from '../api/company';
import type { CompanyResponse } from '../api/company';
import FilterBar from '../components/FilterBar';
import Pagination from '../components/Pagination';
import EmptyState from '../components/EmptyState';
import { useBookmarkStore } from '../store/bookmarkStore';
import { toast } from '../store/toastStore';

const industryOptions = [
    { value: '은행', label: '은행' },
    { value: '보험', label: '보험' },
    { value: '카드', label: '카드' },
    { value: '증권', label: '증권' },
    { value: '공기업', label: '공기업' },
    { value: '핀테크', label: '핀테크' },
    { value: '기타', label: '기타' },
];

const typeOptions = [
    { value: '대기업', label: '대기업' },
    { value: '공기업', label: '공기업' },
    { value: '금융지주', label: '금융지주' },
    { value: '중견기업', label: '중견기업' },
    { value: '스타트업', label: '스타트업' },
];

export default function Companies() {
    const navigate = useNavigate();
    const { isBookmarked, toggleBookmark } = useBookmarkStore();
    const [companies, setCompanies] = useState<CompanyResponse[]>([]);
    const [loading, setLoading] = useState(true);
    const [page, setPage] = useState(1);
    const [industry, setIndustry] = useState('');
    const [companyType, setCompanyType] = useState('');
    const pageSize = 12;

    useEffect(() => {
        const fetchCompanies = async () => {
            setLoading(true);
            try {
                const data = await getCompanyList(page, pageSize, industry || undefined, companyType || undefined);
                setCompanies(data);
            } catch {
                setCompanies([]);
            } finally {
                setLoading(false);
            }
        };
        fetchCompanies();
    }, [page, industry, companyType]);

    const getIndustryIcon = (ind: string) => {
        const icons: Record<string, string> = {
            '은행': '🏦', '보험': '🛡️', '카드': '💳', '증권': '📈',
            '공기업': '🏛️', '핀테크': '💡', '기타': '🏢',
        };
        return icons[ind] || '🏢';
    };

    return (
        <div className="space-y-6">
            <div>
                <h1 className="section-title">기업 탐색</h1>
                <p className="section-subtitle mt-1">금융권 기업의 정보를 탐색하고 관심 기업을 찾아보세요</p>
            </div>

            <FilterBar
                filters={[
                    { label: '업종 전체', value: industry, options: industryOptions, onChange: (v) => { setIndustry(v); setPage(1); } },
                    { label: '유형 전체', value: companyType, options: typeOptions, onChange: (v) => { setCompanyType(v); setPage(1); } },
                ]}
            />

            {loading ? (
                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                    {Array.from({ length: 6 }).map((_, i) => (
                        <div key={i} className="glass-card p-6 animate-pulse">
                            <div className="w-12 h-12 rounded-xl bg-surface-200 dark:bg-surface-700 mb-4" />
                            <div className="h-5 bg-surface-200 dark:bg-surface-700 rounded w-2/3 mb-2" />
                            <div className="h-4 bg-surface-200 dark:bg-surface-700 rounded w-1/2" />
                        </div>
                    ))}
                </div>
            ) : companies.length === 0 ? (
                <EmptyState
                    icon={<Search className="w-8 h-8 text-surface-400" />}
                    title="기업을 찾을 수 없습니다"
                    description="필터 조건을 변경해보세요"
                />
            ) : (
                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                    {companies.map((company, i) => (
                        <motion.div
                            key={company.id}
                            initial={{ opacity: 0, y: 20 }}
                            animate={{ opacity: 1, y: 0 }}
                            transition={{ delay: 0.05 * i }}
                            onClick={() => navigate(`/companies/${company.id}`)}
                            className="glass-card-hover p-6"
                        >
                            <div className="flex items-start gap-4">
                                <div className="w-12 h-12 rounded-xl bg-surface-100 dark:bg-surface-700
                                                flex items-center justify-center text-2xl flex-shrink-0">
                                    {getIndustryIcon(company.industry)}
                                </div>
                                <div className="flex-1 min-w-0">
                                    <h3 className="font-semibold text-surface-900 dark:text-surface-100 truncate">
                                        {company.name}
                                    </h3>
                                    <div className="flex items-center gap-2 mt-1">
                                        <span className="text-xs font-medium px-2 py-0.5 rounded-full
                                                         bg-brand-100 dark:bg-brand-500/15
                                                         text-brand-700 dark:text-brand-400">
                                            {company.industry}
                                        </span>
                                        <span className="text-xs text-surface-500 dark:text-surface-400">
                                            {company.companyType}
                                        </span>
                                    </div>
                                </div>
                                <button
                                    onClick={(e) => {
                                        e.stopPropagation();
                                        toggleBookmark({ id: company.id, type: 'company', name: company.name, subText: company.industry });
                                        toast.info(isBookmarked(company.id, 'company') ? '관심 기업에서 해제했습니다' : '관심 기업에 추가했습니다');
                                    }}
                                    className="p-1.5 rounded-lg hover:bg-amber-50 dark:hover:bg-amber-500/10 transition-colors flex-shrink-0"
                                >
                                    <Star className={`w-4.5 h-4.5 transition-colors ${
                                        isBookmarked(company.id, 'company')
                                            ? 'fill-amber-400 text-amber-400'
                                            : 'text-surface-300 dark:text-surface-600'
                                    }`} />
                                </button>
                            </div>

                            <div className="mt-4 flex items-center gap-4 text-xs text-surface-500 dark:text-surface-400">
                                <span className="flex items-center gap-1">
                                    <MapPin className="w-3.5 h-3.5" />
                                    {company.location || '—'}
                                </span>
                                {company.employeeCount && (
                                    <span className="flex items-center gap-1">
                                        <Users className="w-3.5 h-3.5" />
                                        {company.employeeCount.toLocaleString()}명
                                    </span>
                                )}
                            </div>
                        </motion.div>
                    ))}
                </div>
            )}

            <Pagination currentPage={page} totalPages={10} onPageChange={setPage} />
        </div>
    );
}
