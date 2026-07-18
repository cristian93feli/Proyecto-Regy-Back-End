package com.regyinventory.config;

import com.regyinventory.entities.Permiso;
import com.regyinventory.entities.Rol;
import com.regyinventory.entities.Usuario;
import com.regyinventory.enums.NombrePermiso;
import com.regyinventory.enums.NombreRol;
import com.regyinventory.repository.IPermisoRepository;
import com.regyinventory.repository.IRolRepository;
import com.regyinventory.repository.IUsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final IPermisoRepository permisoRepository;
    private final IRolRepository rolRepository;
    private final IUsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {

        Set<Permiso> allPermisos = createPermissions();

        Rol adminRol = createOrUpdateRole(
                NombreRol.ROLE_ADMIN,
                "Administrador general del sistema",
                allPermisos
        );

        createOrUpdateRole(
                NombreRol.ROLE_GUARDIAN,
                "Responsable operativo del inventario",
                selectPermissions(
                        allPermisos,
                        guardianPermissions()
                )
        );

        createOrUpdateRole(
                NombreRol.ROLE_PACKER,
                "Responsable de preparación y reposición de empaque",
                selectPermissions(
                        allPermisos,
                        packerPermissions()
                )
        );

        createAdminUser(adminRol);
    }

    private Set<Permiso> createPermissions() {

        return Arrays.stream(NombrePermiso.values())
                .map(nombrePermiso ->
                        permisoRepository.findByNombre(nombrePermiso)
                                .orElseGet(() ->
                                        permisoRepository.save(
                                                Permiso.builder()
                                                        .nombre(nombrePermiso)
                                                        .descripcion(
                                                                "Permiso " + nombrePermiso.name()
                                                        )
                                                        .build()
                                        )
                                )
                )
                .collect(Collectors.toSet());
    }

    private Rol createOrUpdateRole(
            NombreRol nombreRol,
            String description,
            Set<Permiso> permisos
    ) {

        Rol rol = rolRepository.findByNombre(nombreRol)
                .orElseGet(() ->
                        Rol.builder()
                                .nombre(nombreRol)
                                .descripcion(description)
                                .build()
                );

        rol.setDescripcion(description);
        rol.setPermisos(new HashSet<>(permisos));

        return rolRepository.save(rol);
    }

    private Set<Permiso> selectPermissions(
            Set<Permiso> allPermisos,
            Set<NombrePermiso> requiredPermissions
    ) {

        return allPermisos.stream()
                .filter(permiso ->
                        requiredPermissions.contains(permiso.getNombre())
                )
                .collect(Collectors.toSet());
    }

    private Set<NombrePermiso> guardianPermissions() {

        return EnumSet.of(
                NombrePermiso.PRODUCT_CREATE,
                NombrePermiso.PRODUCT_READ,
                NombrePermiso.PRODUCT_UPDATE,
                NombrePermiso.PRODUCT_DELETE,

                NombrePermiso.WAREHOUSE_CREATE,
                NombrePermiso.WAREHOUSE_READ,
                NombrePermiso.WAREHOUSE_UPDATE,

                NombrePermiso.LOCATION_CREATE,
                NombrePermiso.LOCATION_READ,
                NombrePermiso.LOCATION_UPDATE,

                NombrePermiso.PACKING_ZONE_READ,

                NombrePermiso.STOCK_READ,
                NombrePermiso.STOCK_RECEIVE,
                NombrePermiso.STOCK_MOVE,

                NombrePermiso.WAREHOUSE_AUDIT,
                NombrePermiso.LOCATION_AUDIT,

                NombrePermiso.REPLENISHMENT_REQUEST_CREATE,
                NombrePermiso.REPLENISHMENT_REQUEST_READ,
                NombrePermiso.REPLENISHMENT_REQUEST_COMPLETE,
                NombrePermiso.REPLENISHMENT_REQUEST_CANCEL
        );
    }

    private Set<NombrePermiso> packerPermissions() {

        return EnumSet.of(
                NombrePermiso.PRODUCT_READ,
                NombrePermiso.PACKING_ZONE_READ,
                NombrePermiso.STOCK_READ,
                NombrePermiso.PACKING_ZONE_AUDIT,

                NombrePermiso.REPLENISHMENT_REQUEST_CREATE,
                NombrePermiso.REPLENISHMENT_REQUEST_READ
        );
    }

    private void createAdminUser(Rol adminRol) {

        if (usuarioRepository.existsByUsername("admin")) {
            return;
        }

        Usuario admin = Usuario.builder()
                .identificacion("ADMIN-001")
                .nombre("Administrador")
                .apellido("REGY")
                .correo("admin@regy.local")
                .username("admin")
                .password(passwordEncoder.encode("Admin123*"))
                .roles(Set.of(adminRol))
                .build();

        usuarioRepository.save(admin);
    }
}