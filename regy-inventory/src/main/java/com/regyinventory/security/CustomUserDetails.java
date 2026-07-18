package com.regyinventory.security;

import com.regyinventory.entities.Permission;
import com.regyinventory.entities.Role;
import com.regyinventory.entities.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Getter
public class CustomUserDetails implements UserDetails {

    private final User user;

    public CustomUserDetails(User user) {
        this.user = user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {

        Set<GrantedAuthority> authorities = new HashSet<>();

        for (Role role : user.getRoles()) {

            authorities.add(new SimpleGrantedAuthority(role.getNombre().name()));

            for (Permission permission : role.getPermissions()) {

                authorities.add(
                        new SimpleGrantedAuthority(permission.getNombre().name())
                );

            }

        }

        return authorities;
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return user.getActivo();
    }

    @Override
    public boolean isAccountNonLocked() {
        return user.getActivo();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return user.getActivo();
    }

    @Override
    public boolean isEnabled() {
        return user.getActivo();
    }

}