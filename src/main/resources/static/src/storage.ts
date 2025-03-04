interface StorageItem<T> {
    value: T;
    expiresAt: number;
}

class LocalStorageWithTTL {
    setItem<T>(key: string, value: T, ttl: number): void {
        const now = Date.now();
        const item: StorageItem<T> = {
            value,
            expiresAt: now + ttl * 1000,
        };
        localStorage.setItem(key, JSON.stringify(item));
    }

    getItem<T>(key: string): T | null {
        const itemStr = localStorage.getItem(key);
        if (!itemStr) return null;

        const item: StorageItem<T> = JSON.parse(itemStr);
        if (Date.now() > item.expiresAt) {
            localStorage.removeItem(key);
            return null;
        }

        return item.value;
    }

    removeItem(key: string): void {
        localStorage.removeItem(key);
    }

    clear(): void {
        localStorage.clear();
    }
}

export const storage = new LocalStorageWithTTL();