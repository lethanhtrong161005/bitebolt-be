package com.ecomove.user.service.impl;

import com.ecomove.common.exception.HttpException;
import com.ecomove.user.constant.UserMessageConstant;
import com.ecomove.user.entity.User;
import com.ecomove.user.repository.UserRepository;
import com.ecomove.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

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
}
