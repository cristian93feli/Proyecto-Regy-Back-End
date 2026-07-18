package com.regyinventory.config;

import com.regyinventory.entities.Permission;
import com.regyinventory.entities.Role;
import com.regyinventory.entities.User;
import com.regyinventory.enums.PermissionName;
import com.regyinventory.enums.RoleName;
import com.regyinventory.repository.IPermissionRepository;
import com.regyinventory.repository.IRoleRepository;
import com.regyinventory.repository.IUserRepository;
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

    private final IPermissionRepository permissionRepository;
    private final IRoleRepository roleRepository;
    private final IUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {

        Set<Permission> allPermissions = createPermissions();

        Role adminRole = createOrUpdateRole(
                RoleName.ROLE_ADMIN,
                "Administrador general del sistema",
                allPermissions
        );

        createOrUpdateRole(
                RoleName.ROLE_GUARDIAN,
                "Responsable operativo del inventario",
                selectPermissions(
                        allPermissions,
                        guardianPermissions()
                )
        );

        createOrUpdateRole(
                RoleName.ROLE_PACKER,
                "Responsable de preparación y reposición de empaque",
                selectPermissions(
                        allPermissions,
                        packerPermissions()
                )
        );

        createAdminUser(adminRole);
    }

    private Set<Permission> createPermissions() {

        return Arrays.stream(PermissionName.values())
                .map(permissionName ->
                        permissionRepository.findByNombre(permissionName)
                                .orElseGet(() ->
                                        permissionRepository.save(
                                                Permission.builder()
                                                        .nombre(permissionName)
                                                        .descripcion(
                                                                "Permiso " + permissionName.name()
                                                        )
                                                        .build()
                                        )
                                )
                )
                .collect(Collectors.toSet());
    }

    private Role createOrUpdateRole(
            RoleName roleName,
            String description,
            Set<Permission> permissions
    ) {

        Role role = roleRepository.findByNombre(roleName)
                .orElseGet(() ->
                        Role.builder()
                                .nombre(roleName)
                                .descripcion(description)
                                .build()
                );

        role.setDescripcion(description);
        role.setPermissions(new HashSet<>(permissions));

        return roleRepository.save(role);
    }

    private Set<Permission> selectPermissions(
            Set<Permission> allPermissions,
            Set<PermissionName> requiredPermissions
    ) {

        return allPermissions.stream()
                .filter(permission ->
                        requiredPermissions.contains(permission.getNombre())
                )
                .collect(Collectors.toSet());
    }

    private Set<PermissionName> guardianPermissions() {

        return EnumSet.of(
                PermissionName.PRODUCT_CREATE,
                PermissionName.PRODUCT_READ,
                PermissionName.PRODUCT_UPDATE,
                PermissionName.PRODUCT_DELETE,

                PermissionName.WAREHOUSE_CREATE,
                PermissionName.WAREHOUSE_READ,
                PermissionName.WAREHOUSE_UPDATE,

                PermissionName.LOCATION_CREATE,
                PermissionName.LOCATION_READ,
                PermissionName.LOCATION_UPDATE,

                PermissionName.PACKING_ZONE_READ,

                PermissionName.STOCK_READ,
                PermissionName.STOCK_RECEIVE,
                PermissionName.STOCK_MOVE,

                PermissionName.WAREHOUSE_AUDIT,
                PermissionName.LOCATION_AUDIT,

                PermissionName.REPLENISHMENT_REQUEST_CREATE,
                PermissionName.REPLENISHMENT_REQUEST_READ,
                PermissionName.REPLENISHMENT_REQUEST_COMPLETE,
                PermissionName.REPLENISHMENT_REQUEST_CANCEL
        );
    }

    private Set<PermissionName> packerPermissions() {

        return EnumSet.of(
                PermissionName.PRODUCT_READ,
                PermissionName.PACKING_ZONE_READ,
                PermissionName.STOCK_READ,
                PermissionName.PACKING_ZONE_AUDIT,

                PermissionName.REPLENISHMENT_REQUEST_CREATE,
                PermissionName.REPLENISHMENT_REQUEST_READ
        );
    }

    private void createAdminUser(Role adminRole) {

        if (userRepository.existsByUsername("admin")) {
            return;
        }

        User admin = User.builder()
                .identificacion("ADMIN-001")
                .nombre("Administrador")
                .apellido("REGY")
                .correo("admin@regy.local")
                .username("admin")
                .password(passwordEncoder.encode("Admin123*"))
                .roles(Set.of(adminRole))
                .build();

        userRepository.save(admin);
    }
}