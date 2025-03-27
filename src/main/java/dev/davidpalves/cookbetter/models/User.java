package dev.davidpalves.cookbetter.models;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class User extends Entity {
    private String id;
    private String email;
    private String username;
    private String password;
    private String name;

    public User() {
    }

    public User(String email, String username, String password, String name) {
        this.email = email;
        this.username = username;
        this.password = password;
        this.name = name;
    }
}
