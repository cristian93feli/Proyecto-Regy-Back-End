package com.regyinventory.service.contracts;

import com.regyinventory.dto.request.ChangePasswordRequestDTO;
import com.regyinventory.dto.request.UserCreateRequestDTO;
import com.regyinventory.dto.request.UserUpdateRequestDTO;
import com.regyinventory.dto.response.PageResponseDTO;
import com.regyinventory.dto.response.UserResponseDTO;

public interface IUserService {

    UserResponseDTO create(UserCreateRequestDTO request);

    UserResponseDTO findById(Long id);

    PageResponseDTO<UserResponseDTO> findAll(
            Integer page,
            Integer size,
            String sortBy,
            String direction
    );

    UserResponseDTO update(
            Long id,
            UserUpdateRequestDTO request
    );

    void changePassword(
            Long id,
            ChangePasswordRequestDTO request
    );

    UserResponseDTO changeActiveStatus(
            Long id,
            boolean active,
            String authenticatedUsername
    );
}