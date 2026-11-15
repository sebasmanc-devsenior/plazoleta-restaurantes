package com.plazoleta.infrastructure.input.rest;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.plazoleta.application.dto.OrderResponse;
import com.plazoleta.application.dto.PageResult;
import com.plazoleta.application.handler.IOrderHandler;
import com.plazoleta.infrastructure.configuration.security.AuthenticatedUserContext;
import com.plazoleta.infrastructure.input.rest.dto.CreateOrderRequest;
import com.plazoleta.infrastructure.input.rest.dto.DeliverOrderRequest;
import com.plazoleta.infrastructure.input.rest.mapper.OrderRequestMapper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/** Solo HTTP: valida el request con Jakarta, toma del token quién llama y su restaurante, y llama al handler. */
@RestController
@RequestMapping("/api")
@Tag(name = "Orders")
public class OrderController {
    private final IOrderHandler orderHandler;

    public OrderController(IOrderHandler orderHandler) {
        this.orderHandler = orderHandler;
    }

    @PostMapping("/orders")
    @Operation(summary = "HU-11 Create order",
            description = "CLIENTE only. Dishes of one restaurant with their quantity; the order starts as PENDIENTE. "
                    + "The client account is checked against the users service. "
                    + "Rejected with 409 while the client has another order PENDIENTE, EN_PREPARACION or LISTO.")
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest body,
                                                     HttpServletRequest request) {
        Long clientId = AuthenticatedUserContext.resolveUserId(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(orderHandler.createOrder(OrderRequestMapper.toCommand(body), clientId));
    }

    @GetMapping("/orders")
    @Operation(summary = "HU-12 List orders by status",
            description = "EMPLEADO only. Orders of the employee's restaurant (taken from the token) in the given status, "
                    + "oldest first, paginated (page, size).")
    public ResponseEntity<PageResult<OrderResponse>> listOrders(@RequestParam String status,
                                                                @RequestParam(defaultValue = "0") int page,
                                                                @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(orderHandler.listOrdersByStatus(employeeRestaurantId(), status, page, size));
    }

    @PatchMapping("/orders/{orderId}/assign")
    @Operation(summary = "HU-13 Assign order to myself",
            description = "EMPLEADO only. The employee in the token becomes the assigned employee and the order goes "
                    + "from PENDIENTE to EN_PREPARACION. Only orders of the employee's restaurant.")
    public ResponseEntity<OrderResponse> assignOrder(@PathVariable Long orderId, HttpServletRequest request) {
        Long employeeId = AuthenticatedUserContext.resolveUserId(request);
        return ResponseEntity.ok(orderHandler.assignOrder(orderId, employeeId, employeeRestaurantId()));
    }

    @PatchMapping("/orders/{orderId}/ready")
    @Operation(summary = "HU-14 Mark order as ready",
            description = "Only the assigned EMPLEADO. The order goes from EN_PREPARACION to LISTO and the client gets an SMS "
                    + "with the security PIN to claim it. The PIN is never returned by the API.")
    public ResponseEntity<OrderResponse> markOrderReady(@PathVariable Long orderId, HttpServletRequest request) {
        Long employeeId = AuthenticatedUserContext.resolveUserId(request);
        return ResponseEntity.ok(orderHandler.markOrderReady(orderId, employeeId, employeeRestaurantId()));
    }

    @PatchMapping("/orders/{orderId}/deliver")
    @Operation(summary = "HU-15 Deliver order",
            description = "Only the assigned EMPLEADO. The order goes from LISTO to ENTREGADO when the PIN typed by "
                    + "the employee is the one the client received by SMS. ENTREGADO is final.")
    public ResponseEntity<OrderResponse> deliverOrder(@PathVariable Long orderId,
                                                      @Valid @RequestBody DeliverOrderRequest body,
                                                      HttpServletRequest request) {
        Long employeeId = AuthenticatedUserContext.resolveUserId(request);
        return ResponseEntity.ok(orderHandler.deliverOrder(orderId, OrderRequestMapper.toCommand(body), employeeId,
                employeeRestaurantId()));
    }

    @PatchMapping("/orders/{orderId}/cancel")
    @Operation(summary = "HU-16 Cancel order",
            description = "CLIENTE only, and only their own order. A PENDIENTE order becomes CANCELADO; in any other "
                    + "status the answer is 409 with the message that it can no longer be cancelled.")
    public ResponseEntity<OrderResponse> cancelOrder(@PathVariable Long orderId, HttpServletRequest request) {
        Long clientId = AuthenticatedUserContext.resolveUserId(request);
        return ResponseEntity.ok(orderHandler.cancelOrder(orderId, clientId));
    }

    /** Restaurante del empleado, tomado del token (null si el token no lo trae). */
    private static Long employeeRestaurantId() {
        var principal = AuthenticatedUserContext.currentPrincipal();
        return principal == null ? null : principal.getRestaurantId();
    }
}
