package com.plazoleta.application.port.output;

import java.util.Optional;

import com.plazoleta.domain.model.User;

/**
 * Consulta de usuarios. Las cuentas pertenecen a users-service; Plazoleta solo las lee.
 */
public interface UserRepositoryPort {
    Optional<User> findById(Long id);

    /** HU-11: consulta siempre a users-service, sin usar los datos del token, para confirmar que la cuenta existe. */
    Optional<User> findRegisteredById(Long id);
}
