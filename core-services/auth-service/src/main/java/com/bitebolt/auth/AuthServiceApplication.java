package com.bitebolt.auth;

import com.bitebolt.auth.config.EntraProperties;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

@SpringBootApplication(scanBasePackages = {"com.bitebolt.auth", "com.bitebolt.common"})
public class AuthServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthServiceApplication.class, args);
    }

    @Bean
    public CommandLineRunner debugConfig(Environment env, EntraProperties entraProps) {
        return args -> {
            System.out.println("================== DEBUG CONFIG ==================");
            System.out.println("CWD: " + java.nio.file.Paths.get("").toAbsolutePath());
            System.out.println("../../.env.dev exists: " + java.nio.file.Files.exists(java.nio.file.Paths.get("../../.env.dev")));
            System.out.println(".env.dev exists: " + java.nio.file.Files.exists(java.nio.file.Paths.get(".env.dev")));
            
            System.out.println("ENTRA_TENANT_ID in env: " + env.getProperty("ENTRA_TENANT_ID"));
            System.out.println("ENTRA_CLIENT_ID in env: " + env.getProperty("ENTRA_CLIENT_ID"));
            System.out.println("EntraProperties TenantId: " + entraProps.getTenantId());
            System.out.println("EntraProperties ClientId: " + entraProps.getClientId());
            System.out.println("EntraProperties AuthorizeUrl: " + entraProps.getAuthorizeUrl());
            System.out.println("==================================================");
        };
    }

    
}
