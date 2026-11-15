package com.plazoleta.infrastructure.input.rest.mapper;

import java.util.List;

import com.plazoleta.application.dto.CreateOrderCommand;
import com.plazoleta.application.dto.DeliverOrderCommand;
import com.plazoleta.application.dto.OrderItemCommand;
import com.plazoleta.infrastructure.input.rest.dto.CreateOrderRequest;
import com.plazoleta.infrastructure.input.rest.dto.DeliverOrderRequest;

/** Convierte los requests HTTP de pedidos (ya validados con Jakarta) en comandos de la capa de aplicación. */
public final class OrderRequestMapper {
    private OrderRequestMapper() {
    }

    public static CreateOrderCommand toCommand(CreateOrderRequest request) {
        // Un plato nulo dentro de la lista se pasa tal cual: el caso de uso responde que le falta el dishId.
        List<OrderItemCommand> dishes = request.dishes().stream()
                .map(item -> item == null ? null : new OrderItemCommand(item.dishId(), item.quantity()))
                .toList();
        return new CreateOrderCommand(request.restaurantId(), dishes);
    }

    public static DeliverOrderCommand toCommand(DeliverOrderRequest request) {
        return new DeliverOrderCommand(request.pin());
    }
}
