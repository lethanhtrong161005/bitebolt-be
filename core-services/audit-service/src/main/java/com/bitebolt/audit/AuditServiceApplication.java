package com.bitebolt.audit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Main entry point for the Audit Service microservice.
 *
 * <p><strong>Standard Execution Steps:</strong></p>
 * <ol>
 *   <li><strong>Bootstrap:</strong> Initializes the Spring Boot application context.</li>
 *   <li><strong>Discovery:</strong> Registers the service with Netflix Eureka via {@code @EnableDiscoveryClient}.</li>
 *   <li><strong>Auditing:</strong> Enables JPA Auditing to automatically populate entity timestamps via {@code @EnableJpaAuditing}.</li>
 * </ol>
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableJpaAuditing
public class AuditServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(AuditServiceApplication.class, args);
    }
}
