package dev.davidpalves.cookbetter.recipes.models;

import dev.davidpalves.cookbetter.models.Entity;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class Comment extends Entity {
    private String id;
    private String userId;
    private String recipeId;
    private String parentComment;
    private List<Comment> replies;
    private String content;
}
