package com.bitebolt.user.service;

import com.bitebolt.user.entity.User;
import com.bitebolt.user.dto.request.UserUpdateRequest;
import com.bitebolt.user.dto.response.UserResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.UUID;

public interface UserService {

  User getUserById(UUID userId);

  User createUser(UUID userId, String fullName, String email, String avatar);

  Page<UserResponse> getAllUsers(Pageable pageable);

  UserResponse getUserProfile(UUID userId);

  UserResponse updateUser(UUID userId, UserUpdateRequest request);

  void deleteUser(UUID userId);
}
