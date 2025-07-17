package com.babili.springbootsecurity.service;

import com.babili.springbootsecurity.dto.AuthResponse;
import com.babili.springbootsecurity.dto.LoginRequest;
import com.babili.springbootsecurity.entity.User;
import com.babili.springbootsecurity.security.UserPrincipal;
import com.babili.springbootsecurity.util.JwtUtils;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    
    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final TwoFactorAuthService twoFactorAuthService;
    private final JwtUtils jwtUtils;
    private final EmailVerificationService emailVerificationService;
    
    public AuthService(AuthenticationManager authenticationManager,
                      UserService userService,
                      TwoFactorAuthService twoFactorAuthService,
                      JwtUtils jwtUtils,
                      EmailVerificationService emailVerificationService) {
        this.authenticationManager = authenticationManager;
        this.userService = userService;
        this.twoFactorAuthService = twoFactorAuthService;
        this.jwtUtils = jwtUtils;
        this.emailVerificationService = emailVerificationService;
    }
    
    public AuthResponse login(LoginRequest request) {
        // Find user
        User user = userService.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Check if email is verified
        if (!user.isEmailVerified()) {
            throw new RuntimeException("Please verify your email first");
        }
        
        // Authenticate user
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
        
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        
        // Handle 2FA if enabled
        if (user.isTwoFactorEnabled()) {
            if (request.getTwoFactorCode() == null || request.getTwoFactorCode().isEmpty()) {
                throw new RuntimeException("Two-factor authentication required");
            }
            
            if (!twoFactorAuthService.verifyCode(user.getTwoFactorSecret(), request.getTwoFactorCode())) {
                throw new RuntimeException("Invalid two-factor authentication code");
            }
        }
        
        // Generate JWT token
        String jwt = jwtUtils.generateJwtToken(userPrincipal);
        
        // Build and return response
        return AuthResponse.builder()
                .token(jwt)
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .emailVerified(user.isEmailVerified())
                .twoFactorEnabled(user.isTwoFactorEnabled())
                .build();
    }
    
    public boolean verifyEmail(String token) {
        return emailVerificationService.verifyToken(token);
    }
    
    public void resendVerificationEmail(String email) {
        User user = userService.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (user.isEmailVerified()) {
            throw new RuntimeException("Email already verified");
        }
        
        emailVerificationService.createVerificationToken(user);
    }
}