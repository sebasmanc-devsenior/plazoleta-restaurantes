package com.plazoleta.infrastructure.output.persistence.mapper;

import com.plazoleta.domain.model.Order;
import com.plazoleta.domain.model.OrderItem;
import com.plazoleta.domain.model.OrderStatus;
import com.plazoleta.infrastructure.output.persistence.entity.DishEntity;
import com.plazoleta.infrastructure.output.persistence.entity.OrderEntity;
import com.plazoleta.infrastructure.output.persistence.entity.OrderItemEntity;

/** Convierte entre el modelo del dominio Order (con sus platos) y las entidades JPA de orders y order_items. */
public final class OrderEntityMapper {
    private OrderEntityMapper() {
    }

    public static OrderEntity toEntity(Order order) {
        OrderEntity entity = new OrderEntity();
        entity.setId(order.getId());
        entity.setClientId(order.getClientId());
        entity.setRestaurantId(order.getRestaurantId());
        entity.setStatus(order.getStatus());
        entity.setCreatedAt(order.getCreatedAt());
        entity.setEmployeeId(order.getEmployeeId());
        entity.setSecurityPin(order.getSecurityPin());
        entity.setActiveClientId(activeClientIdFor(order));
        order.getItems().forEach(item -> entity.getItems().add(
                new OrderItemEntity(entity, DishEntity.reference(item.getDishId()), item.getQuantity())));
        return entity;
    }

    public static Order toDomain(OrderEntity entity) {
        return new Order(entity.getId(), entity.getClientId(), entity.getRestaurantId(), entity.getStatus(),
                entity.getCreatedAt(),
                entity.getItems().stream().map(item -> new OrderItem(item.getDishId(), item.getQuantity())).toList(),
                entity.getEmployeeId(), entity.getSecurityPin());
    }

    /**
     * Valor de active_client_id: el id del cliente mientras el pedido está en proceso, null cuando terminó.
     * La columna es única, así MySQL impide que un cliente tenga dos pedidos en proceso (HU-11).
     */
    public static Long activeClientIdFor(Order order) {
        return OrderStatus.IN_PROCESS.contains(order.getStatus()) ? order.getClientId() : null;
    }
}
