package com.regyinventory.repository;

import com.regyinventory.entities.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IUsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByUsername(String username);

    Optional<Usuario> findByCorreo(String correo);

    boolean existsByUsername(String username);

    boolean existsByCorreo(String correo);

    boolean existsByIdentificacion(String identificacion);

    boolean existsByUsernameAndIdNot(String username, Long id);

    boolean existsByCorreoAndIdNot(String correo, Long id);

    boolean existsByIdentificacionAndIdNot(
            String identificacion,
            Long id
    );
}