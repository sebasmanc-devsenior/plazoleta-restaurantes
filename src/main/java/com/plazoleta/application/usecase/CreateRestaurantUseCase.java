package com.plazoleta.application.usecase;

import com.plazoleta.domain.util.MensajesError;

import com.plazoleta.application.dto.CreateRestaurantCommand;
import com.plazoleta.application.dto.RestaurantResponse;
import com.plazoleta.application.port.input.CreateRestaurantPort;
import com.plazoleta.application.port.output.RestaurantRepositoryPort;
import com.plazoleta.application.port.output.UserRepositoryPort;
import com.plazoleta.domain.model.Role;
import com.plazoleta.domain.exception.UserNotFoundException;
import com.plazoleta.domain.model.Restaurant;
import com.plazoleta.domain.model.User;

public class CreateRestaurantUseCase implements CreateRestaurantPort {
    private final RestaurantRepositoryPort restaurantRepositoryPort;
    private final UserRepositoryPort userRepositoryPort;

    public CreateRestaurantUseCase(RestaurantRepositoryPort restaurantRepositoryPort, UserRepositoryPort userRepositoryPort) {
        this.restaurantRepositoryPort = restaurantRepositoryPort;
        this.userRepositoryPort = userRepositoryPort;
    }

    @Override
    public RestaurantResponse execute(CreateRestaurantCommand command) {
        if (command == null) {
            throw new IllegalArgumentException(MensajesError.CUERPO_DE_LA_PETICION_ES_OBLIGATORIO);
        }
        if (command.getOwnerUserId() == null) {
            throw new IllegalArgumentException(MensajesError.PROPIETARIO_DEL_RESTAURANTE_ES_OBLIGATORIO);
        }

        User owner = userRepositoryPort.findById(command.getOwnerUserId())
                .orElseThrow(() -> new UserNotFoundException(MensajesError.USUARIO_PROPIETARIO_NO_EXISTE));

        if (owner.getRole() != Role.PROPIETARIO) {
            throw new IllegalArgumentException(MensajesError.USUARIO_DEBE_TENER_EL_ROL_PROPIETARIO);
        }

        Restaurant restaurant = new Restaurant(null, command.getName(), command.getNit(), command.getAddress(),
                command.getPhone(), command.getLogoUrl(), owner.getId());

        Restaurant saved = restaurantRepositoryPort.save(restaurant);

        return new RestaurantResponse(saved.getId(), saved.getName(), saved.getNit(), saved.getAddress(),
                saved.getPhone(), saved.getLogoUrl(), saved.getOwnerUserId());
    }
}
