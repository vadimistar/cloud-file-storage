package com.vadimistar.cloudfilestorage.security.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.LogoutConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        HttpSessionRequestCache requestCache = new HttpSessionRequestCache();
        requestCache.setMatchingRequestParameterName(null);

        httpSecurity
                .authorizeHttpRequests((requests) -> requests
                        .requestMatchers(HttpMethod.GET, "/register", "/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/register", "/login").permitAll()
                        .requestMatchers(new AntPathRequestMatcher("/public/**")).permitAll()
                        .requestMatchers(new AntPathRequestMatcher("/**/favicon.ico")).permitAll()
                        .requestMatchers(new AntPathRequestMatcher("/static/**")).permitAll()
                        .requestMatchers(HttpMethod.GET, "/error").permitAll()
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/")
                        .permitAll()
                )
                .requestCache((cache) -> cache.requestCache(requestCache))
                .logout(LogoutConfigurer::permitAll);

        return httpSecurity.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
