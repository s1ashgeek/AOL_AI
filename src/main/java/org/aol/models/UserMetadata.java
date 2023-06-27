package org.aol.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
@Builder
@AllArgsConstructor
public class UserMetadata {
    private String userId;
    private String deviceId;
    private String ip;
    private String email;
    private String userCookies;
}
