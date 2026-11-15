package com.plazoleta.application.usecase;

import com.plazoleta.domain.util.MensajesError;

import com.plazoleta.application.dto.CreateDishCommand;
import com.plazoleta.application.dto.DishResponse;
import com.plazoleta.application.port.input.CreateDishPort;
import com.plazoleta.application.port.output.DishRepositoryPort;
import com.plazoleta.application.port.output.RestaurantRepositoryPort;
import com.plazoleta.application.port.output.UserRepositoryPort;
import com.plazoleta.domain.model.Role;
import com.plazoleta.domain.exception.RestaurantNotFoundException;
import com.plazoleta.domain.exception.RestaurantOwnershipException;
import com.plazoleta.domain.model.Dish;
import com.plazoleta.domain.model.Restaurant;
import com.plazoleta.domain.model.User;

public class CreateDishUseCase implements CreateDishPort {
    private final DishRepositoryPort dishRepositoryPort;
    private final RestaurantRepositoryPort restaurantRepositoryPort;
    private final UserRepositoryPort userRepositoryPort;

    public CreateDishUseCase(DishRepositoryPort dishRepositoryPort, RestaurantRepositoryPort restaurantRepositoryPort, UserRepositoryPort userRepositoryPort) {
        this.dishRepositoryPort = dishRepositoryPort;
        this.restaurantRepositoryPort = restaurantRepositoryPort;
        this.userRepositoryPort = userRepositoryPort;
    }

    @Override
    public DishResponse execute(CreateDishCommand command, Long authenticatedOwnerId) {
        if (command == null) {
            throw new IllegalArgumentException(MensajesError.CUERPO_DE_LA_PETICION_ES_OBLIGATORIO);
        }

        User owner = userRepositoryPort.findById(authenticatedOwnerId)
                .orElseThrow(() -> new IllegalArgumentException(MensajesError.USUARIO_AUTENTICADO_NO_EXISTE));
        if (owner.getRole() != Role.PROPIETARIO) {
            throw new RestaurantOwnershipException(MensajesError.SOLO_UN_PROPIETARIO_PUEDE_CREAR_PLATOS);
        }

        Restaurant restaurant = restaurantRepositoryPort.findById(command.getRestaurantId())
                .orElseThrow(() -> new RestaurantNotFoundException(MensajesError.RESTAURANTE_NO_EXISTE));

        if (!restaurant.getOwnerUserId().equals(authenticatedOwnerId)) {
            throw new RestaurantOwnershipException(MensajesError.PROPIETARIO_NO_ES_DUENO_DE_ESTE_RESTAURANTE);
        }

        Dish dish = new Dish(null, command.getName(), command.getPrice(), command.getDescription(), command.getImageUrl(),
                command.getCategory(), command.getRestaurantId(), true);
        Dish saved = dishRepositoryPort.save(dish);

        return new DishResponse(saved.getId(), saved.getName(), saved.getPrice(), saved.getDescription(),
                saved.getImageUrl(), saved.getCategory(), saved.getRestaurantId(), saved.isActive());
    }
}
