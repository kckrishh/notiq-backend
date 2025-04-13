package com.notiq.app.Controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.notiq.app.Dtos.LoginRequestDto;
import com.notiq.app.Model.User;
import com.notiq.app.Service.AuthService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("hello")
    public String hello() {
        return "Hey we are alive";
    }

    @GetMapping("namaste")
    public String namaste() {
        return "namaste!!";
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody User user) {
        return authService.register(user);
    }

    @GetMapping("/confirm")
    public ResponseEntity<String> confirm(@RequestParam String token) {
        return authService.confirm(token);
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginRequestDto request, HttpServletRequest httpRequest) {
        return authService.login(request, httpRequest);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestParam String email) {
        return authService.sendResetEmail(email);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestParam String token, @RequestParam String newPassword) {
        return authService.resetPassword(token, newPassword);
    }

    @GetMapping("/me")
    public ResponseEntity<String> getMe() {
        return authService.getMe();
    }

    // @GetMapping("/logout")
    // public ResponseEntity<String> logout(HttpServletRequest request) {
    // HttpSession session = request.getSession(false);
    // if (session != null) {
    // session.invalidate();
    // }
    // SecurityContextHolder.clearContext();
    // return ResponseEntity.ok("Logged out successfully");
    // }

}
