package com.plazoleta.application.service;

import com.plazoleta.application.dto.CreateRestaurantCommand;
import com.plazoleta.application.dto.RestaurantResponse;
import com.plazoleta.application.port.input.CreateRestaurantUseCase;
import com.plazoleta.application.port.output.RestaurantRepositoryPort;
import com.plazoleta.application.port.output.UserRepositoryPort;
import com.plazoleta.domain.model.Role;
import com.plazoleta.domain.exception.UserNotFoundException;
import com.plazoleta.domain.model.Restaurant;
import com.plazoleta.domain.model.User;

public class CreateRestaurantService implements CreateRestaurantUseCase {
    private final RestaurantRepositoryPort restaurantRepositoryPort;
    private final UserRepositoryPort userRepositoryPort;

    public CreateRestaurantService(RestaurantRepositoryPort restaurantRepositoryPort, UserRepositoryPort userRepositoryPort) {
        this.restaurantRepositoryPort = restaurantRepositoryPort;
        this.userRepositoryPort = userRepositoryPort;
    }

    @Override
    public RestaurantResponse execute(CreateRestaurantCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("Request body is required");
        }
        if (command.getOwnerUserId() == null) {
            throw new IllegalArgumentException("Restaurant owner is required");
        }

        User owner = userRepositoryPort.findById(command.getOwnerUserId())
                .orElseThrow(() -> new UserNotFoundException("Owner user was not found"));

        if (owner.getRole() != Role.PROPIETARIO) {
            throw new IllegalArgumentException("The user must have the PROPIETARIO role");
        }

        Restaurant restaurant = new Restaurant(null, command.getName(), command.getNit(), command.getAddress(),
                command.getPhone(), command.getLogoUrl(), owner.getId());

        Restaurant saved = restaurantRepositoryPort.save(restaurant);

        return new RestaurantResponse(saved.getId(), saved.getName(), saved.getNit(), saved.getAddress(),
                saved.getPhone(), saved.getLogoUrl(), saved.getOwnerUserId());
    }
}
