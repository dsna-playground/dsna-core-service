package com.dsa.core.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import lombok.extern.slf4j.Slf4j;

@Configuration
@EnableWebSecurity
@Slf4j
public class SecurityConfig {

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
	
	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http.csrf((csrf) -> csrf.disable()) // Disable CSRF for simplicity here
				.authorizeHttpRequests((requests) -> requests
						.requestMatchers("/register").permitAll() // Allow access to registration without authentication
						.requestMatchers("/login").anonymous() // Allow only anonymous users to access /login
                        .requestMatchers("/logout").authenticated() // Allow only logged-in users to /logout
						.anyRequest().authenticated()) // All other requests need authentication
				.formLogin((login) -> login
						.defaultSuccessUrl("/api/profile", true) // Redirect to /api/profile after successful login
						.successHandler((request, response, authentication) -> {
							if (request.getSession().getAttribute("firstLogin") == null) {
								request.getSession().setAttribute("firstLogin", true);
								response.sendRedirect("/api/profile");
							} else {
								response.setStatus(HttpStatus.OK.value());
								response.getWriter().write("Already logged in.");
							}
						}))
                .logout((logout) -> logout
                        .logoutSuccessHandler((request, response, authentication) -> {
                            if (authentication != null && authentication.isAuthenticated()) {
                                if (request.getSession(false) != null) {
                                    request.getSession().invalidate();
                                }
                                response.setStatus(HttpStatus.OK.value());
                                response.getWriter().write("Logged out successfully.");
                            } else {
                                response.setStatus(HttpStatus.OK.value());
                                response.getWriter().write("Already logged out.");
                            }
                        })
                        .deleteCookies("JSESSIONID")) // Clear the session cookie
//                        .invalidateHttpSession(true)) // Invalidate the session
//              .httpBasic((basic) -> basic.disable()) // Disable Spring's default HTTP Basic authentication
                .exceptionHandling((exceptions) -> exceptions
                	    .authenticationEntryPoint((request, response, authException) -> {
                	        response.setStatus(HttpStatus.UNAUTHORIZED.value());
                	        response.getWriter().write("Please login.");
                	    }));

		log.info("Configured SecurityFilterChain: {}", http);

		return http.build();
	}

}
