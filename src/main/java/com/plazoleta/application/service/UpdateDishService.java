package com.plazoleta.application.service;

import com.plazoleta.domain.util.MensajesError;

import com.plazoleta.application.dto.DishResponse;
import com.plazoleta.application.dto.UpdateDishCommand;
import com.plazoleta.application.port.input.UpdateDishUseCase;
import com.plazoleta.application.port.output.DishRepositoryPort;
import com.plazoleta.application.port.output.RestaurantRepositoryPort;
import com.plazoleta.application.port.output.UserRepositoryPort;
import com.plazoleta.domain.model.Role;
import com.plazoleta.domain.exception.DishNotFoundException;
import com.plazoleta.domain.exception.RestaurantOwnershipException;
import com.plazoleta.domain.model.Dish;
import com.plazoleta.domain.model.Restaurant;
import com.plazoleta.domain.model.User;

public class UpdateDishService implements UpdateDishUseCase {
    private final DishRepositoryPort dishRepositoryPort;
    private final RestaurantRepositoryPort restaurantRepositoryPort;
    private final UserRepositoryPort userRepositoryPort;

    public UpdateDishService(DishRepositoryPort dishRepositoryPort, RestaurantRepositoryPort restaurantRepositoryPort, UserRepositoryPort userRepositoryPort) {
        this.dishRepositoryPort = dishRepositoryPort;
        this.restaurantRepositoryPort = restaurantRepositoryPort;
        this.userRepositoryPort = userRepositoryPort;
    }

    @Override
    public DishResponse execute(Long dishId, UpdateDishCommand command, Long authenticatedOwnerId) {
        if (command == null) {
            throw new IllegalArgumentException(MensajesError.CUERPO_DE_LA_PETICION_ES_OBLIGATORIO);
        }
        User owner = userRepositoryPort.findById(authenticatedOwnerId)
                .orElseThrow(() -> new IllegalArgumentException(MensajesError.USUARIO_AUTENTICADO_NO_EXISTE));
        if (owner.getRole() != Role.PROPIETARIO) {
            throw new RestaurantOwnershipException(MensajesError.SOLO_UN_PROPIETARIO_PUEDE_MODIFICAR_PLATOS);
        }

        Dish dish = dishRepositoryPort.findById(dishId)
                .orElseThrow(() -> new DishNotFoundException(MensajesError.PLATO_NO_EXISTE));

        Restaurant restaurant = restaurantRepositoryPort.findById(dish.getRestaurantId())
                .orElseThrow(() -> new IllegalArgumentException(MensajesError.RESTAURANTE_DEL_PLATO_NO_EXISTE));

        if (!restaurant.getOwnerUserId().equals(authenticatedOwnerId)) {
            throw new RestaurantOwnershipException(MensajesError.PROPIETARIO_NO_ES_DUENO_DE_ESTE_RESTAURANTE);
        }

        dish.updatePriceAndDescription(command.getPrice(), command.getDescription());
        Dish saved = dishRepositoryPort.save(dish);

        return new DishResponse(saved.getId(), saved.getName(), saved.getPrice(), saved.getDescription(),
                saved.getImageUrl(), saved.getCategory(), saved.getRestaurantId(), saved.isActive());
    }
}
