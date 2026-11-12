package com.plazoleta.infrastructure.output.persistence.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.plazoleta.domain.model.Order;
import com.plazoleta.domain.model.OrderStatus;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

/** HU-11: tabla orders. client_id referencia a un usuario de otro microservicio, por eso no tiene llave foránea. */
@Entity
@Table(name = "orders", indexes = {
        @Index(name = "idx_orders_client_status", columnList = "client_id,status"),
        @Index(name = "idx_orders_restaurant_status", columnList = "restaurant_id,status")
})
public class OrderEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "client_id", nullable = false)
    private Long clientId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "restaurant_id", nullable = false, foreignKey = @ForeignKey(name = "fk_orders_restaurant"))
    private RestaurantEntity restaurant;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<OrderItemEntity> items = new ArrayList<>();

    public OrderEntity() {
    }

    public static OrderEntity fromDomain(Order order) {
        OrderEntity entity = new OrderEntity();
        entity.id = order.getId();
        entity.clientId = order.getClientId();
        entity.restaurant = RestaurantEntity.reference(order.getRestaurantId());
        entity.status = order.getStatus();
        entity.createdAt = order.getCreatedAt();
        order.getItems().forEach(item -> entity.items.add(
                new OrderItemEntity(entity, DishEntity.reference(item.getDishId()), item.getQuantity())));
        return entity;
    }

    public Order toDomain() {
        return new Order(id, clientId, restaurant.getId(), status, createdAt,
                items.stream().map(OrderItemEntity::toDomain).toList());
    }

    public Long getId() { return id; }
}
