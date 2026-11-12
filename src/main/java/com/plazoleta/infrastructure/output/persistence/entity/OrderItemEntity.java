package com.plazoleta.infrastructure.output.persistence.entity;

import com.plazoleta.domain.model.OrderItem;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/** HU-11: tabla order_items, un plato del pedido con su cantidad. */
@Entity
@Table(name = "order_items")
public class OrderItemEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false, foreignKey = @ForeignKey(name = "fk_order_items_order"))
    private OrderEntity order;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "dish_id", nullable = false, foreignKey = @ForeignKey(name = "fk_order_items_dish"))
    private DishEntity dish;

    @Column(nullable = false)
    private int quantity;

    public OrderItemEntity() {
    }

    OrderItemEntity(OrderEntity order, DishEntity dish, int quantity) {
        this.order = order;
        this.dish = dish;
        this.quantity = quantity;
    }

    OrderItem toDomain() {
        return new OrderItem(dish.getId(), quantity);
    }
}
