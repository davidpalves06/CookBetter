package dev.davidpalves.cookbetter.recipes.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
public class RecipeDTO {
    private String id;
    private String userId;
    private String title;
    private String description;
    private List<String> ingredients;
    private List<String> instructions;
    private List<String> tags;
    private String imageUrl;

}
