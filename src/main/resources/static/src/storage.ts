interface StorageItem<T> {
    value: T;
    expiresAt: number;
}

class SessionStorageWithTTL {
    setItem<T>(key: string, value: T, ttl: number): void {
        const now = Date.now();
        const item: StorageItem<T> = {
            value,
            expiresAt: now + ttl * 1000,
        };
        sessionStorage.setItem(key, JSON.stringify(item));
    }

    getItem<T>(key: string): T | null {
        const itemStr = sessionStorage.getItem(key);
        if (!itemStr) return null;

        const item: StorageItem<T> = JSON.parse(itemStr);
        if (Date.now() > item.expiresAt) {
            sessionStorage.removeItem(key);
            return null;
        }

        return item.value;
    }

    removeItem(key: string): void {
        sessionStorage.removeItem(key);
    }

    clear(): void {
        sessionStorage.clear();
    }
}

export const storage = new SessionStorageWithTTL();