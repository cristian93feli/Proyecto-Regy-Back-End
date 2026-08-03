package com.regyinventory.repository;

import com.regyinventory.entities.LogOperacion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ILogOperacionRepository extends JpaRepository<LogOperacion, Long> {
}
