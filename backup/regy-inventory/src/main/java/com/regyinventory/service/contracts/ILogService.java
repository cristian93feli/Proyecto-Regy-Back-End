package com.regyinventory.service.contracts;

import com.regyinventory.dto.request.*;
import com.regyinventory.dto.response.*;

public interface ILogService {
    PageResponseDTO<LogResponseDTO> listar(Integer page, Integer size);
}
