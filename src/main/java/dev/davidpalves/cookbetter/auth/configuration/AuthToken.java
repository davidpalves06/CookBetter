package dev.davidpalves.cookbetter.auth.configuration;

import java.time.LocalDateTime;

public record AuthToken(String email, LocalDateTime expirationDate) {

    public boolean isExpired() {
        return expirationDate.isBefore(LocalDateTime.now());
    }

}
