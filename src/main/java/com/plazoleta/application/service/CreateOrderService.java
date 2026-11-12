package com.plazoleta.application.service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

import com.plazoleta.application.dto.CreateOrderCommand;
import com.plazoleta.application.dto.OrderResponse;
import com.plazoleta.application.port.input.CreateOrderUseCase;
import com.plazoleta.application.port.output.DishRepositoryPort;
import com.plazoleta.application.port.output.OrderRepositoryPort;
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
    private final Clock clock;

    public CreateOrderService(OrderRepositoryPort orderRepositoryPort, DishRepositoryPort dishRepositoryPort,
                              RestaurantRepositoryPort restaurantRepositoryPort, UserRepositoryPort userRepositoryPort,
                              Clock clock) {
        this.orderRepositoryPort = orderRepositoryPort;
        this.dishRepositoryPort = dishRepositoryPort;
        this.restaurantRepositoryPort = restaurantRepositoryPort;
        this.userRepositoryPort = userRepositoryPort;
        this.clock = clock;
    }

    @Override
    public OrderResponse execute(CreateOrderCommand command, Long clientId) {
        if (command == null) {
            throw new IllegalArgumentException("Request body is required");
        }
        if (command.getRestaurantId() == null) {
            throw new IllegalArgumentException("Restaurant is required");
        }
        if (command.getDishes() == null || command.getDishes().isEmpty()) {
            throw new IllegalArgumentException("An order needs at least one dish");
        }
        List<OrderItem> items = command.getDishes().stream()
                .map(item -> {
                    if (item == null) {
                        throw new IllegalArgumentException("Each order item needs a dishId");
                    }
                    return new OrderItem(item.getDishId(), item.getQuantity());
                })
                .toList();
        Order order = Order.create(clientId, command.getRestaurantId(), items, LocalDateTime.now(clock));

        // Conexión con el microservicio de usuarios: la cuenta del cliente debe existir y tener rol CLIENTE.
        User client = userRepositoryPort.findRegisteredById(clientId)
                .orElseThrow(() -> new UserNotFoundException("Client account was not found"));
        if (client.getRole() != Role.CLIENTE) {
            throw new ForbiddenOperationException("Only a client can place orders");
        }
        if (restaurantRepositoryPort.findById(order.getRestaurantId()).isEmpty()) {
            throw new RestaurantNotFoundException("Restaurant was not found");
        }
        if (orderRepositoryPort.existsByClientIdAndStatusIn(clientId, OrderStatus.IN_PROCESS)) {
            throw new ActiveOrderExistsException(
                    "You already have an order in progress (pending, in preparation or ready)");
        }
        for (OrderItem item : order.getItems()) {
            Dish dish = dishRepositoryPort.findById(item.getDishId())
                    .orElseThrow(() -> new DishNotFoundException("Dish " + item.getDishId() + " was not found"));
            if (!dish.getRestaurantId().equals(order.getRestaurantId())) {
                throw new IllegalArgumentException("All dishes must belong to the selected restaurant");
            }
            if (!dish.isActive()) {
                throw new IllegalArgumentException("Dish " + dish.getId() + " is not available");
            }
        }
        return OrderResponse.from(orderRepositoryPort.save(order));
    }
}
