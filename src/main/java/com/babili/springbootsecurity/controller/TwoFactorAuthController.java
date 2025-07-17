package com.babili.springbootsecurity.controller;

import com.babili.springbootsecurity.dto.MessageResponse;
import com.babili.springbootsecurity.dto.TwoFactorSetupResponse;
import com.babili.springbootsecurity.dto.TwoFactorVerificationRequest;
import com.babili.springbootsecurity.security.UserPrincipal;
import com.babili.springbootsecurity.service.TwoFactorAuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth/2fa")
@CrossOrigin(origins = "http://localhost:3000")
public class TwoFactorAuthController {
    
    private final TwoFactorAuthService twoFactorAuthService;
    
    public TwoFactorAuthController(TwoFactorAuthService twoFactorAuthService) {
        this.twoFactorAuthService = twoFactorAuthService;
    }
    
    @PostMapping("/setup")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> setupTwoFactor(Authentication authentication) {
        try {
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            TwoFactorSetupResponse response = twoFactorAuthService.setupTwoFactor(userPrincipal.getEmail());
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error setting up 2FA"));
        }
    }
    
    @PostMapping("/verify-setup")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> verifyTwoFactorSetup(@Valid @RequestBody TwoFactorVerificationRequest request,
                                                 Authentication authentication) {
        try {
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            twoFactorAuthService.verifyTwoFactorSetup(userPrincipal.getEmail(), request.getCode());
            return ResponseEntity.ok(new MessageResponse("Two-factor authentication enabled successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error verifying 2FA setup"));
        }
    }
    
    @PostMapping("/disable")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> disableTwoFactor(@Valid @RequestBody TwoFactorVerificationRequest request,
                                            Authentication authentication) {
        try {
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            twoFactorAuthService.disableTwoFactor(userPrincipal.getEmail(), request.getCode());
            return ResponseEntity.ok(new MessageResponse("Two-factor authentication disabled successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error disabling 2FA"));
        }
    }
    
    @GetMapping("/qr-code")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<byte[]> getQRCode(Authentication authentication) {
        try {
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            byte[] qrCodeBytes = twoFactorAuthService.getQRCodeForUser(userPrincipal.getEmail());
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_PNG);
            
            return ResponseEntity.ok().headers(headers).body(qrCodeBytes);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}