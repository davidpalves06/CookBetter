package dev.davidpalves.cookbetter.auth.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class AuthenticationResult {
    private String accessToken;
    private String message;
    private AuthenticationDTO user;

    public AuthenticationResult(String message) {
        this.message = message;
        this.accessToken = null;
    }

    public AuthenticationResult(String accessToken, String message, AuthenticationDTO user) {
        this.accessToken = accessToken;
        this.message = message;
        this.user = user;
    }
}
