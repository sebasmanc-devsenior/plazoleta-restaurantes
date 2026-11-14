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
            throw new IllegalArgumentException("El cuerpo de la petición es obligatorio");
        }
        if (command.getOwnerUserId() == null) {
            throw new IllegalArgumentException("El propietario del restaurante es obligatorio");
        }

        User owner = userRepositoryPort.findById(command.getOwnerUserId())
                .orElseThrow(() -> new UserNotFoundException("El usuario propietario no existe"));

        if (owner.getRole() != Role.PROPIETARIO) {
            throw new IllegalArgumentException("El usuario debe tener el rol PROPIETARIO");
        }

        Restaurant restaurant = new Restaurant(null, command.getName(), command.getNit(), command.getAddress(),
                command.getPhone(), command.getLogoUrl(), owner.getId());

        Restaurant saved = restaurantRepositoryPort.save(restaurant);

        return new RestaurantResponse(saved.getId(), saved.getName(), saved.getNit(), saved.getAddress(),
                saved.getPhone(), saved.getLogoUrl(), saved.getOwnerUserId());
    }
}
