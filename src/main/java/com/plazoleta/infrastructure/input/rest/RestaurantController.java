package com.plazoleta.infrastructure.input.rest;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.plazoleta.application.dto.PageResult;
import com.plazoleta.application.dto.RestaurantListItem;
import com.plazoleta.application.dto.RestaurantResponse;
import com.plazoleta.application.handler.IRestaurantHandler;
import com.plazoleta.infrastructure.input.rest.dto.CreateRestaurantRequest;
import com.plazoleta.infrastructure.input.rest.mapper.RestaurantRequestMapper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/** Solo HTTP: recibe el request, lo valida con Jakarta, lo pasa al handler y arma la respuesta. */
@RestController
@RequestMapping("/api")
@Tag(name = "Restaurants")
public class RestaurantController {
    private final IRestaurantHandler restaurantHandler;

    public RestaurantController(IRestaurantHandler restaurantHandler) {
        this.restaurantHandler = restaurantHandler;
    }

    @PostMapping("/restaurants")
    @Operation(summary = "HU-2 Create restaurant", description = "ADMIN only. ownerUserId must be a user with the PROPIETARIO role.")
    public ResponseEntity<RestaurantResponse> createRestaurant(@Valid @RequestBody CreateRestaurantRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(restaurantHandler.createRestaurant(RestaurantRequestMapper.toCommand(request)));
    }

    @GetMapping("/restaurants")
    @Operation(summary = "HU-9 List restaurants", description = "CLIENTE only. Alphabetical order, paginated (page, size); returns name and logo.")
    public ResponseEntity<PageResult<RestaurantListItem>> listRestaurants(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(restaurantHandler.listRestaurants(page, size));
    }
}
