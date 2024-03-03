package com.vadimistar.cloudfilestorage;

import com.vadimistar.cloudfilestorage.common.UserArgumentHandlerResolver;
import com.vadimistar.cloudfilestorage.auth.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
@AllArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final UserService userService;

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
        argumentResolvers.add(new UserArgumentHandlerResolver(userService));
    }
}
