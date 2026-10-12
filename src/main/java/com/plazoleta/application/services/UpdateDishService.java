package com.plazoleta.application.services;

import com.plazoleta.application.dto.DishResponse;
import com.plazoleta.application.dto.UpdateDishCommand;
import com.plazoleta.application.ports.input.UpdateDishUseCase;
import com.plazoleta.application.ports.output.DishRepositoryPort;
import com.plazoleta.application.ports.output.RestaurantRepositoryPort;
import com.plazoleta.application.ports.output.UserRepositoryPort;
import com.plazoleta.domain.models.Role;
import com.plazoleta.domain.exceptions.DishNotFoundException;
import com.plazoleta.domain.exceptions.RestaurantOwnershipException;
import com.plazoleta.domain.models.Dish;
import com.plazoleta.domain.models.Restaurant;
import com.plazoleta.domain.models.User;

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
            throw new IllegalArgumentException("Request body is required");
        }
        User owner = userRepositoryPort.findById(authenticatedOwnerId)
                .orElseThrow(() -> new IllegalArgumentException("Authenticated user was not found"));
        if (owner.getRole() != Role.PROPIETARIO) {
            throw new RestaurantOwnershipException("Only an owner can modify dishes");
        }

        Dish dish = dishRepositoryPort.findById(dishId)
                .orElseThrow(() -> new DishNotFoundException("Dish was not found"));

        Restaurant restaurant = restaurantRepositoryPort.findById(dish.getRestaurantId())
                .orElseThrow(() -> new IllegalArgumentException("The dish's restaurant was not found"));

        if (!restaurant.getOwnerUserId().equals(authenticatedOwnerId)) {
            throw new RestaurantOwnershipException("The owner does not own this restaurant");
        }

        dish.updatePriceAndDescription(command.getPrice(), command.getDescription());
        Dish saved = dishRepositoryPort.save(dish);

        return new DishResponse(saved.getId(), saved.getName(), saved.getPrice(), saved.getDescription(),
                saved.getImageUrl(), saved.getCategory(), saved.getRestaurantId(), saved.isActive());
    }
}
