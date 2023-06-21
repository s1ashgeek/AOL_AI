package org.aol.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.aol.models.enums.PostContext;
import org.joda.time.DateTime;

@NoArgsConstructor
@Data
@Builder
@AllArgsConstructor
public class PostMetadata {
    private DateTime Timestamp;
    private PostContext postContext;
}
