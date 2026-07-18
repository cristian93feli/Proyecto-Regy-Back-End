package com.regyinventory.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class LoginResponseDTO {

    private Long id;
    private String username;
    private String nombre;
    private List<String> roles;
    private String token;
}