package com.vadimistar.cloudfilestorage.common;

import com.vadimistar.cloudfilestorage.common.AuthorizedUser;
import com.vadimistar.cloudfilestorage.exceptions.UserNotLoggedInException;
import com.vadimistar.cloudfilestorage.auth.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
@AllArgsConstructor
public class UserArgumentHandlerResolver implements HandlerMethodArgumentResolver {

    private final UserService userService;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(AuthorizedUser.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        var principal = (org.springframework.security.core.userdetails.User) authentication.getPrincipal();
        return userService.getUserByUsername(principal.getUsername())
                .orElseThrow(UserNotLoggedInException::new);
    }
}
