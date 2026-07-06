package com.example.empleados.repository;

import com.example.empleados.domain.Empleado;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmpleadoRepository extends JpaRepository<Empleado, Long> {

    boolean existsByEmail(String email);

    boolean existsByDni(String dni);
}
