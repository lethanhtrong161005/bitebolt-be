package com.bitebolt.user.service.impl;

import com.bitebolt.common.exception.HttpException;
import com.bitebolt.user.constant.UserMessageConstant;
import com.bitebolt.user.entity.User;
import com.bitebolt.user.repository.UserRepository;
import com.bitebolt.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.bitebolt.user.dto.request.UserUpdateRequest;
import com.bitebolt.user.dto.response.UserResponse;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public User getUserById(UUID userId) {
        log.info("Fetching user profile from database for ID: {}", userId);
        return userRepository.findById(userId)
                .orElseThrow(() -> new HttpException(404, UserMessageConstant.ERROR_USER_NOT_FOUND, userId.toString()));
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public User createUser(UUID userId, String fullName, String email, String avatar) {
        log.info("Provisioning user profile to database: email={}, userId={}", email, userId);
        
        userRepository.insertUser(
                userId,
                fullName,
                email,
                avatar,
                "VERIFIED",
                5.0,
                false
        );

        return userRepository.findById(userId)
                .orElseThrow(() -> new HttpException(500, UserMessageConstant.ERROR_USER_CREATION_FAILED, userId.toString()));
    }
    @Override
    @Transactional(readOnly = true)
    public Page<UserResponse> getAllUsers(Pageable pageable) {
        log.info("Fetching all active users with pagination");
        return userRepository.findAllActiveUsers(pageable)
                .map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserProfile(UUID userId) {
        log.info("Fetching detailed profile for user: {}", userId);
        User user = userRepository.findActiveUserById(userId)
                .orElseThrow(() -> new HttpException(404, UserMessageConstant.ERROR_USER_NOT_FOUND, userId.toString()));
        return mapToResponse(user);
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public UserResponse updateUser(UUID userId, UserUpdateRequest request) {
        log.info("Updating user profile for user: {}", userId);
        User user = userRepository.findActiveUserById(userId)
                .orElseThrow(() -> new HttpException(404, UserMessageConstant.ERROR_USER_NOT_FOUND, userId.toString()));

        user.setFullName(request.getFullName());
        if (request.getAvatar() != null) {
            user.setAvatar(request.getAvatar());
        }
        if (request.getKycStatus() != null) {
            user.setKycStatus(request.getKycStatus());
        }

        userRepository.save(user);
        return mapToResponse(user);
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public void deleteUser(UUID userId) {
        log.info("Soft deleting user: {}", userId);
        User user = userRepository.findActiveUserById(userId)
                .orElseThrow(() -> new HttpException(404, UserMessageConstant.ERROR_USER_NOT_FOUND, userId.toString()));
        
        user.setIsDeleted(true);
        userRepository.save(user);
    }

    private UserResponse mapToResponse(User user) {
        return UserResponse.builder()
                .userId(user.getUserId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .avatar(user.getAvatar())
                .kycStatus(user.getKycStatus())
                .rating(user.getRating())
                .createdDate(user.getCreatedAt() != null ? java.time.LocalDateTime.ofInstant(user.getCreatedAt(), java.time.ZoneId.systemDefault()) : null)
                .lastModifiedDate(user.getUpdatedAt() != null ? java.time.LocalDateTime.ofInstant(user.getUpdatedAt(), java.time.ZoneId.systemDefault()) : null)
                .build();
    }
}
