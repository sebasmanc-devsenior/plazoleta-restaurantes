package com.plazoleta.application.usecase;

import com.plazoleta.domain.util.MensajesError;

import com.plazoleta.application.dto.DishResponse;
import com.plazoleta.application.port.input.SetDishAvailabilityPort;
import com.plazoleta.application.port.output.DishRepositoryPort;
import com.plazoleta.application.port.output.RestaurantRepositoryPort;
import com.plazoleta.application.port.output.UserRepositoryPort;
import com.plazoleta.domain.model.Role;
import com.plazoleta.domain.exception.DishNotFoundException;
import com.plazoleta.domain.exception.RestaurantOwnershipException;

public class SetDishAvailabilityUseCase implements SetDishAvailabilityPort {
    private final DishRepositoryPort dishRepositoryPort;
    private final RestaurantRepositoryPort restaurantRepositoryPort;
    private final UserRepositoryPort userRepositoryPort;

    public SetDishAvailabilityUseCase(DishRepositoryPort dishRepositoryPort,
                                     RestaurantRepositoryPort restaurantRepositoryPort,
                                     UserRepositoryPort userRepositoryPort) {
        this.dishRepositoryPort = dishRepositoryPort;
        this.restaurantRepositoryPort = restaurantRepositoryPort;
        this.userRepositoryPort = userRepositoryPort;
    }

    @Override
    public DishResponse execute(Long dishId, boolean active, Long authenticatedOwnerId) {
        var owner = userRepositoryPort.findById(authenticatedOwnerId)
                .orElseThrow(() -> new RestaurantOwnershipException(MensajesError.USUARIO_AUTENTICADO_NO_EXISTE));
        if (owner.getRole() != Role.PROPIETARIO) {
            throw new RestaurantOwnershipException(MensajesError.SOLO_UN_PROPIETARIO_PUEDE_HABILITAR_O_DESHABILITAR);
        }
        var dish = dishRepositoryPort.findById(dishId)
                .orElseThrow(() -> new DishNotFoundException(MensajesError.PLATO_NO_EXISTE));
        var restaurant = restaurantRepositoryPort.findById(dish.getRestaurantId())
                .orElseThrow(() -> new RestaurantOwnershipException(MensajesError.RESTAURANTE_DEL_PLATO_NO_EXISTE));
        if (!restaurant.getOwnerUserId().equals(authenticatedOwnerId)) {
            throw new RestaurantOwnershipException(MensajesError.NO_PUEDES_MODIFICAR_PLATOS_DE_OTRO_RESTAURANTE);
        }
        if (active) {
            dish.activate();
        } else {
            dish.deactivate();
        }
        var saved = dishRepositoryPort.save(dish);
        return new DishResponse(saved.getId(), saved.getName(), saved.getPrice(), saved.getDescription(),
                saved.getImageUrl(), saved.getCategory(), saved.getRestaurantId(), saved.isActive());
    }
}
