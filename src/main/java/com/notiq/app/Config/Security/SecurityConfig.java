package com.notiq.app.Config.Security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.notiq.app.Repo.UserRepo;

import jakarta.servlet.http.HttpServletResponse;

@Configuration
public class SecurityConfig {

    private final UserRepo repo;

    public SecurityConfig(UserRepo repo) {
        this.repo = repo;
    }

    @Bean
    public UserDetailsManager userDetailsManager() {
        return new CustomUserDetailsManager(repo);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return NoOpPasswordEncoder.getInstance();
    }

    @Bean
    public AuthenticationManager authenticationManager(UserDetailsManager userDetailsManager,
            PasswordEncoder encoder) {
        return new CustomAuthenticationManager(userDetailsManager, encoder);
    }

    @Bean
    public SecurityFilterChain configure(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .sessionManagement(c -> c
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                        .invalidSessionUrl("http://localhost:4200/auth/login")
                        .maximumSessions(1)
                        .expiredUrl("http://localhost:4200/auth/login"))
                .logout(logout -> logout // Add explicit logout configuration
                        .logoutUrl("/auth/logout")
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .deleteCookies("JSESSIONID")
                        .logoutSuccessHandler((request, response, authentication) -> {
                            new SecurityContextLogoutHandler().logout(request, response, authentication);
                            response.setStatus(HttpServletResponse.SC_OK);
                            response.getWriter().write("Logged out successfully");
                            System.out.println("logged out successfully");
                        }))
                .authorizeHttpRequests(c -> c.requestMatchers(HttpMethod.POST, "/auth/register").permitAll())
                .authorizeHttpRequests(c -> c.requestMatchers(HttpMethod.POST, "/auth/login").permitAll())
                .authorizeHttpRequests(c -> c.requestMatchers(HttpMethod.POST, "/auth/forgot-password").permitAll())
                .authorizeHttpRequests(c -> c.requestMatchers(HttpMethod.POST, "/auth/reset-password").permitAll())
                .authorizeHttpRequests(c -> c.requestMatchers(HttpMethod.GET, "/auth/confirm").permitAll()
                        .anyRequest().authenticated())
                .csrf(c -> c.disable());

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.addAllowedOrigin("http://localhost:4200");
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

}
