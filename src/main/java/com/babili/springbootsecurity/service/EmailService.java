package com.babili.springbootsecurity.service;

import com.babili.springbootsecurity.entity.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    private final JavaMailSender mailSender;
    
    @Value("${app.name}")
    private String appName;
    
    @Value("${app.frontend-url}")
    private String frontendUrl;
    
    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }
    
    public void sendVerificationEmail(User user, String token) {
        String verificationUrl = frontendUrl + "/verify-email?token=" + token;
        
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(user.getEmail());
        message.setSubject("Email Verification - " + appName);
        message.setText("Dear " + user.getFirstName() + ",\n\n" +
                "Please click the following link to verify your email address:\n" +
                verificationUrl + "\n\n" +
                "This link will expire in 24 hours.\n\n" +
                "Best regards,\n" +
                appName + " Team");
        
        mailSender.send(message);
    }
}