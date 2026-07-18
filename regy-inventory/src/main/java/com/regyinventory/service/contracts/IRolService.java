package com.regyinventory.service.contracts;

import com.regyinventory.dto.response.RolResponseDTO;

import java.util.List;

public interface IRolService {

    List<RolResponseDTO> findAll(boolean activeOnly);

    RolResponseDTO findById(Long id);
}