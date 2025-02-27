package dev.davidpalves.cookbetter.auth.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthenticationDTO {
    private String name;
    private String email;
    private String password;
    private String username;

    public AuthenticationDTO() {}
    public AuthenticationDTO(String name, String email, String password, String username) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.username = username;
    }
}