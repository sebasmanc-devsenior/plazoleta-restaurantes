package com.plazoleta.application.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.plazoleta.domain.model.Order;
import com.plazoleta.domain.model.OrderItem;
import com.plazoleta.domain.model.OrderStatus;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/** La respuesta del pedido usa su propio DTO de salida para los platos, sin cambiar el JSON que ve el cliente. */
class OrderResponseTest {
    private final Order order = new Order(5L, 7L, 1L, OrderStatus.PENDIENTE, LocalDateTime.of(2026, 11, 10, 12, 0),
            List.of(new OrderItem(10L, 2), new OrderItem(11L, 1)));

    @Test
    void eachDishOfTheOrderBecomesAnOrderItemResponse() {
        OrderResponse response = OrderResponse.from(order);

        assertThat(response.getDishes()).hasSize(2).allSatisfy(d -> assertThat(d).isInstanceOf(OrderItemResponse.class));
        assertThat(response.getDishes()).extracting(OrderItemResponse::getDishId).containsExactly(10L, 11L);
        assertThat(response.getDishes()).extracting(OrderItemResponse::getQuantity).containsExactly(2, 1);
    }

    @Test
    void theJsonStillShowsDishIdAndQuantityForEachDish() {
        JsonNode json = JsonMapper.builder().build().valueToTree(OrderResponse.from(order));

        assertThat(json.get("dishes").get(0).get("dishId").asLong()).isEqualTo(10L);
        assertThat(json.get("dishes").get(0).get("quantity").asInt()).isEqualTo(2);
        assertThat(json.get("dishes").get(0).size()).isEqualTo(2);
    }
}
