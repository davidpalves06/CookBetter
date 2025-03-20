package dev.davidpalves.cookbetter.recipes.models;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class Recipe {
    private String id;
    private String title;
    private String userId;
    private String description;
    private List<String> ingredients;
    private List<String> instructions;
    private List<String> tags;
    private String image;

    public Recipe() {
    }

    public Recipe(String id,String title, String userId, String description, List<String> ingredients, List<String> instructions, String image) {
        this.id = id;
        this.title = title;
        this.userId = userId;
        this.description = description;
        this.ingredients = ingredients;
        this.instructions = instructions;
        this.image = image;
    }
}
