package com.plazoleta.application.port.output;

import java.util.Optional;

import com.plazoleta.domain.model.User;

/**
 * Consulta de usuarios. Las cuentas pertenecen a users-service; Plazoleta solo las lee.
 */
public interface UserRepositoryPort {
    Optional<User> findById(Long id);
}
