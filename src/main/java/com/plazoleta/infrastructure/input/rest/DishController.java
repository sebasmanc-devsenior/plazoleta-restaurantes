package com.plazoleta.infrastructure.input.rest;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.plazoleta.application.dto.DishResponse;
import com.plazoleta.application.dto.PageResult;
import com.plazoleta.application.handler.IDishHandler;
import com.plazoleta.infrastructure.configuration.security.AuthenticatedUserContext;
import com.plazoleta.infrastructure.input.rest.dto.CreateDishRequest;
import com.plazoleta.infrastructure.input.rest.dto.DishAvailabilityRequest;
import com.plazoleta.infrastructure.input.rest.dto.UpdateDishRequest;
import com.plazoleta.infrastructure.input.rest.mapper.DishRequestMapper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/** Solo HTTP: valida el request con Jakarta, toma el id del propietario del token y llama al handler. */
@RestController
@RequestMapping("/api")
@Tag(name = "Dishes")
public class DishController {
    private final IDishHandler dishHandler;

    public DishController(IDishHandler dishHandler) {
        this.dishHandler = dishHandler;
    }

    @PostMapping("/dishes")
    @Operation(summary = "HU-3 Create dish", description = "Restaurant OWNER only. New dishes are active.")
    public ResponseEntity<DishResponse> createDish(@Valid @RequestBody CreateDishRequest body,
                                                   HttpServletRequest request) {
        Long ownerId = AuthenticatedUserContext.resolveUserId(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(dishHandler.createDish(DishRequestMapper.toCommand(body), ownerId));
    }

    @PutMapping("/dishes/{dishId}")
    @Operation(summary = "HU-4 Update dish", description = "Restaurant OWNER only. Only price and description can change.")
    public ResponseEntity<DishResponse> updateDish(@PathVariable Long dishId, @Valid @RequestBody UpdateDishRequest body,
                                                   HttpServletRequest request) {
        Long ownerId = AuthenticatedUserContext.resolveUserId(request);
        return ResponseEntity.ok(dishHandler.updateDish(dishId, DishRequestMapper.toCommand(body), ownerId));
    }

    @PatchMapping("/dishes/{dishId}/availability")
    @Operation(summary = "HU-7 Enable or disable dish", description = "Only the OWNER of the dish's restaurant.")
    public ResponseEntity<DishResponse> setAvailability(@PathVariable Long dishId,
                                                        @Valid @RequestBody DishAvailabilityRequest body,
                                                        HttpServletRequest request) {
        Long ownerId = AuthenticatedUserContext.resolveUserId(request);
        return ResponseEntity.ok(dishHandler.setDishAvailability(dishId, body.active(), ownerId));
    }

    @GetMapping("/restaurants/{restaurantId}/dishes")
    @Operation(summary = "HU-10 List restaurant dishes",
            description = "CLIENTE only. Dishes ordered by name, paginated (page, size), optional category filter. "
                    + "Disabled dishes are listed with active = false: they are visible but cannot be ordered.")
    public ResponseEntity<PageResult<DishResponse>> listRestaurantDishes(
            @PathVariable Long restaurantId,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(dishHandler.listRestaurantDishes(restaurantId, category, page, size));
    }
}
