package com.ecomove.user.repository;

import com.ecomove.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

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
