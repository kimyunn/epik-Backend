package com.epik.global.config;

import com.epik.global.security.filter.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sess ->
                        sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .formLogin(form ->
                        form.disable())
                .httpBasic(basic ->
                        basic.disable())
                .authorizeHttpRequests(auth -> auth
                        // 회원 전용
                        .requestMatchers("/api/v1/auth/logout").authenticated() // 로그아웃
                        // 비회원
                        .requestMatchers("/api/v1/auth/**").permitAll() // 인증 및 회원가입
                        // 공개 조회
                        .requestMatchers(HttpMethod.GET, "/api/v1/popups/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/notices/**").permitAll()
                        .anyRequest().authenticated()
                )

                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);


        return http.build();
    }
}
