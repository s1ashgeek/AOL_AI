package org.aol.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.aol.models.enums.PostContext;

import java.util.Date;

@NoArgsConstructor
@Data
@Builder
@AllArgsConstructor
public class PostMetadata {
    private Date timestamp;
    private PostContext postContext;
}
