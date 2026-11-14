package com.plazoleta.application.service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

import com.plazoleta.application.dto.CreateOrderCommand;
import com.plazoleta.application.dto.OrderResponse;
import com.plazoleta.application.port.input.CreateOrderUseCase;
import com.plazoleta.application.port.output.DishRepositoryPort;
import com.plazoleta.application.port.output.OrderRepositoryPort;
import com.plazoleta.application.port.output.OrderTraceabilityPort;
import com.plazoleta.application.port.output.RestaurantRepositoryPort;
import com.plazoleta.application.port.output.UserRepositoryPort;
import com.plazoleta.domain.exception.ActiveOrderExistsException;
import com.plazoleta.domain.exception.DishNotFoundException;
import com.plazoleta.domain.exception.ForbiddenOperationException;
import com.plazoleta.domain.exception.RestaurantNotFoundException;
import com.plazoleta.domain.exception.UserNotFoundException;
import com.plazoleta.domain.model.Dish;
import com.plazoleta.domain.model.Order;
import com.plazoleta.domain.model.OrderItem;
import com.plazoleta.domain.model.OrderStatus;
import com.plazoleta.domain.model.Role;
import com.plazoleta.domain.model.User;

public class CreateOrderService implements CreateOrderUseCase {
    private final OrderRepositoryPort orderRepositoryPort;
    private final DishRepositoryPort dishRepositoryPort;
    private final RestaurantRepositoryPort restaurantRepositoryPort;
    private final UserRepositoryPort userRepositoryPort;
    private final OrderTraceabilityPort orderTraceabilityPort;
    private final Clock clock;

    public CreateOrderService(OrderRepositoryPort orderRepositoryPort, DishRepositoryPort dishRepositoryPort,
                              RestaurantRepositoryPort restaurantRepositoryPort, UserRepositoryPort userRepositoryPort,
                              OrderTraceabilityPort orderTraceabilityPort, Clock clock) {
        this.orderRepositoryPort = orderRepositoryPort;
        this.dishRepositoryPort = dishRepositoryPort;
        this.restaurantRepositoryPort = restaurantRepositoryPort;
        this.userRepositoryPort = userRepositoryPort;
        this.orderTraceabilityPort = orderTraceabilityPort;
        this.clock = clock;
    }

    @Override
    public OrderResponse execute(CreateOrderCommand command, Long clientId) {
        if (command == null) {
            throw new IllegalArgumentException("El cuerpo de la petición es obligatorio");
        }
        if (command.getRestaurantId() == null) {
            throw new IllegalArgumentException("El restaurante es obligatorio");
        }
        if (command.getDishes() == null || command.getDishes().isEmpty()) {
            throw new IllegalArgumentException("El pedido debe tener al menos un plato");
        }
        List<OrderItem> items = command.getDishes().stream()
                .map(item -> {
                    if (item == null) {
                        throw new IllegalArgumentException("Cada plato del pedido necesita un dishId");
                    }
                    return new OrderItem(item.getDishId(), item.getQuantity());
                })
                .toList();
        Order order = Order.create(clientId, command.getRestaurantId(), items, LocalDateTime.now(clock));

        // Conexión con el microservicio de usuarios: la cuenta del cliente debe existir y tener rol CLIENTE.
        User client = userRepositoryPort.findRegisteredById(clientId)
                .orElseThrow(() -> new UserNotFoundException("La cuenta del cliente no existe"));
        if (client.getRole() != Role.CLIENTE) {
            throw new ForbiddenOperationException("Solo un cliente puede hacer pedidos");
        }
        if (restaurantRepositoryPort.findById(order.getRestaurantId()).isEmpty()) {
            throw new RestaurantNotFoundException("El restaurante no existe");
        }
        if (orderRepositoryPort.existsByClientIdAndStatusIn(clientId, OrderStatus.IN_PROCESS)) {
            throw new ActiveOrderExistsException(
                    "Ya tienes un pedido en proceso (pendiente, en preparación o listo)");
        }
        for (OrderItem item : order.getItems()) {
            Dish dish = dishRepositoryPort.findById(item.getDishId())
                    .orElseThrow(() -> new DishNotFoundException("El plato " + item.getDishId() + " no existe"));
            if (!dish.getRestaurantId().equals(order.getRestaurantId())) {
                throw new IllegalArgumentException("Todos los platos deben ser del restaurante seleccionado");
            }
            if (!dish.isActive()) {
                throw new IllegalArgumentException("El plato " + dish.getId() + " no está disponible");
            }
        }
        Order saved = orderRepositoryPort.save(order);
        // HU-17: primer registro del pedido, sin estado anterior.
        orderTraceabilityPort.record(saved, null);
        return OrderResponse.from(saved);
    }
}
