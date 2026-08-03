package com.regyinventory.service.contracts;

import com.regyinventory.dto.request.LoginRequestDTO;
import com.regyinventory.dto.response.LoginResponseDTO;

public interface IAuthenticationService {

    LoginResponseDTO login(LoginRequestDTO request);
}