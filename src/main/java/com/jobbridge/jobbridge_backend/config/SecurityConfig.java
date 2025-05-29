package com.jobbridge.jobbridge_backend.config;


import com.jobbridge.jobbridge_backend.entity.User;
import com.jobbridge.jobbridge_backend.repository.UserRepository;
import com.jobbridge.jobbridge_backend.security.JwtAuthenticationFilter;
import com.jobbridge.jobbridge_backend.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // 인증이 필요 없는 공개 API 엔드포인트
                        .requestMatchers(
                                "/api/user/login",
                                "/api/user/signup",
                                "/api/user/send-code",
                                "/api/user/verify",
                                // 비밀번호 재설정 API 엔드포인트
                                "/api/user/password-reset",
                                "/api/user/password-reset/confirm"
                        ).permitAll()

                        .requestMatchers("/api/resume/**").hasAuthority("ROLE_INDIVIDUAL")
                        .requestMatchers("/api/match/jobs").hasAuthority("ROLE_INDIVIDUAL")      // 개인: 추천 채용공고
                        .requestMatchers("/api/match/resumes").hasAuthority("ROLE_COMPANY")      // 기업: 추천 이력서
                        .requestMatchers("/api/match/career").hasAnyAuthority("ROLE_INDIVIDUAL", "ROLE_COMPANY")  // 둘 다

                        // 채용공고 접근 권한 수정 - 조회는 모두 허용, 등록/수정/삭제는 COMPANY만 허용
                        .requestMatchers("/api/job-posting/my", "/api/job-posting/{id}").authenticated()
                        .requestMatchers("/api/job-posting", "/api/job-posting/{id}").hasAuthority("ROLE_COMPANY")

                        // 채용공고 검색 API 접근 허용
                        .requestMatchers("/api/jobs/**").permitAll()

                        // 지원 관련 API - 개인 회원만 허용
                        .requestMatchers("/api/apply/**").hasAuthority("ROLE_INDIVIDUAL")
                        .requestMatchers("/api/applications/check/**").hasAuthority("ROLE_INDIVIDUAL")
                        .requestMatchers("/api/applications/mine").hasAuthority("ROLE_INDIVIDUAL")

                        // 테스트용 API (개발 환경에서만 사용)
                        .requestMatchers("/api/applications/all").permitAll()

                        // 기타 모든 요청은 인증 필요
                        .anyRequest().authenticated()
                );

        // JWT 인증 필터 추가
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:5173")); // 프론트엔드 도메인
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(UserRepository userRepository) {
        return username -> {
            User user = userRepository.findByEmail(username)
                    .orElseThrow(() -> new UsernameNotFoundException("유저를 찾을 수 없습니다."));
            return new UserDetailsImpl(user);
        };
    }
}