package com.bitebolt.common.security.resolver;

import com.bitebolt.common.security.annotation.CurrentUser;
import com.bitebolt.common.security.context.UserContext;
import com.bitebolt.common.security.context.UserContextHolder;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * Resolves the @CurrentUser annotation into the actual UserContext 
 * stored in the UserContextHolder.
 */
public class CurrentUserArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterAnnotation(CurrentUser.class) != null 
                && parameter.getParameterType().equals(UserContext.class);
    }

    @Override
    public UserContext resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        return UserContextHolder.getContext();
    }
}
