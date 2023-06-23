package org.aol.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.aol.models.enums.PostContext;

@NoArgsConstructor
@Data
@Builder
@AllArgsConstructor
public class GetSuggestionsPreviewRequest {
    private String text;
    private double creativity;
    private String emotion;
    private String imageS3URL;
    private PostContext postContext;
    private UserMetadata userMetadata;
}
