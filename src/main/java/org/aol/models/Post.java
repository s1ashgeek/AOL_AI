package org.aol.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.aol.models.enums.PostStatus;

import java.util.UUID;

@NoArgsConstructor
@Data
@Builder
@AllArgsConstructor
public class Post {
    private UUID postId;
    private String imageLink;
    private String textSuggestion;
    private PostMetadata postMetadata;
    private PostStatus postStatus;
}
