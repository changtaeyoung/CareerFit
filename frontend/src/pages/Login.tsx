import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { motion } from 'framer-motion';
import { Mail, Lock, ArrowRight, AlertCircle, Sun, Moon } from 'lucide-react';
import { login } from '../api/auth';
import { useAuthStore } from '../store/authStore';
import { useThemeStore } from '../store/themeStore';
import AnimatedBackground from '../components/AnimatedBackground';

export default function Login() {
    const navigate = useNavigate();
    const setAuth = useAuthStore((state) => state.setAuth);
    const { theme, toggleTheme } = useThemeStore();

    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [isLoading, setIsLoading] = useState(false);
    const [error, setError] = useState('');

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setError('');
        setIsLoading(true);

        try {
            const response = await login({ email, password });
            setAuth(
                { userId: response.userId, email: response.email, name: response.name },
                response.accessToken,
                response.refreshToken
            );
            navigate('/dashboard');
        } catch (err: any) {
            setError(err.response?.data?.message || '로그인에 실패했습니다');
        } finally {
            setIsLoading(false);
        }
    };

    return (
        <div className="min-h-screen bg-gradient-to-br from-surface-50 via-brand-50 to-accent-50
                         dark:from-surface-950 dark:via-surface-900 dark:to-surface-900
                         flex items-center justify-center p-4 relative overflow-hidden transition-colors duration-500">
            <AnimatedBackground />

            {/* 다크/라이트 모드 토글 */}
            <button
                onClick={toggleTheme}
                className="absolute top-4 right-4 z-20 p-2.5 rounded-xl
                           bg-white/60 dark:bg-surface-800/60 backdrop-blur-lg
                           border border-surface-200/60 dark:border-surface-700/60
                           hover:bg-white/80 dark:hover:bg-surface-700/80
                           text-surface-600 dark:text-surface-300
                           transition-all duration-200 shadow-sm"
            >
                {theme === 'light' ? <Moon className="w-5 h-5" /> : <Sun className="w-5 h-5" />}
            </button>

            <motion.div
                initial={{ opacity: 0, y: 30 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ duration: 0.6, ease: 'easeOut' }}
                className="relative w-full max-w-md z-10"
            >
                <div className="glass-card p-10">
                    {/* 로고 + 제목 */}
                    <div className="text-center mb-10">
                        <motion.div
                            initial={{ scale: 0, rotate: -180 }}
                            animate={{ scale: 1, rotate: 0 }}
                            transition={{ delay: 0.2, type: 'spring', stiffness: 150 }}
                            className="inline-block mb-6"
                        >
                            <div className="w-20 h-20 rounded-3xl flex items-center justify-center shadow-lg shadow-brand-500/30 overflow-hidden bg-white dark:bg-surface-800">
                                <img src="/logo.png" alt="CareerFit" className="w-16 h-16 object-contain" />
                            </div>
                        </motion.div>
                        <motion.h1
                            initial={{ opacity: 0 }}
                            animate={{ opacity: 1 }}
                            transition={{ delay: 0.4 }}
                            className="text-4xl font-bold text-surface-900 dark:text-surface-50 mb-3 tracking-tight"
                        >
                            CareerFit
                        </motion.h1>
                        <motion.p
                            initial={{ opacity: 0 }}
                            animate={{ opacity: 1 }}
                            transition={{ delay: 0.5 }}
                            className="text-surface-500 dark:text-surface-400 text-sm"
                        >
                            금융권 IT 취업, 당신의 핏을 분석합니다
                        </motion.p>
                    </div>

                    {/* 에러 메시지 */}
                    {error && (
                        <motion.div
                            initial={{ opacity: 0, y: -10 }}
                            animate={{ opacity: 1, y: 0 }}
                            className="mb-6 p-4 bg-red-50 dark:bg-red-500/10 border border-red-200 dark:border-red-500/20 rounded-xl flex items-center gap-2"
                        >
                            <AlertCircle className="w-5 h-5 text-red-500 flex-shrink-0" />
                            <p className="text-sm text-red-700 dark:text-red-400">{error}</p>
                        </motion.div>
                    )}

                    {/* 폼 */}
                    <form onSubmit={handleSubmit} className="space-y-5">
                        <motion.div
                            initial={{ opacity: 0, x: -20 }}
                            animate={{ opacity: 1, x: 0 }}
                            transition={{ delay: 0.6 }}
                        >
                            <label className="label-text">이메일</label>
                            <div className="relative group">
                                <Mail className="absolute left-4 top-1/2 -translate-y-1/2 w-5 h-5 text-surface-400 group-focus-within:text-brand-600 dark:group-focus-within:text-brand-400 transition-colors" />
                                <input
                                    id="login-email"
                                    type="email"
                                    value={email}
                                    onChange={(e) => setEmail(e.target.value)}
                                    placeholder="example@email.com"
                                    className="input-field pl-12"
                                    required
                                />
                            </div>
                        </motion.div>

                        <motion.div
                            initial={{ opacity: 0, x: -20 }}
                            animate={{ opacity: 1, x: 0 }}
                            transition={{ delay: 0.7 }}
                        >
                            <label className="label-text">비밀번호</label>
                            <div className="relative group">
                                <Lock className="absolute left-4 top-1/2 -translate-y-1/2 w-5 h-5 text-surface-400 group-focus-within:text-brand-600 dark:group-focus-within:text-brand-400 transition-colors" />
                                <input
                                    id="login-password"
                                    type="password"
                                    value={password}
                                    onChange={(e) => setPassword(e.target.value)}
                                    placeholder="••••••••"
                                    className="input-field pl-12"
                                    required
                                />
                            </div>
                        </motion.div>

                        <motion.button
                            id="login-submit"
                            type="submit"
                            disabled={isLoading}
                            whileHover={{ scale: 1.01 }}
                            whileTap={{ scale: 0.99 }}
                            initial={{ opacity: 0, y: 10 }}
                            animate={{ opacity: 1, y: 0 }}
                            transition={{ delay: 0.8 }}
                            className="w-full btn-primary flex items-center justify-center gap-2 group mt-8"
                        >
                            {isLoading ? (
                                <motion.div
                                    animate={{ rotate: 360 }}
                                    transition={{ duration: 1, repeat: Infinity, ease: 'linear' }}
                                    className="w-5 h-5 border-2 border-white border-t-transparent rounded-full"
                                />
                            ) : (
                                <>
                                    로그인
                                    <ArrowRight className="w-5 h-5 group-hover:translate-x-1 transition-transform" />
                                </>
                            )}
                        </motion.button>
                    </form>

                    {/* 회원가입 링크 */}
                    <motion.div
                        initial={{ opacity: 0 }}
                        animate={{ opacity: 1 }}
                        transition={{ delay: 0.9 }}
                        className="mt-8 text-center"
                    >
                        <p className="text-surface-500 dark:text-surface-400 text-sm">
                            아직 계정이 없으신가요?{' '}
                            <Link
                                to="/signup"
                                className="text-brand-600 dark:text-brand-400 hover:text-brand-700 dark:hover:text-brand-300 font-semibold transition-colors"
                            >
                                회원가입
                            </Link>
                        </p>
                    </motion.div>
                </div>
            </motion.div>
        </div>
    );
}