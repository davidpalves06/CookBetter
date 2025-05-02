package dev.davidpalves.cookbetter.recipes.models;

import dev.davidpalves.cookbetter.models.Entity;
import lombok.Getter;
import lombok.Setter;

import java.time.Duration;
import java.util.List;

@Getter
@Setter
public class Recipe extends Entity {
    private String id;
    private String title;
    private String userId;
    private String description;
    private List<String> ingredients;
    private List<String> instructions;
    private List<String> tags;
    private String image;
    private int duration;

    public Recipe() {}

    public Recipe(String id,String title, String userId, String description,
                  List<String> ingredients, List<String> instructions, String image,
                  int duration) {
        this.id = id;
        this.title = title;
        this.userId = userId;
        this.description = description;
        this.ingredients = ingredients;
        this.instructions = instructions;
        this.image = image;
        this.duration = duration;
    }
}
