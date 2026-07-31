package com.bitebolt.auth.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Enterprise standard Environment Post Processor to securely load .env files
 * directly into the Spring Environment before configuration properties are resolved.
 */
public class EnvLoaderEnvironmentPostProcessor implements EnvironmentPostProcessor {

    private static final String DOTENV_PROPERTY_SOURCE_NAME = "dotenvProperties";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        Path envPath = findEnvFile();
        if (envPath == null) {
            System.out.println("[BiteBolt EnvLoader] Warning: .env.dev file not found in parent directories.");
            return;
        }

        System.out.println("[BiteBolt EnvLoader] Found environment file at: " + envPath.toAbsolutePath());
        Map<String, Object> envMap = new HashMap<>();

        try (Stream<String> lines = Files.lines(envPath)) {
            lines.forEach(line -> {
                line = line.trim();
                if (!line.isEmpty() && !line.startsWith("#")) {
                    int separatorIndex = line.indexOf('=');
                    if (separatorIndex > 0) {
                        String key = line.substring(0, separatorIndex).trim();
                        String value = line.substring(separatorIndex + 1).trim();
                        
                        // Remove surrounding quotes if present
                        if (value.startsWith("\"") && value.endsWith("\"") || value.startsWith("'") && value.endsWith("'")) {
                            value = value.substring(1, value.length() - 1);
                        }
                        envMap.put(key, value);
                    }
                }
            });
        } catch (IOException e) {
            System.err.println("[BiteBolt EnvLoader] Failed to read environment file: " + e.getMessage());
            return;
        }

        if (!envMap.isEmpty()) {
            MutablePropertySources propertySources = environment.getPropertySources();
            // Add as first property source to ensure it overrides defaults and is available for config-server placeholders
            propertySources.addFirst(new MapPropertySource(DOTENV_PROPERTY_SOURCE_NAME, envMap));
            System.out.println("[BiteBolt EnvLoader] Successfully injected " + envMap.size() + " variables into Spring Environment.");
        }
    }

    private Path findEnvFile() {
        Path currentDir = Paths.get("").toAbsolutePath();
        String[] possibleNames = {".env.dev", ".env"};

        while (currentDir != null) {
            for (String name : possibleNames) {
                Path file = currentDir.resolve(name);
                if (Files.exists(file)) {
                    return file;
                }
            }
            currentDir = currentDir.getParent();
        }
        return null;
    }
}
