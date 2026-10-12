package com.plazoleta.infrastructure.adapters.input.rest;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.plazoleta.application.dto.CreateDishCommand;
import com.plazoleta.application.dto.DishResponse;
import com.plazoleta.application.ports.input.CreateDishUseCase;
import com.plazoleta.infrastructure.security.AuthenticatedUserContext;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api")
@Tag(name = "Dishes")
public class DishController {
    private final CreateDishUseCase createDishUseCase;

    public DishController(CreateDishUseCase createDishUseCase) {
        this.createDishUseCase = createDishUseCase;
    }

    @PostMapping("/dishes")
    @Operation(summary = "HU-3 Create dish", description = "Restaurant OWNER only. New dishes are active.")
    public ResponseEntity<DishResponse> createDish(@RequestBody CreateDishCommand command, HttpServletRequest request) {
        Long ownerId = AuthenticatedUserContext.resolveUserId(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createDishUseCase.execute(command, ownerId));
    }
}
