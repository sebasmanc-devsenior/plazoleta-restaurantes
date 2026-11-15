package com.plazoleta.application.service;

import com.plazoleta.application.dto.CreateDishCommand;
import com.plazoleta.application.dto.DishResponse;
import com.plazoleta.application.port.input.CreateDishUseCase;
import com.plazoleta.application.port.output.DishRepositoryPort;
import com.plazoleta.application.port.output.RestaurantRepositoryPort;
import com.plazoleta.application.port.output.UserRepositoryPort;
import com.plazoleta.domain.model.Role;
import com.plazoleta.domain.exception.RestaurantNotFoundException;
import com.plazoleta.domain.exception.RestaurantOwnershipException;
import com.plazoleta.domain.model.Dish;
import com.plazoleta.domain.model.Restaurant;
import com.plazoleta.domain.model.User;

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
            throw new IllegalArgumentException("El cuerpo de la petición es obligatorio");
        }

        User owner = userRepositoryPort.findById(authenticatedOwnerId)
                .orElseThrow(() -> new IllegalArgumentException("El usuario autenticado no existe"));
        if (owner.getRole() != Role.PROPIETARIO) {
            throw new RestaurantOwnershipException("Solo un propietario puede crear platos");
        }

        Restaurant restaurant = restaurantRepositoryPort.findById(command.getRestaurantId())
                .orElseThrow(() -> new RestaurantNotFoundException("El restaurante no existe"));

        if (!restaurant.getOwnerUserId().equals(authenticatedOwnerId)) {
            throw new RestaurantOwnershipException("El propietario no es dueño de este restaurante");
        }

        Dish dish = new Dish(null, command.getName(), command.getPrice(), command.getDescription(), command.getImageUrl(),
                command.getCategory(), command.getRestaurantId(), true);
        Dish saved = dishRepositoryPort.save(dish);

        return new DishResponse(saved.getId(), saved.getName(), saved.getPrice(), saved.getDescription(),
                saved.getImageUrl(), saved.getCategory(), saved.getRestaurantId(), saved.isActive());
    }
}
