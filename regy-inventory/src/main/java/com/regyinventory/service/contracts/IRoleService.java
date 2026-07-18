package com.regyinventory.service.contracts;

import com.regyinventory.dto.response.RoleResponseDTO;

import java.util.List;

public interface IRoleService {

    List<RoleResponseDTO> findAll(boolean activeOnly);

    RoleResponseDTO findById(Long id);
}