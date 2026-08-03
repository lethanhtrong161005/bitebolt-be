package com.bitebolt.user.grpc;

import com.bitebolt.common.exception.HttpException;
import com.bitebolt.grpc.user.CreateUserProfileRequest;
import com.bitebolt.grpc.user.GetUserProfileRequest;
import com.bitebolt.grpc.user.UserProfileResponse;
import com.bitebolt.grpc.user.UserGrpcServiceGrpc;
import com.bitebolt.user.entity.User;
import com.bitebolt.user.service.UserService;
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

  private final UserService userService;

  @Override
  public void getUserProfile(
      GetUserProfileRequest request, StreamObserver<UserProfileResponse> responseObserver) {
    log.info("Received gRPC request to fetch user profile for ID: {}", request.getUserId());
    try {
      UUID userId = UUID.fromString(request.getUserId());
      User user = userService.getUserById(userId);

      UserProfileResponse response =
          UserProfileResponse.newBuilder()
              .setUserId(user.getUserId().toString())
              .setFullName(user.getFullName() != null ? user.getFullName() : "")
              .setEmail(user.getEmail() != null ? user.getEmail() : "")
              .setAvatar(user.getAvatar() != null ? user.getAvatar() : "")
              .build();

      responseObserver.onNext(response);
      responseObserver.onCompleted();

    } catch (Exception e) {
      handleException(e, responseObserver, "Fetch user profile failed");
    }
  }

  @Override
  public void createUserProfile(
      CreateUserProfileRequest request, StreamObserver<UserProfileResponse> responseObserver) {
    log.info("Received gRPC request to create user profile for email: {}", request.getEmail());
    try {
      UUID userId = UUID.fromString(request.getUserId());
      User user =
          userService.createUser(
              userId, request.getFullName(), request.getEmail(), request.getAvatar());

      UserProfileResponse response =
          UserProfileResponse.newBuilder()
              .setUserId(user.getUserId().toString())
              .setFullName(user.getFullName() != null ? user.getFullName() : "")
              .setEmail(user.getEmail() != null ? user.getEmail() : "")
              .setAvatar(user.getAvatar() != null ? user.getAvatar() : "")
              .build();

      responseObserver.onNext(response);
      responseObserver.onCompleted();

    } catch (Exception e) {
      handleException(e, responseObserver, "Create user profile failed");
    }
  }

  private void handleException(Throwable e, StreamObserver<?> responseObserver, String logPrefix) {
    if (e instanceof IllegalArgumentException) {
      log.error("{} - Invalid UUID format", logPrefix, e);
      responseObserver.onError(
          Status.INVALID_ARGUMENT
              .withDescription("Invalid UUID format: " + e.getMessage())
              .asRuntimeException());
    } else if (e instanceof HttpException httpEx) {
      log.warn(
          "{} - Business exception: status={}, message={}",
          logPrefix,
          httpEx.getStatusCode(),
          httpEx.getMessage());
      Status status;
      switch (httpEx.getStatusCode()) {
        case 400:
          status = Status.INVALID_ARGUMENT;
          break;
        case 401:
          status = Status.UNAUTHENTICATED;
          break;
        case 403:
          status = Status.PERMISSION_DENIED;
          break;
        case 404:
          status = Status.NOT_FOUND;
          break;
        case 409:
          status = Status.ALREADY_EXISTS;
          break;
        default:
          status = Status.INTERNAL;
          break;
      }
      responseObserver.onError(status.withDescription(httpEx.getMessage()).asRuntimeException());
    } else {
      log.error("{} - Unexpected error occurred", logPrefix, e);
      responseObserver.onError(
          Status.INTERNAL.withDescription("Internal server error").asRuntimeException());
    }
  }
}
