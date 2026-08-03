package com.regyinventory.repository;

import com.regyinventory.entities.ConfiguracionSistema;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IConfiguracionSistemaRepository extends JpaRepository<ConfiguracionSistema,Long>  {
    java.util.Optional<ConfiguracionSistema> findByClave(String clave);
}
