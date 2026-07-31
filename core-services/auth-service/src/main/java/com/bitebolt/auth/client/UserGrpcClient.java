package com.bitebolt.auth.client;

import com.bitebolt.auth.constant.AuthMessageConstant;
import com.bitebolt.common.exception.HttpException;
import com.bitebolt.grpc.user.GetUserProfileRequest;
import com.bitebolt.grpc.user.UserProfileResponse;
import com.bitebolt.grpc.user.UserGrpcServiceGrpc;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class UserGrpcClient {

    @GrpcClient("user-service")
    private UserGrpcServiceGrpc.UserGrpcServiceBlockingStub userGrpcStub;

    public UserProfileResponse getUserProfile(UUID userId) {
        log.info("Calling user-service via gRPC for user profile ID: {}", userId);
        try {
            GetUserProfileRequest request = GetUserProfileRequest.newBuilder()
                    .setUserId(userId.toString())
                    .build();

            return userGrpcStub
                    .withDeadlineAfter(3, TimeUnit.SECONDS)
                    .getUserProfile(request);

        } catch (StatusRuntimeException e) {
            Status.Code code = e.getStatus().getCode();
            String description = e.getStatus().getDescription();
            log.error("gRPC error occurred. Code: {}, Description: {}", code, description, e);

            // Map gRPC NOT_FOUND to ERROR_USER_NOT_FOUND (HTTP 401)
            if (code == Status.Code.NOT_FOUND || 
                (description != null && description.toLowerCase().contains("not found"))) {
                log.warn("User profile not found for ID: {}", userId);
                throw new HttpException(401, AuthMessageConstant.ERROR_USER_NOT_FOUND);
            }
            
            // Map gRPC DEADLINE_EXCEEDED to ERROR_USER_SERVICE_TIMEOUT (HTTP 408)
            if (code == Status.Code.DEADLINE_EXCEEDED) {
                log.warn("gRPC deadline exceeded when calling user-service for ID: {}", userId);
                throw new HttpException(408, AuthMessageConstant.ERROR_USER_SERVICE_TIMEOUT);
            }
            
            // Map gRPC INVALID_ARGUMENT to HTTP 400
            if (code == Status.Code.INVALID_ARGUMENT) {
                throw new HttpException(400, AuthMessageConstant.ERROR_INVALID_USER_ID);
            }

            throw new HttpException(503, AuthMessageConstant.ERROR_USER_SERVICE_UNAVAILABLE);
        }
    }

    public UserProfileResponse createUserProfile(UUID userId, String fullName, String email, String avatar) {
        log.info("Calling user-service via gRPC to create user profile for email: {}", email);
        try {
            com.bitebolt.grpc.user.CreateUserProfileRequest request = com.bitebolt.grpc.user.CreateUserProfileRequest.newBuilder()
                    .setUserId(userId.toString())
                    .setFullName(fullName != null ? fullName : "")
                    .setEmail(email != null ? email : "")
                    .setAvatar(avatar != null ? avatar : "")
                    .build();

            return userGrpcStub
                    .withDeadlineAfter(3, TimeUnit.SECONDS)
                    .createUserProfile(request);
        } catch (StatusRuntimeException e) {
            log.error("gRPC error occurred when creating user profile. Code: {}, Description: {}", 
                    e.getStatus().getCode(), e.getStatus().getDescription(), e);
            throw new HttpException(503, AuthMessageConstant.ERROR_USER_SERVICE_UNAVAILABLE);
        }
    }
}
