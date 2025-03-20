package dev.davidpalves.cookbetter.auth.configuration;

import java.time.LocalDateTime;

public record AuthToken(String username, String userId, LocalDateTime refreshDate,LocalDateTime expirationDate) {

    public boolean isExpired() {
        return expirationDate.isBefore(LocalDateTime.now());
    }
    public boolean needsRefresh() {
        return refreshDate.isBefore(LocalDateTime.now());
    }

}
