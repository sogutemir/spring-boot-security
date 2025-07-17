package com.babili.springbootsecurity.service;

import com.babili.springbootsecurity.dto.TwoFactorSetupResponse;
import com.babili.springbootsecurity.entity.User;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import com.warrenstrange.googleauth.GoogleAuthenticatorQRGenerator;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;

@Service
public class TwoFactorAuthService {
    private static final String ISSUER = "SecurityExampleApp";
    private final GoogleAuthenticator gAuth = new GoogleAuthenticator();
    private final UserService userService;
    
    public TwoFactorAuthService(UserService userService) {
        this.userService = userService;
    }
    
    public String generateSecret() {
        final GoogleAuthenticatorKey key = gAuth.createCredentials();
        return key.getKey();
    }
    
    public String generateQRUrl(String secret, String email) {
        return GoogleAuthenticatorQRGenerator.getOtpAuthURL(ISSUER, email, 
                gAuth.createCredentials(secret));
    }
    
    public byte[] generateQRCode(String qrUrl) throws Exception {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(qrUrl, BarcodeFormat.QR_CODE, 200, 200);
        
        ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);
        return pngOutputStream.toByteArray();
    }
    
    public boolean verifyCode(String secret, String code) {
        String normalizedCode = code.replaceAll("\\s", "");
        return gAuth.authorize(secret, Integer.parseInt(normalizedCode));
    }
    
    // Business logic methods
    public TwoFactorSetupResponse setupTwoFactor(String userEmail) {
        User user = userService.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (user.isTwoFactorEnabled()) {
            throw new RuntimeException("Two-factor authentication is already enabled");
        }
        
        String secret = generateSecret();
        String qrUrl = generateQRUrl(secret, user.getEmail());
        
        // Save secret temporarily (will be confirmed when user verifies)
        user.setTwoFactorSecret(secret);
        userService.save(user);
        
        return TwoFactorSetupResponse.builder()
                .secret(secret)
                .qrCodeUrl(qrUrl)
                .manualEntryKey(secret)
                .build();
    }
    
    public void verifyTwoFactorSetup(String userEmail, String code) {
        User user = userService.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (user.getTwoFactorSecret() == null) {
            throw new RuntimeException("Two-factor setup not initiated");
        }
        
        if (!verifyCode(user.getTwoFactorSecret(), code)) {
            throw new RuntimeException("Invalid verification code");
        }
        
        user.setTwoFactorEnabled(true);
        userService.save(user);
    }
    
    public void disableTwoFactor(String userEmail, String code) {
        User user = userService.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (!user.isTwoFactorEnabled()) {
            throw new RuntimeException("Two-factor authentication is not enabled");
        }
        
        if (!verifyCode(user.getTwoFactorSecret(), code)) {
            throw new RuntimeException("Invalid verification code");
        }
        
        user.setTwoFactorEnabled(false);
        user.setTwoFactorSecret(null);
        userService.save(user);
    }
    
    public byte[] getQRCodeForUser(String userEmail) throws Exception {
        User user = userService.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (user.getTwoFactorSecret() == null) {
            throw new RuntimeException("Two-factor setup not initiated");
        }
        
        String qrUrl = generateQRUrl(user.getTwoFactorSecret(), user.getEmail());
        return generateQRCode(qrUrl);
    }
}