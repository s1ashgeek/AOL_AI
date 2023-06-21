package org.aol.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
@Builder
@AllArgsConstructor
public class Permissions {
    private boolean agreeToReuse;
    private boolean agreeToTnC;
}
