package com.bitebolt.user.controller;

import com.bitebolt.common.dto.ApiResponse;
import com.bitebolt.common.utils.ResponseHelper;
import com.bitebolt.user.dto.request.UserUpdateRequest;
import com.bitebolt.user.dto.response.UserResponse;
import com.bitebolt.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

import com.bitebolt.user.constant.UserMessageConstant;

/**
 * REST controller for managing User Profiles (Rider/Driver).
 * Provides endpoints for retrieving, updating, and deleting user data.
 */
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * Fetch a paginated list of all active users.
     *
     * @param pageable pagination and sorting details
     * @return a page of UserResponse
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<UserResponse>>> getAllUsers(@org.springdoc.core.annotations.ParameterObject Pageable pageable) {
        Page<UserResponse> users = userService.getAllUsers(pageable);
        return ResponseEntity.ok(ResponseHelper.successWithData(users, UserMessageConstant.SUCCESS_FETCHED));
    }

    /**
     * Get detailed profile of a specific user.
     *
     * @param userId the UUID of the user
     * @return UserResponse containing user details
     */
    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable UUID userId) {
        UserResponse user = userService.getUserProfile(userId);
        return ResponseEntity.ok(ResponseHelper.successWithData(user, UserMessageConstant.SUCCESS_FETCHED));
    }

    /**
     * Update an existing user's profile.
     *
     * @param userId the UUID of the user
     * @param request the update payload
     * @return the updated UserResponse
     */
    @PutMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @PathVariable UUID userId,
            @Valid @RequestBody UserUpdateRequest request) {
        UserResponse updatedUser = userService.updateUser(userId, request);
        return ResponseEntity.ok(ResponseHelper.successWithData(updatedUser, UserMessageConstant.SUCCESS_UPDATED));
    }

    /**
     * Soft delete a user profile.
     *
     * @param userId the UUID of the user to delete
     * @return success response
     */
    @DeleteMapping("/{userId}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable UUID userId) {
        userService.deleteUser(userId);
        return ResponseEntity.ok(ResponseHelper.success(UserMessageConstant.SUCCESS_DELETED));
    }
}
