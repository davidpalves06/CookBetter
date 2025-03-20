package dev.davidpalves.cookbetter.profile.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProfileDTO {
    private String userId;
    private String username;
    private String name;
    private String description;
    private String avatarPhoto;
    private Integer followers;
    private Integer following;
    private Integer recipes;

    public ProfileDTO(String userId,String username, String name, String description, String avatarPhoto, Integer followers, Integer following, Integer recipes) {
        this.name = name;
        this.username = username;
        this.description = description;
        this.avatarPhoto = avatarPhoto;
        this.followers = followers;
        this.following = following;
        this.recipes = recipes;
    }

    public ProfileDTO() {}
    public ProfileDTO(String username, String name) {
        this.name = name;
        this.username = username;
    }

    public ProfileDTO(String userId, String username , String name) {
        this.userId = userId;
        this.name = name;
        this.username = username;
    }
}
