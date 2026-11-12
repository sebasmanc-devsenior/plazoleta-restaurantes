package com.plazoleta.application.dto;

import java.util.List;

/** HU-11: restaurante elegido y los platos con su cantidad. */
public class CreateOrderCommand {
    private Long restaurantId;
    private List<OrderItemCommand> dishes;

    public CreateOrderCommand() {}

    public CreateOrderCommand(Long restaurantId, List<OrderItemCommand> dishes) {
        this.restaurantId = restaurantId;
        this.dishes = dishes;
    }

    public Long getRestaurantId() { return restaurantId; }
    public void setRestaurantId(Long restaurantId) { this.restaurantId = restaurantId; }
    public List<OrderItemCommand> getDishes() { return dishes; }
    public void setDishes(List<OrderItemCommand> dishes) { this.dishes = dishes; }
}
