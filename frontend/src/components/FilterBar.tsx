import { ChevronDown } from 'lucide-react';

interface FilterOption {
    value: string;
    label: string;
}

interface FilterBarProps {
    filters: {
        label: string;
        value: string;
        options: FilterOption[];
        onChange: (value: string) => void;
    }[];
}

export default function FilterBar({ filters }: FilterBarProps) {
    return (
        <div className="flex flex-wrap gap-3 mb-6">
            {filters.map((filter) => (
                <div key={filter.label} className="relative">
                    <select
                        value={filter.value}
                        onChange={(e) => filter.onChange(e.target.value)}
                        className="appearance-none pl-4 pr-10 py-2.5 rounded-xl text-sm font-medium
                                   bg-white dark:bg-surface-800
                                   border border-surface-200 dark:border-surface-700
                                   text-surface-700 dark:text-surface-300
                                   focus:ring-2 focus:ring-brand-500/20 focus:border-brand-500
                                   transition-all duration-200 outline-none cursor-pointer"
                    >
                        <option value="">{filter.label}</option>
                        {filter.options.map((opt) => (
                            <option key={opt.value} value={opt.value}>
                                {opt.label}
                            </option>
                        ))}
                    </select>
                    <ChevronDown className="absolute right-3.5 top-1/2 -translate-y-1/2 w-4 h-4 text-surface-400 pointer-events-none" />
                </div>
            ))}
        </div>
    );
}
