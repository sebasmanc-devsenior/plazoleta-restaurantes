package com.plazoleta.application.service;

import com.plazoleta.application.dto.DishResponse;
import com.plazoleta.application.port.input.SetDishAvailabilityUseCase;
import com.plazoleta.application.port.output.DishRepositoryPort;
import com.plazoleta.application.port.output.RestaurantRepositoryPort;
import com.plazoleta.application.port.output.UserRepositoryPort;
import com.plazoleta.domain.model.Role;
import com.plazoleta.domain.exception.DishNotFoundException;
import com.plazoleta.domain.exception.RestaurantOwnershipException;

public class SetDishAvailabilityService implements SetDishAvailabilityUseCase {
    private final DishRepositoryPort dishRepositoryPort;
    private final RestaurantRepositoryPort restaurantRepositoryPort;
    private final UserRepositoryPort userRepositoryPort;

    public SetDishAvailabilityService(DishRepositoryPort dishRepositoryPort,
                                     RestaurantRepositoryPort restaurantRepositoryPort,
                                     UserRepositoryPort userRepositoryPort) {
        this.dishRepositoryPort = dishRepositoryPort;
        this.restaurantRepositoryPort = restaurantRepositoryPort;
        this.userRepositoryPort = userRepositoryPort;
    }

    @Override
    public DishResponse execute(Long dishId, boolean active, Long authenticatedOwnerId) {
        var owner = userRepositoryPort.findById(authenticatedOwnerId)
                .orElseThrow(() -> new RestaurantOwnershipException("Authenticated user was not found"));
        if (owner.getRole() != Role.PROPIETARIO) {
            throw new RestaurantOwnershipException("Only an owner can enable or disable dishes");
        }
        var dish = dishRepositoryPort.findById(dishId)
                .orElseThrow(() -> new DishNotFoundException("Dish was not found"));
        var restaurant = restaurantRepositoryPort.findById(dish.getRestaurantId())
                .orElseThrow(() -> new RestaurantOwnershipException("The dish's restaurant was not found"));
        if (!restaurant.getOwnerUserId().equals(authenticatedOwnerId)) {
            throw new RestaurantOwnershipException("You cannot modify dishes from another restaurant");
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
