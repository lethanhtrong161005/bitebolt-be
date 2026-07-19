package com.ecomove.user.service;

import com.ecomove.user.entity.User;
import java.util.UUID;

public interface UserService {

    User getUserById(UUID userId);

    User createUser(UUID userId, String fullName, String email, String avatar);
}
