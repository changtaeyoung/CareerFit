import { useState } from 'react';
import { NavLink, useNavigate } from 'react-router-dom';
import { motion, AnimatePresence } from 'framer-motion';
import {
    LayoutDashboard, Building2, Briefcase, BarChart3,
    FileText, PenLine, User, Sun, Moon, LogOut, Menu, X, ChevronDown
} from 'lucide-react';
import { useAuthStore } from '../store/authStore';
import { useThemeStore } from '../store/themeStore';

const navGroups = [
    {
        items: [
            { path: '/dashboard', icon: LayoutDashboard, label: '대시보드' },
        ],
    },
    {
        label: '탐색',
        items: [
            { path: '/companies', icon: Building2, label: '기업 탐색' },
            { path: '/postings', icon: Briefcase, label: '채용공고' },
        ],
    },
    {
        label: '관리',
        items: [
            { path: '/spec', icon: FileText, label: '내 스펙' },
            { path: '/analysis/history', icon: BarChart3, label: '핏 분석' },
            { path: '/cover-letters', icon: PenLine, label: '자기소개서' },
        ],
    },
    {
        label: '설정',
        items: [
            { path: '/mypage', icon: User, label: '마이페이지' },
        ],
    },
];

export default function Layout({ children }: { children: React.ReactNode }) {
    const [sidebarOpen, setSidebarOpen] = useState(false);
    const [profileOpen, setProfileOpen] = useState(false);
    const { user, logout } = useAuthStore();
    const { theme, toggleTheme } = useThemeStore();
    const navigate = useNavigate();

    const handleLogout = () => {
        logout();
        navigate('/login');
    };

    const userInitial = user?.name?.charAt(0) || 'U';

    return (
        <div className="min-h-screen bg-surface-50 dark:bg-surface-900 transition-colors duration-300">
            {/* 모바일 사이드바 오버레이 */}
            <AnimatePresence>
                {sidebarOpen && (
                    <motion.div
                        initial={{ opacity: 0 }}
                        animate={{ opacity: 1 }}
                        exit={{ opacity: 0 }}
                        className="fixed inset-0 bg-black/40 backdrop-blur-sm z-40 lg:hidden"
                        onClick={() => setSidebarOpen(false)}
                    />
                )}
            </AnimatePresence>

            {/* 사이드바 */}
            <aside
                className={`fixed top-0 left-0 h-full w-64 z-50 transform transition-transform duration-300 ease-out
                    lg:translate-x-0
                    ${sidebarOpen ? 'translate-x-0' : '-translate-x-full'}
                    bg-white/80 dark:bg-surface-800/90 backdrop-blur-xl
                    border-r border-surface-200/60 dark:border-surface-700/60`}
            >
                {/* 로고 — 클릭 시 대시보드 이동 */}
                <div
                    className="flex items-center gap-3 px-6 h-16 border-b border-surface-200/60 dark:border-surface-700/60 cursor-pointer"
                    onClick={() => { navigate('/dashboard'); setSidebarOpen(false); }}
                >
                    <div className="w-9 h-9 rounded-xl flex items-center justify-center shadow-md shadow-brand-500/20 overflow-hidden">
                        <img src="/logo.png" alt="CareerFit" className="w-9 h-9 object-contain" />
                    </div>
                    <span className="text-lg font-bold text-surface-900 dark:text-surface-50 tracking-tight">
                        CareerFit
                    </span>
                    <button
                        onClick={(e) => { e.stopPropagation(); setSidebarOpen(false); }}
                        className="ml-auto lg:hidden p-1 rounded-lg hover:bg-surface-100 dark:hover:bg-surface-700"
                    >
                        <X className="w-5 h-5 text-surface-500" />
                    </button>
                </div>

                {/* 그룹별 네비게이션 */}
                <nav className="px-3 py-4 space-y-1 overflow-y-auto" style={{ maxHeight: 'calc(100vh - 8rem)' }}>
                    {navGroups.map((group, gi) => (
                        <div key={gi}>
                            {group.label && (
                                <p className="px-4 pt-4 pb-1.5 text-[11px] font-semibold uppercase tracking-wider text-surface-400 dark:text-surface-500">
                                    {group.label}
                                </p>
                            )}
                            {group.items.map((item) => (
                                <NavLink
                                    key={item.path}
                                    to={item.path}
                                    onClick={() => setSidebarOpen(false)}
                                    className={({ isActive }) =>
                                        `flex items-center gap-3 px-4 py-2.5 rounded-xl text-sm font-medium transition-all duration-200 group
                                        ${isActive
                                            ? 'bg-brand-50 dark:bg-brand-500/10 text-brand-700 dark:text-brand-400 shadow-sm'
                                            : 'text-surface-600 dark:text-surface-400 hover:bg-surface-100 dark:hover:bg-surface-700/50 hover:text-surface-900 dark:hover:text-surface-200'
                                        }`
                                    }
                                >
                                    <item.icon className="w-5 h-5 flex-shrink-0 group-hover:scale-110 transition-transform" />
                                    {item.label}
                                </NavLink>
                            ))}
                        </div>
                    ))}
                </nav>

                {/* 하단 다크모드 토글 */}
                <div className="absolute bottom-0 left-0 right-0 p-4 border-t border-surface-200/60 dark:border-surface-700/60">
                    <button
                        id="theme-toggle"
                        onClick={toggleTheme}
                        className="flex items-center gap-3 w-full px-4 py-2.5 rounded-xl text-sm font-medium
                                   text-surface-600 dark:text-surface-400
                                   hover:bg-surface-100 dark:hover:bg-surface-700/50
                                   transition-all duration-200"
                    >
                        {theme === 'light' ? (
                            <Moon className="w-5 h-5" />
                        ) : (
                            <Sun className="w-5 h-5" />
                        )}
                        {theme === 'light' ? '다크 모드' : '라이트 모드'}
                    </button>
                </div>
            </aside>

            {/* 메인 콘텐츠 영역 */}
            <div className="lg:ml-64 min-h-screen flex flex-col">
                {/* 탑바 */}
                <header className="sticky top-0 z-30 h-16 flex items-center justify-between px-4 lg:px-8
                                   bg-white/60 dark:bg-surface-800/60 backdrop-blur-xl
                                   border-b border-surface-200/60 dark:border-surface-700/60">
                    <button
                        id="mobile-menu-toggle"
                        onClick={() => setSidebarOpen(true)}
                        className="lg:hidden p-2 rounded-xl hover:bg-surface-100 dark:hover:bg-surface-700 transition-colors"
                    >
                        <Menu className="w-5 h-5 text-surface-600 dark:text-surface-400" />
                    </button>

                    <div className="flex-1" />

                    {/* 유저 프로필 드롭다운 */}
                    <div className="relative">
                        <button
                            id="profile-dropdown"
                            onClick={() => setProfileOpen(!profileOpen)}
                            className="flex items-center gap-2 pl-2 pr-3 py-1.5 rounded-xl
                                       hover:bg-surface-100 dark:hover:bg-surface-700/50 transition-colors"
                        >
                            <div className="w-8 h-8 bg-gradient-to-br from-brand-500 to-accent-500 rounded-lg
                                            flex items-center justify-center text-white text-sm font-bold">
                                {userInitial}
                            </div>
                            <span className="hidden sm:block text-sm font-medium text-surface-700 dark:text-surface-300">
                                {user?.name}
                            </span>
                            <ChevronDown className={`w-4 h-4 text-surface-400 transition-transform ${profileOpen ? 'rotate-180' : ''}`} />
                        </button>

                        <AnimatePresence>
                            {profileOpen && (
                                <motion.div
                                    initial={{ opacity: 0, y: -8, scale: 0.95 }}
                                    animate={{ opacity: 1, y: 0, scale: 1 }}
                                    exit={{ opacity: 0, y: -8, scale: 0.95 }}
                                    transition={{ duration: 0.15 }}
                                    className="absolute right-0 mt-2 w-48 py-2
                                               bg-white dark:bg-surface-800 rounded-xl
                                               shadow-lg border border-surface-200 dark:border-surface-700"
                                >
                                    <div className="px-4 py-2 border-b border-surface-100 dark:border-surface-700">
                                        <p className="text-sm font-semibold text-surface-900 dark:text-surface-100">
                                            {user?.name}
                                        </p>
                                        <p className="text-xs text-surface-500 truncate">{user?.email}</p>
                                    </div>
                                    <button
                                        onClick={() => { navigate('/mypage'); setProfileOpen(false); }}
                                        className="w-full text-left px-4 py-2 text-sm text-surface-600 dark:text-surface-400
                                                   hover:bg-surface-50 dark:hover:bg-surface-700 transition-colors"
                                    >
                                        마이페이지
                                    </button>
                                    <button
                                        id="logout-button"
                                        onClick={handleLogout}
                                        className="w-full text-left px-4 py-2 text-sm text-red-600 dark:text-red-400
                                                   hover:bg-red-50 dark:hover:bg-red-500/10 transition-colors flex items-center gap-2"
                                    >
                                        <LogOut className="w-4 h-4" />
                                        로그아웃
                                    </button>
                                </motion.div>
                            )}
                        </AnimatePresence>
                    </div>
                </header>

                {/* 페이지 콘텐츠 */}
                <main className="flex-1 p-4 lg:p-8">
                    <motion.div
                        initial={{ opacity: 0, y: 12 }}
                        animate={{ opacity: 1, y: 0 }}
                        transition={{ duration: 0.3, ease: 'easeOut' }}
                    >
                        {children}
                    </motion.div>
                </main>
            </div>
        </div>
    );
}
