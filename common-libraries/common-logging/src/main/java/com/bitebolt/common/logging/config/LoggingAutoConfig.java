package com.bitebolt.common.logging.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Auto-configuration class for the common-logging module.
 *
 * <p><strong>Standard Execution Steps:</strong></p>
 * <ol>
 *   <li><strong>Discovery:</strong> Picked up by Spring Boot's auto-configuration mechanism (via {@code org.springframework.boot.autoconfigure.AutoConfiguration.imports}).</li>
 *   <li><strong>Component Scan:</strong> Scans the {@code com.bitebolt.common.logging} package to register filters, aspects, and components.</li>
 *   <li><strong>Initialization:</strong> Instantiates tracing filters and audit publishers as Spring Beans in the consuming microservice.</li>
 * </ol>
 */
@Configuration
@ComponentScan(basePackages = "com.bitebolt.common.logging")
public class LoggingAutoConfig {
}
