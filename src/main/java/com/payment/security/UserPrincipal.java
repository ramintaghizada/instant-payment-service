
package com.payment.security;

import com.payment.model.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Collection;
import java.util.UUID;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
public class UserPrincipal implements UserDetails {
    
    private UUID id;
    private String phoneNumber;
    private String email;
    private String password;
    private Collection<? extends GrantedAuthority> authorities;
    private boolean emailVerified;
    private boolean phoneVerified;
    private boolean twoFactorEnabled;
    
    public static UserPrincipal create(User user) {
        Collection<GrantedAuthority> authorities = user.getRoles().stream()
            .flatMap(role -> role.getPermissions().stream())
            .map(permission -> new SimpleGrantedAuthority(permission.getName()))
            .collect(Collectors.toSet());
        
        return new UserPrincipal(
            user.getId(),
            user.getPhoneNumber(),
            user.getEmail(),
            user.getPasswordHash(),
            authorities,
            user.isEmailVerified(),
            user.isPhoneVerified(),
            user.isTwoFactorEnabled()
        );
    }
    
    @Override
    public String getUsername() {
        return phoneNumber;
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
        return true;
    }
}