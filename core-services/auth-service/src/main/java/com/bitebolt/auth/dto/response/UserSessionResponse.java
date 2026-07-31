package com.bitebolt.auth.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserSessionResponse {
    private String userId;
    private String fullName;
    private String email;
    private String avatar;
    private String role;
}
