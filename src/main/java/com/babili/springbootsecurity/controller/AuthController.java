package com.babili.springbootsecurity.controller;

import com.babili.springbootsecurity.dto.*;
import com.babili.springbootsecurity.exception.UserAlreadyExistsException;
import com.babili.springbootsecurity.service.AuthService;
import com.babili.springbootsecurity.service.RegistrationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:3000")
public class AuthController {
    
    private final AuthService authService;
    private final RegistrationService registrationService;
    
    public AuthController(AuthService authService, RegistrationService registrationService) {
        this.authService = authService;
        this.registrationService = registrationService;
    }
    
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        try {
            registrationService.registerUser(request);
            return ResponseEntity.ok(new MessageResponse("User registered successfully! Please check your email to verify your account."));
        } catch (UserAlreadyExistsException e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }
    
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            AuthResponse authResponse = authService.login(request);
            return ResponseEntity.ok(authResponse);
        } catch (RuntimeException e) {
            if (e.getMessage().equals("Two-factor authentication required")) {
                return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                        .body(new MessageResponse(e.getMessage()));
            }
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Invalid credentials"));
        }
    }
    
    @PostMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(@RequestParam String token) {
        try {
            if (authService.verifyEmail(token)) {
                return ResponseEntity.ok(new MessageResponse("Email verified successfully!"));
            } else {
                return ResponseEntity.badRequest().body(new MessageResponse("Invalid or expired token"));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Invalid or expired token"));
        }
    }
    
    @PostMapping("/resend-verification")
    public ResponseEntity<?> resendVerification(@RequestParam String email) {
        try {
            authService.resendVerificationEmail(email);
            return ResponseEntity.ok(new MessageResponse("Verification email sent"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }
}