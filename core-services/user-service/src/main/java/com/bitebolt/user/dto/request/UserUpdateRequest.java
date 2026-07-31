package com.bitebolt.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO for updating an existing user profile.
 */
@Getter
@Setter
public class UserUpdateRequest {
    
    @NotBlank(message = "Full name cannot be blank")
    private String fullName;
    
    private String avatar;
    
    private String kycStatus;
}
