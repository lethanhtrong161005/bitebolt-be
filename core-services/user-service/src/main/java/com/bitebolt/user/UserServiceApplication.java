package com.bitebolt.user;

import com.bitebolt.user.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.UUID;

@SpringBootApplication(scanBasePackages = {"com.bitebolt.user", "com.bitebolt.common"})
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
                        "testuser@BiteBolt.com",
                        "https://cdn.BiteBolt.vn/avatar.png",
                        "VERIFIED",
                        5.0,
                        false
                );
            }
        };
    }
}
