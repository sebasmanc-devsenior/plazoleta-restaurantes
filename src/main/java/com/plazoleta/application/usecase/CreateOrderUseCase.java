package com.plazoleta.application.usecase;

import com.plazoleta.domain.util.MensajesError;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

import com.plazoleta.application.dto.CreateOrderCommand;
import com.plazoleta.application.dto.OrderResponse;
import com.plazoleta.application.port.input.CreateOrderPort;
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

public class CreateOrderUseCase implements CreateOrderPort {
    private final OrderRepositoryPort orderRepositoryPort;
    private final DishRepositoryPort dishRepositoryPort;
    private final RestaurantRepositoryPort restaurantRepositoryPort;
    private final UserRepositoryPort userRepositoryPort;
    private final OrderTraceabilityPort orderTraceabilityPort;
    private final Clock clock;

    public CreateOrderUseCase(OrderRepositoryPort orderRepositoryPort, DishRepositoryPort dishRepositoryPort,
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
            throw new IllegalArgumentException(MensajesError.CUERPO_DE_LA_PETICION_ES_OBLIGATORIO);
        }
        if (command.getRestaurantId() == null) {
            throw new IllegalArgumentException(MensajesError.RESTAURANTE_ES_OBLIGATORIO);
        }
        if (command.getDishes() == null || command.getDishes().isEmpty()) {
            throw new IllegalArgumentException(MensajesError.PEDIDO_DEBE_TENER_AL_MENOS_UN_PLATO);
        }
        List<OrderItem> items = command.getDishes().stream()
                .map(item -> {
                    if (item == null) {
                        throw new IllegalArgumentException(MensajesError.CADA_PLATO_DEL_PEDIDO_NECESITA_UN_DISHID);
                    }
                    return new OrderItem(item.getDishId(), item.getQuantity());
                })
                .toList();
        Order order = Order.create(clientId, command.getRestaurantId(), items, LocalDateTime.now(clock));

        // Conexión con el microservicio de usuarios: la cuenta del cliente debe existir y tener rol CLIENTE.
        User client = userRepositoryPort.findRegisteredById(clientId)
                .orElseThrow(() -> new UserNotFoundException(MensajesError.CUENTA_DEL_CLIENTE_NO_EXISTE));
        if (client.getRole() != Role.CLIENTE) {
            throw new ForbiddenOperationException(MensajesError.SOLO_UN_CLIENTE_PUEDE_HACER_PEDIDOS);
        }
        if (restaurantRepositoryPort.findById(order.getRestaurantId()).isEmpty()) {
            throw new RestaurantNotFoundException(MensajesError.RESTAURANTE_NO_EXISTE);
        }
        if (orderRepositoryPort.existsByClientIdAndStatusIn(clientId, OrderStatus.IN_PROCESS)) {
            throw new ActiveOrderExistsException(
                    MensajesError.YA_TIENES_UN_PEDIDO_EN_PROCESO_PENDIENTE);
        }
        for (OrderItem item : order.getItems()) {
            Dish dish = dishRepositoryPort.findById(item.getDishId())
                    .orElseThrow(() -> new DishNotFoundException(String.format(MensajesError.PLATO_CON_ID_NO_EXISTE, item.getDishId())));
            if (!dish.getRestaurantId().equals(order.getRestaurantId())) {
                throw new IllegalArgumentException(MensajesError.TODOS_LOS_PLATOS_DEBEN_SER_DEL_RESTAURANTE);
            }
            if (!dish.isActive()) {
                throw new IllegalArgumentException(String.format(MensajesError.PLATO_NO_DISPONIBLE, dish.getId()));
            }
        }
        Order saved = orderRepositoryPort.save(order);
        // HU-17: primer registro del pedido, sin estado anterior.
        orderTraceabilityPort.record(saved, null);
        return OrderResponse.from(saved);
    }
}
