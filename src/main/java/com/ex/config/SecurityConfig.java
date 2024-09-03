package com.ex.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.client.web.OAuth2LoginAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.ex.jwt.JWTFilter;
import com.ex.jwt.JWTUtil;
import com.ex.oauth2.CustomSuccessHandler;
import com.ex.service.CustomOAuth2UserService;

import jakarta.servlet.Filter;
import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
	
	private final CustomOAuth2UserService customOAuth2UserService;
	
	private final CustomSuccessHandler customSuccessHandler;
	
	private final JWTUtil jwtUtil;
	
	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		//csrf disable
		http.csrf((auth) -> auth.disable());
		//From 로그인 방식 disable
		http.formLogin((auth) -> auth.disable());
        //HTTP Basic 인증 방식 disable
		http.httpBasic((auth) -> auth.disable());
		//JWTFilter추가 (토큰 만료 재로그인 무한루프 오류 방지 하기 위해 JWTFilter를 OAuth2LoginAuthenticationFilter뒷 순서로 작동할수 있게 추가)
		http.addFilterAfter(new JWTFilter(jwtUtil), OAuth2LoginAuthenticationFilter.class);
        //oauth2
		http.oauth2Login((oauth2) -> oauth2
				.userInfoEndpoint((userInfoEndpointConfig) -> userInfoEndpointConfig
						.userService(customOAuth2UserService))
				.successHandler(customSuccessHandler));
        //경로별 인가 작업
		http.authorizeHttpRequests((auth) -> auth
				.requestMatchers("/").permitAll()
				.anyRequest().authenticated());
        //세션 설정 : STATELESS
		http.sessionManagement((session) -> session
				.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
		
		return http.build();
		
	}
	
}
