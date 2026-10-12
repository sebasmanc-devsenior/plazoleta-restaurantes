package com.plazoleta.infrastructure.adapters.input.rest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.plazoleta.application.dto.RestaurantResponse;
import com.plazoleta.application.ports.input.GetRestaurantUseCase;

/**
 * Rutas solo para otros microservicios (protegidas con el token interno, no con JWT de usuario).
 */
@io.swagger.v3.oas.annotations.Hidden
@RestController
@RequestMapping("/internal/restaurants")
public class InternalRestaurantController {
    private final GetRestaurantUseCase getRestaurantUseCase;

    public InternalRestaurantController(GetRestaurantUseCase getRestaurantUseCase) {
        this.getRestaurantUseCase = getRestaurantUseCase;
    }

    @GetMapping("/{restaurantId}")
    public ResponseEntity<RestaurantResponse> getRestaurant(@PathVariable Long restaurantId) {
        return ResponseEntity.ok(getRestaurantUseCase.execute(restaurantId));
    }
}
