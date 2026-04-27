import { create } from 'zustand';

export interface BookmarkItem {
    id: number;
    type: 'company' | 'posting';
    name: string;       // 기업명 or 공고 제목
    subText?: string;   // 업종 or 기업명
    deadline?: string;  // 공고 마감일
}

interface BookmarkState {
    bookmarks: BookmarkItem[];
    addBookmark: (item: BookmarkItem) => void;
    removeBookmark: (id: number, type: 'company' | 'posting') => void;
    isBookmarked: (id: number, type: 'company' | 'posting') => boolean;
    toggleBookmark: (item: BookmarkItem) => void;
}

const loadBookmarks = (): BookmarkItem[] => {
    try {
        const stored = localStorage.getItem('bookmarks');
        return stored ? JSON.parse(stored) : [];
    } catch {
        return [];
    }
};

const saveBookmarks = (bookmarks: BookmarkItem[]) => {
    localStorage.setItem('bookmarks', JSON.stringify(bookmarks));
};

export const useBookmarkStore = create<BookmarkState>((set, get) => ({
    bookmarks: loadBookmarks(),

    addBookmark: (item) => {
        const next = [...get().bookmarks, item];
        saveBookmarks(next);
        set({ bookmarks: next });
    },

    removeBookmark: (id, type) => {
        const next = get().bookmarks.filter(
            (b) => !(b.id === id && b.type === type)
        );
        saveBookmarks(next);
        set({ bookmarks: next });
    },

    isBookmarked: (id, type) =>
        get().bookmarks.some((b) => b.id === id && b.type === type),

    toggleBookmark: (item) => {
        if (get().isBookmarked(item.id, item.type)) {
            get().removeBookmark(item.id, item.type);
        } else {
            get().addBookmark(item);
        }
    },
}));
