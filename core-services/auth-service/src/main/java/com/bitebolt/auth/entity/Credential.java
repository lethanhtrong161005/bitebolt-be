package com.bitebolt.auth.entity;

import com.bitebolt.auth.enums.AuthMethod;
import com.bitebolt.auth.enums.Role;
import com.bitebolt.auth.enums.Status;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "credentials")
public class Credential extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "id", updatable = false, nullable = false)
  private UUID id;

  @Column(name = "user_id", nullable = false)
  private UUID userId;

  @Column(name = "phone", unique = true)
  private String phone;

  @Column(name = "password_hash")
  private String passwordHash;

  @Enumerated(EnumType.STRING)
  @Column(name = "role", nullable = false)
  private Role role;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false)
  private Status status;

  @Enumerated(EnumType.STRING)
  @Column(name = "auth_method", nullable = false)
  private AuthMethod authMethod = AuthMethod.PHONE_OTP;

  @Column(name = "email", unique = true)
  private String email;

  @Column(name = "entra_object_id", unique = true)
  private String entraObjectId;
}
