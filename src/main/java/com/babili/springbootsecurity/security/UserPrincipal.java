package com.babili.springbootsecurity.security;

import com.babili.springbootsecurity.entity.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Getter
public class UserPrincipal implements UserDetails {
    private Long id;
    private String email;
    private String password;
    private Collection<? extends GrantedAuthority> authorities;
    private boolean emailVerified;
    private boolean twoFactorEnabled;
    
    public UserPrincipal(Long id, String email, String password, 
                        Collection<? extends GrantedAuthority> authorities,
                        boolean emailVerified, boolean twoFactorEnabled) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.authorities = authorities;
        this.emailVerified = emailVerified;
        this.twoFactorEnabled = twoFactorEnabled;
    }
    
    public static UserPrincipal create(User user) {
        List<GrantedAuthority> authorities = Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_" + user.getRole().name())
        );
        
        return new UserPrincipal(
                user.getId(),
                user.getEmail(),
                user.getPassword(),
                authorities,
                user.isEmailVerified(),
                user.isTwoFactorEnabled()
        );
    }
    
    @Override
    public String getUsername() {
        return email;
    }
    
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }
    
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }
    
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
    
    @Override
    public boolean isEnabled() {
        return emailVerified;
    }
}