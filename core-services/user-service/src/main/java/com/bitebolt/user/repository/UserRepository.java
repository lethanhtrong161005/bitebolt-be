package com.bitebolt.user.repository;

import com.bitebolt.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    @Query("SELECT u FROM User u WHERE u.isDeleted = false")
    Page<User> findAllActiveUsers(Pageable pageable);

    @Query("SELECT u FROM User u WHERE u.userId = :userId AND u.isDeleted = false")
    Optional<User> findActiveUserById(@Param("userId") UUID userId);

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO users (user_id, full_name, email, avatar, kyc_status, rating, is_deleted, created_at, updated_at, created_by, updated_by) " +
            "VALUES (:userId, :fullName, :email, :avatar, :kycStatus, :rating, :isDeleted, NOW(), NOW(), 'system', 'system') " +
            "ON CONFLICT (user_id) DO NOTHING", nativeQuery = true)
    void insertUser(@Param("userId") UUID userId, 
                    @Param("fullName") String fullName, 
                    @Param("email") String email, 
                    @Param("avatar") String avatar, 
                    @Param("kycStatus") String kycStatus, 
                    @Param("rating") Double rating, 
                    @Param("isDeleted") Boolean isDeleted);
}
