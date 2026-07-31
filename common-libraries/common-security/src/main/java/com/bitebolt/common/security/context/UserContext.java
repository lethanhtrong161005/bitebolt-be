package com.bitebolt.common.security.context;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object containing the authenticated user's context.
 * Populated by the UserContextInterceptor from HTTP headers.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserContext {
    private String userId;
    private String role;
    private String fullName;
    private String email;
    private String avatar;
}
