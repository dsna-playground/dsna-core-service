package com.dsa.core.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.savedrequest.NullRequestCache;

import com.dsa.core.security.JwtAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

	private final JwtAuthenticationFilter jwtAuthenticationFilter;
	private final AuthenticationConfiguration authenticationConfiguration;

	public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter, AuthenticationConfiguration authenticationConfiguration) {
		this.jwtAuthenticationFilter = jwtAuthenticationFilter;
		this.authenticationConfiguration = authenticationConfiguration;
	}

	@Bean
	public AuthenticationManager authenticationManager() throws Exception {
		return authenticationConfiguration.getAuthenticationManager();
	}
	
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
	
	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http.csrf(AbstractHttpConfigurer::disable)
				.formLogin(AbstractHttpConfigurer::disable)
				.httpBasic(AbstractHttpConfigurer::disable)
				.logout(AbstractHttpConfigurer::disable)
				.requestCache((cache) -> cache.requestCache(new NullRequestCache()))
				.sessionManagement((session) ->
						session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authorizeHttpRequests((requests) -> requests
						.requestMatchers("/api/auth/login", "/api/auth/register").permitAll()
						.requestMatchers("/api/v1/workspaces/**", "/api/profile/**").authenticated()
						.anyRequest().authenticated())
                .exceptionHandling((exceptions) -> exceptions
						.authenticationEntryPoint((request, response, authException) -> {
							response.setStatus(HttpStatus.UNAUTHORIZED.value());
							response.getWriter().write("A valid bearer token is required.");
						}))
				.addFilterBefore(
						jwtAuthenticationFilter,
						UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}

}
