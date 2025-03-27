package dev.davidpalves.cookbetter.profile.models;

import dev.davidpalves.cookbetter.models.Entity;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Profile extends Entity {
    private String id;
    private String userId;
    private String username;
    private String name;
    private String description;
    private String avatarPhoto;
    private Integer followers;
    private Integer following;
    private Integer recipes;

    public Profile(String id, String userId, String username, String name, String description, String avatarPhoto, Integer followers, Integer following, Integer recipes) {
        this.id = id;
        this.userId = userId;
        this.username = username;
        this.name = name;
        this.description = description;
        this.avatarPhoto = avatarPhoto;
        this.followers = followers;
        this.following = following;
        this.recipes = recipes;
    }

    public Profile(String userId, String username, String name) {
        this.userId = userId;
        this.username = username;
        this.name = name;
        this.description = "";
        this.avatarPhoto = null;
        this.followers = 0;
        this.following = 0;
        this.recipes = 0;
    }
}
