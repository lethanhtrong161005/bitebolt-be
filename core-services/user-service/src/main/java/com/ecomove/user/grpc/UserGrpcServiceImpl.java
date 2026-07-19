package com.ecomove.user.grpc;

import com.ecomove.grpc.user.GetUserProfileRequest;
import com.ecomove.grpc.user.UserProfileResponse;
import com.ecomove.grpc.user.UserGrpcServiceGrpc;
import com.ecomove.user.entity.User;
import com.ecomove.user.repository.UserRepository;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;

import java.util.UUID;

@GrpcService
@RequiredArgsConstructor
@Slf4j
public class UserGrpcServiceImpl extends UserGrpcServiceGrpc.UserGrpcServiceImplBase {

    private final UserRepository userRepository;

    @Override
    public void getUserProfile(GetUserProfileRequest request, StreamObserver<UserProfileResponse> responseObserver) {
        log.info("Received gRPC request to fetch user profile for ID: {}", request.getUserId());
        try {
            UUID userId = UUID.fromString(request.getUserId());
            userRepository.findById(userId).ifPresentOrElse(
                    user -> {
                        UserProfileResponse response = UserProfileResponse.newBuilder()
                                .setUserId(user.getUserId().toString())
                                .setFullName(user.getFullName() != null ? user.getFullName() : "")
                                .setEmail(user.getEmail() != null ? user.getEmail() : "")
                                .setAvatar(user.getAvatar() != null ? user.getAvatar() : "")
                                .build();
                        responseObserver.onNext(response);
                        responseObserver.onCompleted();
                    },
                    () -> {
                        log.warn("User with ID: {} not found", userId);
                        responseObserver.onError(Status.NOT_FOUND
                                .withDescription("User not found: " + userId)
                                .asRuntimeException());
                    }
            );
        } catch (IllegalArgumentException e) {
            log.error("Invalid UUID format: {}", request.getUserId(), e);
            responseObserver.onError(Status.INVALID_ARGUMENT
                    .withDescription("Invalid user ID format: " + request.getUserId())
                    .asRuntimeException());
        } catch (Exception e) {
            log.error("Failed to fetch user profile via gRPC", e);
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Internal server error")
                    .asRuntimeException());
        }
    }
}
