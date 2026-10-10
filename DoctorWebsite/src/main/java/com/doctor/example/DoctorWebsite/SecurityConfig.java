
package com.doctor.example.DoctorWebsite;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth

                        // Public website pages and static assets
                        .requestMatchers(
                                "/", "/index.html", "/style.css", "/script.js",
                                "/favicon.ico", "/images/**", "/fonts/**"
                        ).permitAll()

                        // Public appointment booking and available slots
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/appointments/available-slots"
                        ).permitAll()

                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/appointments"
                        ).permitAll()

                        // Public contact form
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/contact"
                        ).permitAll()

                        // All other requests require authentication
                        .anyRequest().authenticated()
                )
                .httpBasic(Customizer.withDefaults());

        return http.build();
    }

    // Create one admin account using environment variables
    @Bean
    public UserDetailsService userDetailsService(
            @Value("${ADMIN_USERNAME}") String username,
            @Value("${ADMIN_PASSWORD}") String password,
            PasswordEncoder passwordEncoder) {

        UserDetails admin = User.withUsername(username)
                .password(passwordEncoder.encode(password))
                .roles("ADMIN")
                .build();

        return new InMemoryUserDetailsManager(admin);
    }

    // Secure password encoding
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
