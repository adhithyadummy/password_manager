package com.aditya.password_manager.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http.csrf(AbstractHttpConfigurer::disable).authorizeHttpRequests(auth -> auth
				// 1. Allow access to your REST APIs
				.requestMatchers("/api/users/**", "/api/passwords/**").permitAll()

				// 2. Allow access to your frontend static files (HTML, CSS, JS)
				.requestMatchers("/", "/index.html", "/style.css", "/app.js", "/favicon.ico").permitAll()

				// Require authentication for anything else
				.anyRequest().authenticated());

		return http.build();
	}
}