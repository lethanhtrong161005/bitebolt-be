package com.bitebolt.common.security.config;

import com.bitebolt.common.security.interceptor.UserContextInterceptor;
import com.bitebolt.common.security.resolver.CurrentUserArgumentResolver;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * Automatically configures Spring Web MVC to register UserContextInterceptor,
 * CurrentUserArgumentResolver, and global enterprise CORS mappings in any microservice that imports this module.
 *
 * <p><strong>Standard Execution Steps:</strong>
 *
 * <ol>
 *   <li><strong>Interceptor:</strong> Registers {@link UserContextInterceptor} to extract user context headers.
 *   <li><strong>Argument Resolver:</strong> Registers {@link CurrentUserArgumentResolver} for controller parameter injection.
 *   <li><strong>CORS Policy:</strong> Configures central CORS preflight and cross-origin permissions across all paths.
 * </ol>
 */
@Configuration
public class WebSecurityConfig implements WebMvcConfigurer {

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(new UserContextInterceptor());
  }

  @Override
  public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
    resolvers.add(new CurrentUserArgumentResolver());
  }

  @Override
  public void addCorsMappings(CorsRegistry registry) {
    registry.addMapping("/**")
        .allowedOriginPatterns("*")
        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD", "PATCH")
        .allowedHeaders("*")
        .exposedHeaders("Authorization", "Content-Type", "X-Trace-Id")
        .allowCredentials(true)
        .maxAge(3600);
  }
}
