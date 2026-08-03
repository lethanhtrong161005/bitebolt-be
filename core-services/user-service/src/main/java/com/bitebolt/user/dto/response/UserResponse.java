package com.bitebolt.user.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

/** DTO for returning user profile details. */
@Getter
@Setter
@Builder
public class UserResponse {
  private UUID userId;
  private String fullName;
  private String avatar;
  private String email;
  private String kycStatus;
  private Double rating;
  private LocalDateTime createdDate;
  private LocalDateTime lastModifiedDate;
}
