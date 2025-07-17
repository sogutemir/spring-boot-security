package com.babili.springbootsecurity.repository;

import com.babili.springbootsecurity.entity.User;
import com.babili.springbootsecurity.service.UserService;
import com.warrenstrange.googleauth.ICredentialRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CustomCredentialRepository implements ICredentialRepository {

    private final UserService userService;

    public CustomCredentialRepository(UserService userService) {
        this.userService = userService;
    }

    @Override
    public String getSecretKey(String userName) {
        return userService.findByEmail(userName)
                .map(User::getTwoFactorSecret)
                .orElse(null);
    }

    @Override
    public void saveUserCredentials(String userName, String secretKey, int validationCode, List<Integer> scratchCodes) {
        userService.findByEmail(userName).ifPresent(user -> {
            user.setTwoFactorSecret(secretKey);
            userService.save(user);
        });
    }
}
