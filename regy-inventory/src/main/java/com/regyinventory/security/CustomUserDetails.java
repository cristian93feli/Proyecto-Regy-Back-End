package com.regyinventory.security;

import com.regyinventory.entities.Permiso;
import com.regyinventory.entities.Rol;
import com.regyinventory.entities.Usuario;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Getter
public class CustomUserDetails implements UserDetails {

    private final Usuario usuario;

    public CustomUserDetails(Usuario user) {
        this.usuario = user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {

        Set<GrantedAuthority> authorities = new HashSet<>();

        for (Rol rol : usuario.getRoles()) {

            authorities.add(new SimpleGrantedAuthority(rol.getNombre().name()));

            for (Permiso permiso : rol.getPermisos()) {

                authorities.add(
                        new SimpleGrantedAuthority(permiso.getNombre().name())
                );

            }

        }

        return authorities;
    }

    @Override
    public String getPassword() {
        return usuario.getPassword();
    }

    @Override
    public String getUsername() {
        return usuario.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return usuario.getActivo();
    }

    @Override
    public boolean isAccountNonLocked() {
        return usuario.getActivo();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return usuario.getActivo();
    }

    @Override
    public boolean isEnabled() {
        return usuario.getActivo();
    }

}