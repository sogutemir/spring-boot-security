package com.babili.springbootsecurity.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TwoFactorVerificationRequest {
    @NotBlank(message = "Code is required")
    private String code;
}