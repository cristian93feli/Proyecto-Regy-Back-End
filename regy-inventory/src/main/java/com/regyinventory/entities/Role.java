package com.regyinventory.entities;

import com.regyinventory.enums.RoleName;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role extends BaseEntity{



    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true)
    private RoleName nombre;

    @Column(length = 250)
    private String descripcion;


    @ManyToMany(mappedBy = "roles")
    @Builder.Default
    private Set<User> usuarios = new HashSet<>();

    @ManyToMany(fetch = FetchType.EAGER)

    @JoinTable(
            name = "roles_permissions",
            joinColumns = @JoinColumn(name = "role_id"),
            inverseJoinColumns = @JoinColumn(name = "permission_id")
    )
    @Builder.Default
    private Set<Permission> permissions = new HashSet<>();

}