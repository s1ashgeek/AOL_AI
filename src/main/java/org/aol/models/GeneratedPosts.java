package org.aol.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Data
@Builder
@AllArgsConstructor
public class GeneratedPosts {
    private String postId_UserId;
    private String postContext_Timestamp;
    private List<Post> generatedPosts;
    private Post userInput;
}
