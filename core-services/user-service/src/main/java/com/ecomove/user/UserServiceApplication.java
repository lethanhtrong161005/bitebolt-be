package com.ecomove.user;

import com.ecomove.user.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.UUID;

@SpringBootApplication(scanBasePackages = {"com.ecomove.user", "com.ecomove.common"})
public class UserServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }

    @Bean
    public CommandLineRunner seedDemoUser(UserRepository userRepository) {
        return args -> {
            UUID testUserId = UUID.fromString("e81bb380-4966-4194-a15d-4f1073860bb4");
            if (!userRepository.existsById(testUserId)) {
                userRepository.insertUser(
                        testUserId,
                        "Nguyễn Văn A",
                        "testuser@ecomove.com",
                        "https://cdn.ecomove.vn/avatar.png",
                        "VERIFIED",
                        5.0,
                        false
                );
            }
        };
    }
}
