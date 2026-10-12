package com.plazoleta.infrastructure.adapters.input.rest;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.plazoleta.application.dto.CreateDishCommand;
import com.plazoleta.application.dto.DishResponse;
import com.plazoleta.application.dto.UpdateDishCommand;
import com.plazoleta.application.ports.input.CreateDishUseCase;
import com.plazoleta.application.ports.input.SetDishAvailabilityUseCase;
import com.plazoleta.application.ports.input.UpdateDishUseCase;
import com.plazoleta.infrastructure.security.AuthenticatedUserContext;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api")
@Tag(name = "Dishes")
public class DishController {
    private final CreateDishUseCase createDishUseCase;
    private final UpdateDishUseCase updateDishUseCase;
    private final SetDishAvailabilityUseCase setDishAvailabilityUseCase;

    public DishController(CreateDishUseCase createDishUseCase, UpdateDishUseCase updateDishUseCase,
                          SetDishAvailabilityUseCase setDishAvailabilityUseCase) {
        this.createDishUseCase = createDishUseCase;
        this.updateDishUseCase = updateDishUseCase;
        this.setDishAvailabilityUseCase = setDishAvailabilityUseCase;
    }

    @PostMapping("/dishes")
    @Operation(summary = "HU-3 Create dish", description = "Restaurant OWNER only. New dishes are active.")
    public ResponseEntity<DishResponse> createDish(@RequestBody CreateDishCommand command, HttpServletRequest request) {
        Long ownerId = AuthenticatedUserContext.resolveUserId(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createDishUseCase.execute(command, ownerId));
    }

    @PutMapping("/dishes/{dishId}")
    @Operation(summary = "HU-4 Update dish", description = "Restaurant OWNER only. Only price and description can change.")
    public ResponseEntity<DishResponse> updateDish(@PathVariable Long dishId, @RequestBody UpdateDishCommand command,
                                                   HttpServletRequest request) {
        Long ownerId = AuthenticatedUserContext.resolveUserId(request);
        return ResponseEntity.ok(updateDishUseCase.execute(dishId, command, ownerId));
    }

    @PatchMapping("/dishes/{dishId}/availability")
    @Operation(summary = "HU-7 Enable or disable dish", description = "Only the OWNER of the dish's restaurant.")
    public ResponseEntity<DishResponse> setAvailability(@PathVariable Long dishId,
                                                        @RequestBody AvailabilityRequest command,
                                                        HttpServletRequest request) {
        Long ownerId = AuthenticatedUserContext.resolveUserId(request);
        return ResponseEntity.ok(setDishAvailabilityUseCase.execute(dishId, command.isActive(), ownerId));
    }

    public static class AvailabilityRequest {
        private boolean active;

        public boolean isActive() { return active; }
        public void setActive(boolean active) { this.active = active; }
    }
}
