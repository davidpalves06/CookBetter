package dev.davidpalves.cookbetter.models;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public abstract class Entity {
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
}
