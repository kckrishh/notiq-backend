package com.notiq.app.Config.Security;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.UserDetailsManager;

public class CustomAuthenticationManager implements AuthenticationManager {
    private final UserDetailsManager userDetailsManager;
    private final PasswordEncoder passwordEncoder;

    public CustomAuthenticationManager(
            UserDetailsManager userDetailsManager,
            PasswordEncoder passwordEncoder) {
        this.userDetailsManager = userDetailsManager;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String email = authentication.getName();
        // String password = authentication.getCredentials().toString();
        Object credentials = authentication.getCredentials();
        if (credentials == null) {
            throw new RuntimeException("Hello, no credentials found");
        }
        String password = credentials.toString();

        System.out.println("Authenticating user: " + authentication.getName());
        System.out.println("Credentials: " + authentication.getCredentials());

        CustomUserDetails userDetails = (CustomUserDetails) userDetailsManager.loadUserByUsername(email);

        if (!userDetails.isEnabled()) {
            throw new BadCredentialsException("Email not verified.");
        }

        if (!passwordEncoder.matches(password, userDetails.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }

}
