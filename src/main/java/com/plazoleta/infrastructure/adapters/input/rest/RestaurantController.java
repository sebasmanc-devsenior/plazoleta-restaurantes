package com.plazoleta.infrastructure.adapters.input.rest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.plazoleta.application.dto.CreateRestaurantCommand;
import com.plazoleta.application.dto.PageResult;
import com.plazoleta.application.dto.RestaurantListItem;
import com.plazoleta.application.dto.RestaurantResponse;
import com.plazoleta.application.ports.input.CreateRestaurantUseCase;
import com.plazoleta.application.ports.input.ListRestaurantsUseCase;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api")
@Tag(name = "Restaurants")
public class RestaurantController {
    private final CreateRestaurantUseCase createRestaurantUseCase;
    private final ListRestaurantsUseCase listRestaurantsUseCase;

    public RestaurantController(CreateRestaurantUseCase createRestaurantUseCase,
                                ListRestaurantsUseCase listRestaurantsUseCase) {
        this.createRestaurantUseCase = createRestaurantUseCase;
        this.listRestaurantsUseCase = listRestaurantsUseCase;
    }

    @PostMapping("/restaurants")
    @Operation(summary = "HU-2 Create restaurant", description = "ADMIN only. ownerUserId must be a user with the PROPIETARIO role.")
    public ResponseEntity<RestaurantResponse> createRestaurant(@RequestBody CreateRestaurantCommand command) {
        return ResponseEntity.status(HttpStatus.CREATED).body(createRestaurantUseCase.execute(command));
    }

    @GetMapping("/restaurants")
    @Operation(summary = "HU-9 List restaurants", description = "CLIENTE only. Alphabetical order, paginated (page, size); returns name and logo.")
    public ResponseEntity<PageResult<RestaurantListItem>> listRestaurants(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(listRestaurantsUseCase.execute(page, size));
    }
}
