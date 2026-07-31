package com.bitebolt.auth.repository;

import com.bitebolt.auth.entity.Credential;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CredentialRepository extends JpaRepository<Credential, UUID> {

    Optional<Credential> findByUserId(UUID userId);

    Optional<Credential> findByPhone(String phone);

    Optional<Credential> findByEmail(String email);

    Optional<Credential> findByEntraObjectId(String entraObjectId);
}
