package com.regyinventory.service.implementation;

import com.regyinventory.dto.request.LoginRequestDTO;
import com.regyinventory.dto.response.LoginResponseDTO;
import com.regyinventory.security.CustomUserDetails;
import com.regyinventory.security.JwtService;
import com.regyinventory.service.contracts.IAuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthenticationService implements IAuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @Override
    public LoginResponseDTO login(LoginRequestDTO request) {

        Authentication authentication =
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                request.getUsername(),
                                request.getPassword()
                        )
                );

        CustomUserDetails userDetails =
                (CustomUserDetails) authentication.getPrincipal();

        String token = jwtService.generateToken(userDetails);

        List<String> roles = userDetails.getUsuario()
                .getRoles()
                .stream()
                .map(role -> role.getNombre().name())
                .sorted()
                .toList();

        String nombreCompleto = (
                userDetails.getUsuario().getNombre()
                        + " "
                        + userDetails.getUsuario().getApellido()
        ).trim();

        return LoginResponseDTO.builder()
                .id(userDetails.getUsuario().getId())
                .username(userDetails.getUsername())
                .nombre(nombreCompleto)
                .roles(roles)
                .token(token)
                .build();
    }
}