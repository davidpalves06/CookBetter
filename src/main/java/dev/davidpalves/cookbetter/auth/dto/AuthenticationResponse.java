package dev.davidpalves.cookbetter.auth.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthenticationResponse {
    private String errorMessage;

    public AuthenticationResponse(String errorMessage) {
        this.errorMessage = errorMessage;
    }

}