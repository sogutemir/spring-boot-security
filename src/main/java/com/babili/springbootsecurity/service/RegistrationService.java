package com.babili.springbootsecurity.service;

import com.babili.springbootsecurity.dto.RegisterRequest;
import com.babili.springbootsecurity.entity.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class RegistrationService {
    
    private final UserService userService;
    private final EmailVerificationService emailVerificationService;
    
    public RegistrationService(UserService userService, 
                             EmailVerificationService emailVerificationService) {
        this.userService = userService;
        this.emailVerificationService = emailVerificationService;
    }
    
    public void registerUser(RegisterRequest request) {
        // Create user
        User user = userService.createUser(request);
        
        // Send verification email
        emailVerificationService.createVerificationToken(user);
    }
}