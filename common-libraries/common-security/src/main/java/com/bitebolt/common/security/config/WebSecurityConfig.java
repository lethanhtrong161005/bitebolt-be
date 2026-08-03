package com.bitebolt.common.security.config;

import com.bitebolt.common.security.interceptor.UserContextInterceptor;
import com.bitebolt.common.security.resolver.CurrentUserArgumentResolver;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * Automatically configures Spring Web MVC to register the UserContextInterceptor and
 * CurrentUserArgumentResolver in any microservice that imports this module.
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
}
