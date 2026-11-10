package com.plazoleta.application.dto;

public class OrderItemCommand {
    private Long dishId;
    private Integer quantity;

    public OrderItemCommand() {}

    public OrderItemCommand(Long dishId, Integer quantity) {
        this.dishId = dishId;
        this.quantity = quantity;
    }

    public Long getDishId() { return dishId; }
    public void setDishId(Long dishId) { this.dishId = dishId; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
}
