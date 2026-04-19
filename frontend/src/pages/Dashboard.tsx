import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { motion } from 'framer-motion';
import {
    BarChart3, Building2, Briefcase, FileText,
    ArrowRight, TrendingUp, Clock, Sparkles,
    Star, X
} from 'lucide-react';
import { useAuthStore } from '../store/authStore';
import { getAnalysisHistory } from '../api/analysis';
import type { AnalysisHistoryResponse } from '../api/analysis';
import { getSpecHistory } from '../api/user';
import type { SpecVersionSummary } from '../api/user';
import { useBookmarkStore } from '../store/bookmarkStore';
import ScoreGauge from '../components/ScoreGauge';

const stagger = {
    container: { transition: { staggerChildren: 0.08 } },
    item: { initial: { opacity: 0, y: 20 }, animate: { opacity: 1, y: 0 } },
};

export default function Dashboard() {
    const { user } = useAuthStore();
    const navigate = useNavigate();
    const { bookmarks, removeBookmark } = useBookmarkStore();
    const [recentAnalysis, setRecentAnalysis] = useState<AnalysisHistoryResponse[]>([]);
    const [specHistory, setSpecHistory] = useState<SpecVersionSummary[]>([]);
    const [, setLoading] = useState(true);

    useEffect(() => {
        const fetchData = async () => {
            try {
                const [analysis, specs] = await Promise.allSettled([
                    getAnalysisHistory(),
                    getSpecHistory(),
                ]);
                if (analysis.status === 'fulfilled') setRecentAnalysis(analysis.value.slice(0, 3));
                if (specs.status === 'fulfilled') setSpecHistory(specs.value);
            } catch {
                // 에러 시 빈 상태 유지
            } finally {
                setLoading(false);
            }
        };
        fetchData();
    }, []);

    const quickActions = [
        { icon: FileText, label: '스펙 등록', desc: '내 스펙을 등록하세요', path: '/spec', color: 'from-brand-500 to-brand-600' },
        { icon: Building2, label: '기업 탐색', desc: '금융권 기업을 탐색하세요', path: '/companies', color: 'from-accent-500 to-accent-600' },
        { icon: Briefcase, label: '채용공고', desc: '최신 채용공고 확인', path: '/postings', color: 'from-purple-500 to-purple-600' },
        { icon: BarChart3, label: '핏 분석', desc: '나의 적합도를 분석하세요', path: '/analysis/history', color: 'from-amber-500 to-orange-500' },
    ];

    const getGreeting = () => {
        const hour = new Date().getHours();
        if (hour < 12) return '좋은 아침이에요';
        if (hour < 18) return '좋은 오후에요';
        return '좋은 저녁이에요';
    };

    // 스펙 완성도 계산 (최신 버전 기준)
    const latestSpec = specHistory.length > 0 ? specHistory[specHistory.length - 1] : null;
    const specSteps = [
        { label: '기본 정보', done: !!latestSpec },
        { label: '자격증/어학', done: latestSpec ? (latestSpec as any).hasCertificates ?? true : false },
        { label: '경력/프로젝트', done: latestSpec ? (latestSpec as any).hasExperience ?? true : false },
    ];
    const specCompletionPercent = latestSpec ? Math.round((specSteps.filter(s => s.done).length / specSteps.length) * 100) : 0;

    const companyBookmarks = bookmarks.filter(b => b.type === 'company');
    const postingBookmarks = bookmarks.filter(b => b.type === 'posting');

    return (
        <motion.div variants={stagger.container} initial="initial" animate="animate" className="space-y-8">
            {/* 환영 헤더 */}
            <motion.div variants={stagger.item} className="flex items-start justify-between">
                <div>
                    <h1 className="text-3xl font-bold text-surface-900 dark:text-surface-50 tracking-tight">
                        {getGreeting()}, <span className="text-brand-600 dark:text-brand-400">{user?.name}</span>님 👋
                    </h1>
                    <p className="mt-2 text-surface-500 dark:text-surface-400">
                        오늘도 커리어 성장을 위한 한 발짝을 내딛어보세요
                    </p>
                </div>
            </motion.div>

            {/* 신규 유저 온보딩 가이드 */}
            {specHistory.length === 0 && recentAnalysis.length === 0 && (
                <motion.div variants={stagger.item} className="glass-card p-6 border-2 border-dashed border-brand-300 dark:border-brand-500/30">
                    <h2 className="font-bold text-lg text-surface-900 dark:text-surface-50 mb-1">🚀 시작하기</h2>
                    <p className="text-sm text-surface-500 dark:text-surface-400 mb-5">3단계만 따라하면 나의 취업 적합도를 분석할 수 있어요</p>
                    <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
                        {[
                            { step: 1, title: '내 스펙 등록', desc: '학력·기술스택·자격증을 등록하세요', path: '/spec', color: 'from-brand-500 to-brand-600' },
                            { step: 2, title: '채용공고 탐색', desc: '관심 기업의 채용공고를 확인하세요', path: '/postings', color: 'from-accent-500 to-accent-600' },
                            { step: 3, title: '핏 분석 실행', desc: '공고와 나의 적합도를 AI로 분석하세요', path: '/analysis/history', color: 'from-purple-500 to-purple-600' },
                        ].map(item => (
                            <button
                                key={item.step}
                                onClick={() => navigate(item.path)}
                                className="text-left p-4 rounded-xl bg-surface-50 dark:bg-surface-800/50 hover:bg-surface-100 dark:hover:bg-surface-700/50 transition-colors group"
                            >
                                <div className={`w-8 h-8 rounded-lg bg-gradient-to-br ${item.color} flex items-center justify-center text-white text-sm font-bold mb-3 group-hover:scale-110 transition-transform`}>
                                    {item.step}
                                </div>
                                <p className="font-semibold text-sm text-surface-900 dark:text-surface-100">{item.title}</p>
                                <p className="text-xs text-surface-500 dark:text-surface-400 mt-1">{item.desc}</p>
                            </button>
                        ))}
                    </div>
                </motion.div>
            )}

            {/* 핏 점수 요약 + 스펙 현황 */}
            <motion.div variants={stagger.item} className="grid grid-cols-1 md:grid-cols-2 gap-6">
                {/* 최근 핏 점수 */}
                <div className="glass-card p-6">
                    <div className="flex items-center gap-2 mb-4">
                        <TrendingUp className="w-5 h-5 text-brand-500" />
                        <h2 className="font-semibold text-surface-800 dark:text-surface-200">최근 핏 분석</h2>
                    </div>
                    {recentAnalysis.length > 0 ? (
                        <div className="flex items-center gap-6">
                            <ScoreGauge score={recentAnalysis[0].totalScore} size={100} strokeWidth={8} />
                            <div className="flex-1 min-w-0">
                                <p className="font-semibold text-surface-900 dark:text-surface-100 truncate">
                                    {recentAnalysis[0].companyName}
                                </p>
                                <p className="text-sm text-surface-500 dark:text-surface-400 truncate mt-0.5">
                                    {recentAnalysis[0].postingTitle}
                                </p>
                                <button
                                    onClick={() => navigate(`/analysis/${recentAnalysis[0].reportId}`)}
                                    className="mt-3 text-sm text-brand-600 dark:text-brand-400 font-medium hover:underline flex items-center gap-1"
                                >
                                    상세 보기 <ArrowRight className="w-4 h-4" />
                                </button>
                            </div>
                        </div>
                    ) : (
                        <div className="text-center py-6">
                            <Sparkles className="w-10 h-10 text-surface-300 dark:text-surface-600 mx-auto mb-3" />
                            <p className="text-sm text-surface-500 dark:text-surface-400">아직 분석 결과가 없습니다</p>
                            <button
                                onClick={() => navigate('/postings')}
                                className="mt-2 text-sm text-brand-600 dark:text-brand-400 font-medium hover:underline"
                            >
                                채용공고에서 분석 시작하기
                            </button>
                        </div>
                    )}
                </div>

                {/* 스펙 현황 + 완성도 바 */}
                <div className="glass-card p-6">
                    <div className="flex items-center gap-2 mb-4">
                        <FileText className="w-5 h-5 text-accent-500" />
                        <h2 className="font-semibold text-surface-800 dark:text-surface-200">내 스펙 현황</h2>
                    </div>
                    {specHistory.length > 0 ? (
                        <div>
                            <div className="flex items-center gap-4 mb-4">
                                <div className="w-12 h-12 rounded-xl bg-accent-100 dark:bg-accent-500/15 flex items-center justify-center">
                                    <span className="text-lg font-bold text-accent-600 dark:text-accent-400">
                                        v{specHistory[specHistory.length - 1].versionNo}
                                    </span>
                                </div>
                                <div>
                                    <p className="font-semibold text-surface-900 dark:text-surface-100">
                                        {specHistory[specHistory.length - 1].university}
                                    </p>
                                    <p className="text-sm text-surface-500 dark:text-surface-400">
                                        {specHistory[specHistory.length - 1].education}
                                    </p>
                                </div>
                            </div>

                            {/* 스펙 완성도 */}
                            <div className="mb-3">
                                <div className="flex items-center justify-between text-xs mb-1.5">
                                    <span className="font-medium text-surface-600 dark:text-surface-400">스펙 완성도</span>
                                    <span className="font-bold text-accent-600 dark:text-accent-400">{specCompletionPercent}%</span>
                                </div>
                                <div className="w-full h-2 bg-surface-200 dark:bg-surface-700 rounded-full overflow-hidden">
                                    <motion.div
                                        initial={{ width: 0 }}
                                        animate={{ width: `${specCompletionPercent}%` }}
                                        transition={{ duration: 0.8, ease: 'easeOut' }}
                                        className="h-full bg-gradient-to-r from-accent-500 to-brand-500 rounded-full"
                                    />
                                </div>
                                <div className="flex gap-2 mt-2">
                                    {specSteps.map(s => (
                                        <span key={s.label} className={`text-[10px] font-medium px-2 py-0.5 rounded-full ${
                                            s.done
                                                ? 'bg-accent-100 dark:bg-accent-500/15 text-accent-700 dark:text-accent-400'
                                                : 'bg-surface-100 dark:bg-surface-700 text-surface-400 dark:text-surface-500'
                                        }`}>
                                            {s.done ? '✅' : '⬜'} {s.label}
                                        </span>
                                    ))}
                                </div>
                            </div>

                            <p className="text-xs text-surface-400 dark:text-surface-500 flex items-center gap-1">
                                <Clock className="w-3.5 h-3.5" />
                                총 {specHistory.length}개 버전 등록됨
                            </p>
                            <button
                                onClick={() => navigate('/spec')}
                                className="mt-3 text-sm text-accent-600 dark:text-accent-400 font-medium hover:underline flex items-center gap-1"
                            >
                                스펙 관리 <ArrowRight className="w-4 h-4" />
                            </button>
                        </div>
                    ) : (
                        <div className="text-center py-6">
                            <FileText className="w-10 h-10 text-surface-300 dark:text-surface-600 mx-auto mb-3" />
                            <p className="text-sm text-surface-500 dark:text-surface-400">등록된 스펙이 없습니다</p>
                            <button
                                onClick={() => navigate('/spec')}
                                className="mt-2 text-sm text-accent-600 dark:text-accent-400 font-medium hover:underline"
                            >
                                스펙 등록하기
                            </button>
                        </div>
                    )}
                </div>
            </motion.div>

            {/* 관심 목록 위젯 */}
            {(companyBookmarks.length > 0 || postingBookmarks.length > 0) && (
                <motion.div variants={stagger.item}>
                    <h2 className="section-title mb-4 flex items-center gap-2">
                        <Star className="w-5 h-5 text-amber-400 fill-amber-400" />
                        관심 목록
                    </h2>
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                        {/* 관심 기업 */}
                        {companyBookmarks.length > 0 && (
                            <div className="glass-card p-5">
                                <h3 className="text-sm font-semibold text-surface-600 dark:text-surface-400 mb-3 flex items-center gap-1.5">
                                    <Building2 className="w-4 h-4" />
                                    관심 기업 ({companyBookmarks.length})
                                </h3>
                                <div className="space-y-2">
                                    {companyBookmarks.slice(0, 5).map(b => (
                                        <div
                                            key={b.id}
                                            onClick={() => navigate(`/companies/${b.id}`)}
                                            className="flex items-center justify-between p-3 rounded-xl
                                                       bg-surface-50 dark:bg-surface-700/50 hover:bg-surface-100 dark:hover:bg-surface-700
                                                       cursor-pointer transition-colors"
                                        >
                                            <div>
                                                <p className="text-sm font-medium text-surface-800 dark:text-surface-200">{b.name}</p>
                                                {b.subText && (
                                                    <p className="text-xs text-surface-500 dark:text-surface-400">{b.subText}</p>
                                                )}
                                            </div>
                                            <button
                                                onClick={(e) => { e.stopPropagation(); removeBookmark(b.id, 'company'); }}
                                                className="p-1 rounded-lg hover:bg-surface-200 dark:hover:bg-surface-600 transition-colors"
                                            >
                                                <X className="w-3.5 h-3.5 text-surface-400" />
                                            </button>
                                        </div>
                                    ))}
                                </div>
                            </div>
                        )}

                        {/* 관심 공고 */}
                        {postingBookmarks.length > 0 && (
                            <div className="glass-card p-5">
                                <h3 className="text-sm font-semibold text-surface-600 dark:text-surface-400 mb-3 flex items-center gap-1.5">
                                    <Briefcase className="w-4 h-4" />
                                    관심 공고 ({postingBookmarks.length})
                                </h3>
                                <div className="space-y-2">
                                    {postingBookmarks.slice(0, 5).map(b => {
                                        const daysLeft = b.deadline
                                            ? Math.ceil((new Date(b.deadline).getTime() - Date.now()) / (1000 * 60 * 60 * 24))
                                            : null;
                                        return (
                                            <div
                                                key={b.id}
                                                onClick={() => navigate(`/postings/${b.id}`)}
                                                className="flex items-center justify-between p-3 rounded-xl
                                                           bg-surface-50 dark:bg-surface-700/50 hover:bg-surface-100 dark:hover:bg-surface-700
                                                           cursor-pointer transition-colors"
                                            >
                                                <div className="flex-1 min-w-0">
                                                    <p className="text-sm font-medium text-surface-800 dark:text-surface-200 truncate">{b.name}</p>
                                                    <div className="flex items-center gap-2 mt-0.5">
                                                        <p className="text-xs text-surface-500 dark:text-surface-400">{b.subText}</p>
                                                        {daysLeft !== null && daysLeft >= 0 && (
                                                            <span className={`text-[10px] font-bold ${
                                                                daysLeft <= 3 ? 'text-red-500' : daysLeft <= 7 ? 'text-amber-500' : 'text-surface-400'
                                                            }`}>
                                                                D-{daysLeft}
                                                            </span>
                                                        )}
                                                    </div>
                                                </div>
                                                <button
                                                    onClick={(e) => { e.stopPropagation(); removeBookmark(b.id, 'posting'); }}
                                                    className="p-1 rounded-lg hover:bg-surface-200 dark:hover:bg-surface-600 transition-colors"
                                                >
                                                    <X className="w-3.5 h-3.5 text-surface-400" />
                                                </button>
                                            </div>
                                        );
                                    })}
                                </div>
                            </div>
                        )}
                    </div>
                </motion.div>
            )}

            {/* 빠른 액션 */}
            <motion.div variants={stagger.item}>
                <h2 className="section-title mb-4">빠른 시작</h2>
                <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
                    {quickActions.map((action, i) => (
                        <motion.button
                            key={action.label}
                            onClick={() => navigate(action.path)}
                            whileHover={{ scale: 1.03, y: -2 }}
                            whileTap={{ scale: 0.98 }}
                            initial={{ opacity: 0, y: 20 }}
                            animate={{ opacity: 1, y: 0 }}
                            transition={{ delay: 0.1 * i }}
                            className="glass-card p-5 text-left group"
                        >
                            <div className={`w-10 h-10 rounded-xl bg-gradient-to-br ${action.color}
                                            flex items-center justify-center mb-3 shadow-md
                                            group-hover:scale-110 transition-transform`}>
                                <action.icon className="w-5 h-5 text-white" />
                            </div>
                            <p className="font-semibold text-surface-800 dark:text-surface-200 text-sm">
                                {action.label}
                            </p>
                            <p className="text-xs text-surface-500 dark:text-surface-400 mt-0.5">
                                {action.desc}
                            </p>
                        </motion.button>
                    ))}
                </div>
            </motion.div>

            {/* 최근 분석 히스토리 */}
            {recentAnalysis.length > 1 && (
                <motion.div variants={stagger.item}>
                    <div className="flex items-center justify-between mb-4">
                        <h2 className="section-title">최근 분석 결과</h2>
                        <button
                            onClick={() => navigate('/analysis/history')}
                            className="text-sm text-brand-600 dark:text-brand-400 font-medium hover:underline flex items-center gap-1"
                        >
                            모두 보기 <ArrowRight className="w-4 h-4" />
                        </button>
                    </div>
                    <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                        {recentAnalysis.map((item, i) => (
                            <motion.div
                                key={item.reportId}
                                initial={{ opacity: 0, y: 20 }}
                                animate={{ opacity: 1, y: 0 }}
                                transition={{ delay: 0.1 * i }}
                                onClick={() => navigate(`/analysis/${item.reportId}`)}
                                className="glass-card-hover p-5"
                            >
                                <div className="flex items-center justify-between mb-3">
                                    <ScoreGauge score={item.totalScore} size={56} strokeWidth={5} showLabel={false} />
                                    <span className={`text-xs font-medium px-2 py-1 rounded-full ${
                                        item.requiredAllMet
                                            ? 'bg-accent-100 dark:bg-accent-500/15 text-accent-700 dark:text-accent-400'
                                            : 'bg-red-100 dark:bg-red-500/15 text-red-700 dark:text-red-400'
                                    }`}>
                                        {item.requiredAllMet ? '필수 충족' : '필수 미충족'}
                                    </span>
                                </div>
                                <p className="font-semibold text-surface-900 dark:text-surface-100 text-sm truncate">
                                    {item.companyName}
                                </p>
                                <p className="text-xs text-surface-500 dark:text-surface-400 truncate mt-0.5">
                                    {item.postingTitle}
                                </p>
                            </motion.div>
                        ))}
                    </div>
                </motion.div>
            )}
        </motion.div>
    );
}