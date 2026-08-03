package com.bitebolt.user.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "users")
public class User extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "user_id", updatable = false, nullable = false)
  private UUID userId;

  @Column(name = "full_name")
  private String fullName;

  @Column(name = "avatar")
  private String avatar;

  @Column(name = "email")
  private String email;

  @Column(name = "kyc_status")
  private String kycStatus;

  @Column(name = "rating")
  private Double rating;
}
