package com.notiq.app.Service;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.UUID;

// import org.springframework.boot.autoconfigure.couchbase.CouchbaseProperties.Authentication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.util.UriUtils;

import com.notiq.app.Config.Security.CustomUserDetails;
import com.notiq.app.Dtos.LoginRequestDto;
import com.notiq.app.Model.User;
import com.notiq.app.Repo.UserRepo;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Service
public class AuthService {

    private final UserRepo repo;
    private final UserDetailsManager userDetailsManager;
    private final EmailService emailService;
    private final AuthenticationManager authenticationManager;

    public AuthService(UserRepo repo, UserDetailsManager userDetailsManager,
            EmailService emailService, AuthenticationManager authenticationManager) {
        this.repo = repo;
        this.userDetailsManager = userDetailsManager;
        this.emailService = emailService;
        this.authenticationManager = authenticationManager;
    }

    public ResponseEntity<String> register(User user) {
        if (repo.findByEmail(user.getEmail()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("User with this email already exists");
        }

        CustomUserDetails userDetails = new CustomUserDetails(user);

        userDetailsManager.createUser(userDetails);
        User savedUser = repo.findByEmail(user.getEmail())
                .orElseThrow(() -> new IllegalStateException("User was not saved"));
        emailService.sendVerificationEmail(savedUser.getEmail(), savedUser.getVerificationToken());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body("User registered successfully. Check your email to verify your account.");
    }

    public ResponseEntity<String> confirm(String token) {
        Optional<User> optionalUser = repo.findByVerificationToken(token);

        String message;
        boolean isValid = false;

        if (optionalUser.isEmpty()) {
            message = "Invalid or expired verification link.";
            String encodedMessage = UriUtils.encode(message, StandardCharsets.UTF_8);
            String redirectUrl = String.format(
                    "https://notiq-app.netlify.app/auth/confirmation?valid=%s&message=%s",
                    isValid,
                    encodedMessage);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).location(URI.create(redirectUrl)).build();

        } else {
            User user = optionalUser.get();

            if (user.isEnabled()) {
                message = "Account already verified. Please log in.";
                String encodedMessage = UriUtils.encode(message, StandardCharsets.UTF_8);
                String redirectUrl = String.format(
                        "https://notiq-app.netlify.app/auth/confirmation?valid=%s&message=%s",
                        isValid,
                        encodedMessage);
                return ResponseEntity.status(HttpStatus.CONFLICT).location(URI.create(redirectUrl)).build();

            } else {

                user.setEnabled(true);
                user.setVerificationToken(null);
                repo.save(user);

                isValid = true;
                message = "Your account has been verified. You can now log in.";
            }
        }

        String encodedMessage = UriUtils.encode(message, StandardCharsets.UTF_8);
        // String redirectUrl = String.format(
        // "http://localhost:4200/auth/confirmation?valid=%s&message=%s",
        // isValid,
        // encodedMessage);
        String redirectUrl = String.format(
                "https://notiq-app.netlify.app/auth/confirmation?valid=%s&message=%s",
                isValid,
                encodedMessage);

        return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(redirectUrl)).build();
    }

    public ResponseEntity<String> login(LoginRequestDto request, HttpServletRequest httpRequest) {
        try {
            SecurityContextHolder.clearContext();

            HttpSession oldSession = httpRequest.getSession(false);
            System.out.println(oldSession);
            if (oldSession != null) {
                oldSession.invalidate();
            }

            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.password()));

            SecurityContextHolder.getContext().setAuthentication(authentication);

            // ✅ Step 2: Create HTTP session (stores JSESSIONID)
            HttpSession session = httpRequest.getSession(true);
            session.setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());
            System.out.println(session);

            return ResponseEntity.ok("Login successful");
        } catch (AuthenticationException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid email or password");
        }
    }

    public ResponseEntity<String> sendResetEmail(String email) {
        Optional<User> optionalUser = repo.findByEmail(email);

        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No account found with that email");
        }

        User user = optionalUser.get();
        String token = UUID.randomUUID().toString();
        user.setResetToken(token);
        repo.save(user);

        emailService.sendForgetPassEmail(email, token);
        return ResponseEntity.ok("Password reset email sent");
    }

    public ResponseEntity<String> resetPassword(String token, String newPassword) {
        Optional<User> optionalUser = repo.findByResetToken(token);
        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Inalid or expired reset link");
        }

        User user = optionalUser.get();
        user.setPassword(newPassword);
        user.setResetToken(null);
        repo.save(user);

        return ResponseEntity.ok("Password reset successfully. You can now log in");
    }

    public ResponseEntity<String> getMe() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return ResponseEntity.ok(authentication.getName());
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }
    }
}
