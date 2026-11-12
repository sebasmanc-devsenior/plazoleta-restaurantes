package com.plazoleta.infrastructure.input.rest;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.plazoleta.application.dto.CreateOrderCommand;
import com.plazoleta.application.dto.OrderResponse;
import com.plazoleta.application.dto.PageResult;
import com.plazoleta.application.port.input.CreateOrderUseCase;
import com.plazoleta.application.port.input.ListOrdersByStatusUseCase;
import com.plazoleta.infrastructure.configuration.security.AuthenticatedUserContext;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api")
@Tag(name = "Orders")
public class OrderController {
    private final CreateOrderUseCase createOrderUseCase;
    private final ListOrdersByStatusUseCase listOrdersByStatusUseCase;

    public OrderController(CreateOrderUseCase createOrderUseCase, ListOrdersByStatusUseCase listOrdersByStatusUseCase) {
        this.createOrderUseCase = createOrderUseCase;
        this.listOrdersByStatusUseCase = listOrdersByStatusUseCase;
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

    @GetMapping("/orders")
    @Operation(summary = "HU-12 List orders by status",
            description = "EMPLEADO only. Orders of the employee's restaurant (taken from the token) in the given status, "
                    + "oldest first, paginated (page, size).")
    public ResponseEntity<PageResult<OrderResponse>> listOrders(@RequestParam String status,
                                                                @RequestParam(defaultValue = "0") int page,
                                                                @RequestParam(defaultValue = "10") int size) {
        var principal = AuthenticatedUserContext.currentPrincipal();
        Long restaurantId = principal == null ? null : principal.getRestaurantId();
        return ResponseEntity.ok(listOrdersByStatusUseCase.execute(restaurantId, status, page, size));
    }
}
