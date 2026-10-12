package com.plazoleta.application.services;

import com.plazoleta.application.dto.CreateDishCommand;
import com.plazoleta.application.dto.DishResponse;
import com.plazoleta.application.ports.input.CreateDishUseCase;
import com.plazoleta.application.ports.output.DishRepositoryPort;
import com.plazoleta.application.ports.output.RestaurantRepositoryPort;
import com.plazoleta.application.ports.output.UserRepositoryPort;
import com.plazoleta.domain.models.Role;
import com.plazoleta.domain.exceptions.RestaurantNotFoundException;
import com.plazoleta.domain.exceptions.RestaurantOwnershipException;
import com.plazoleta.domain.models.Dish;
import com.plazoleta.domain.models.Restaurant;
import com.plazoleta.domain.models.User;

public class CreateDishService implements CreateDishUseCase {
    private final DishRepositoryPort dishRepositoryPort;
    private final RestaurantRepositoryPort restaurantRepositoryPort;
    private final UserRepositoryPort userRepositoryPort;

    public CreateDishService(DishRepositoryPort dishRepositoryPort, RestaurantRepositoryPort restaurantRepositoryPort, UserRepositoryPort userRepositoryPort) {
        this.dishRepositoryPort = dishRepositoryPort;
        this.restaurantRepositoryPort = restaurantRepositoryPort;
        this.userRepositoryPort = userRepositoryPort;
    }

    @Override
    public DishResponse execute(CreateDishCommand command, Long authenticatedOwnerId) {
        if (command == null) {
            throw new IllegalArgumentException("Request body is required");
        }

        User owner = userRepositoryPort.findById(authenticatedOwnerId)
                .orElseThrow(() -> new IllegalArgumentException("Authenticated user was not found"));
        if (owner.getRole() != Role.PROPIETARIO) {
            throw new RestaurantOwnershipException("Only an owner can create dishes");
        }

        Restaurant restaurant = restaurantRepositoryPort.findById(command.getRestaurantId())
                .orElseThrow(() -> new RestaurantNotFoundException("Restaurant was not found"));

        if (!restaurant.getOwnerUserId().equals(authenticatedOwnerId)) {
            throw new RestaurantOwnershipException("The owner does not own this restaurant");
        }

        Dish dish = new Dish(null, command.getName(), command.getPrice(), command.getDescription(), command.getImageUrl(),
                command.getCategory(), command.getRestaurantId(), true);
        Dish saved = dishRepositoryPort.save(dish);

        return new DishResponse(saved.getId(), saved.getName(), saved.getPrice(), saved.getDescription(),
                saved.getImageUrl(), saved.getCategory(), saved.getRestaurantId(), saved.isActive());
    }
}
