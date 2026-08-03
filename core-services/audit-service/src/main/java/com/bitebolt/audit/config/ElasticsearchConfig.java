package com.bitebolt.audit.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

/**
 * Configuration class for Spring Data Elasticsearch repositories.
 */
@Configuration
@EnableElasticsearchRepositories(basePackages = "com.bitebolt.audit.repository")
public class ElasticsearchConfig {
}
