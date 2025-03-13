package dev.davidpalves.cookbetter.profile.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProfileDTO {
    private String name;
    private String username;

    public ProfileDTO() {}
    public ProfileDTO(String name, String username) {
        this.name = name;
        this.username = username;
    }
}
