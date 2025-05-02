package dev.davidpalves.cookbetter.recipes.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class CommentDTO {
    private String parentComment;
    private String content;
}
