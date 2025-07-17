package com.babili.springbootsecurity.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {
    private String token;
    private String email;
    private String firstName;
    private String lastName;
    private boolean emailVerified;
    private boolean twoFactorEnabled;
}