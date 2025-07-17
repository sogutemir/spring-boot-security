package com.babili.springbootsecurity.service;

import com.babili.springbootsecurity.entity.EmailVerificationToken;
import com.babili.springbootsecurity.entity.User;
import com.babili.springbootsecurity.repository.EmailVerificationTokenRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class EmailVerificationService {
    private final EmailVerificationTokenRepository tokenRepository;
    private final EmailService emailService;
    
    public EmailVerificationService(EmailVerificationTokenRepository tokenRepository, 
                                  EmailService emailService) {
        this.tokenRepository = tokenRepository;
        this.emailService = emailService;
    }
    
    public void createVerificationToken(User user) {
        // Delete existing token if any
        tokenRepository.deleteByUser(user);
        
        String token = UUID.randomUUID().toString();
        EmailVerificationToken verificationToken = EmailVerificationToken.builder()
                .token(token)
                .user(user)
                .expiresAt(LocalDateTime.now().plusHours(24))
                .build();
        
        tokenRepository.save(verificationToken);
        emailService.sendVerificationEmail(user, token);
    }
    
    public boolean verifyToken(String token) {
        Optional<EmailVerificationToken> verificationToken = tokenRepository.findByToken(token);
        
        if (verificationToken.isEmpty()) {
            return false;
        }
        
        EmailVerificationToken emailToken = verificationToken.get();
        
        if (emailToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            tokenRepository.delete(emailToken);
            return false;
        }
        
        User user = emailToken.getUser();
        user.setEmailVerified(true);
        
        tokenRepository.delete(emailToken);
        return true;
    }
}