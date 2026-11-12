package com.plazoleta.infrastructure.input.rest;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.plazoleta.application.dto.CreateOrderCommand;
import com.plazoleta.application.dto.OrderResponse;
import com.plazoleta.application.port.input.CreateOrderUseCase;
import com.plazoleta.infrastructure.configuration.security.AuthenticatedUserContext;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api")
@Tag(name = "Orders")
public class OrderController {
    private final CreateOrderUseCase createOrderUseCase;

    public OrderController(CreateOrderUseCase createOrderUseCase) {
        this.createOrderUseCase = createOrderUseCase;
    }

    @PostMapping("/orders")
    @Operation(summary = "HU-11 Create order",
            description = "CLIENTE only. Dishes of one restaurant with their quantity; the order starts as PENDIENTE. "
                    + "The client account is checked against the users service. "
                    + "Rejected with 409 while the client has another order PENDIENTE, EN_PREPARACION or LISTO.")
    public ResponseEntity<OrderResponse> createOrder(@RequestBody CreateOrderCommand command,
                                                     HttpServletRequest request) {
        Long clientId = AuthenticatedUserContext.resolveUserId(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createOrderUseCase.execute(command, clientId));
    }
}
