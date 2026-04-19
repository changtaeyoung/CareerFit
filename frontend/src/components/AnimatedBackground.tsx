export default function AnimatedBackground() {
    return (
        <div className="absolute inset-0 overflow-hidden pointer-events-none">
            <div
                className="absolute -top-40 -right-40 w-96 h-96
                           bg-brand-300 dark:bg-brand-600 rounded-full
                           mix-blend-multiply dark:mix-blend-soft-light
                           filter blur-3xl opacity-30 dark:opacity-20 animate-blob"
            />
            <div
                className="absolute -bottom-40 -left-40 w-96 h-96
                           bg-accent-300 dark:bg-accent-600 rounded-full
                           mix-blend-multiply dark:mix-blend-soft-light
                           filter blur-3xl opacity-30 dark:opacity-20 animate-blob"
                style={{ animationDelay: '2s' }}
            />
            <div
                className="absolute top-1/2 left-1/2 w-96 h-96
                           bg-purple-200 dark:bg-purple-700 rounded-full
                           mix-blend-multiply dark:mix-blend-soft-light
                           filter blur-3xl opacity-20 dark:opacity-10 animate-blob"
                style={{ animationDelay: '4s' }}
            />
        </div>
    );
}
