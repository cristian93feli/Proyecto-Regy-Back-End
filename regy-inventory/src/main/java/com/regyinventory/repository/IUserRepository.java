package com.regyinventory.repository;

import com.regyinventory.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IUserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByCorreo(String correo);

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