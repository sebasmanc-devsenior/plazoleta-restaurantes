package com.plazoleta.infrastructure.output.persistence.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.plazoleta.domain.model.Dish;
import com.plazoleta.domain.model.Order;
import com.plazoleta.domain.model.OrderItem;
import com.plazoleta.domain.model.OrderStatus;
import com.plazoleta.domain.model.Restaurant;
import com.plazoleta.infrastructure.output.persistence.entity.DishEntity;
import com.plazoleta.infrastructure.output.persistence.entity.OrderEntity;
import com.plazoleta.infrastructure.output.persistence.entity.RestaurantEntity;

/** Los mappers pasan del dominio a la entidad JPA y de vuelta sin perder ningún dato. */
class EntityMappersTest {

    @Test
    void restaurantGoesToTheEntityAndBackWithAllItsFields() {
        Restaurant restaurant = new Restaurant(3L, "Sabor Paisa", "900123456", "Calle 10 # 20-30", "+573001112233",
                "https://logo.png", 7L);

        RestaurantEntity entity = RestaurantEntityMapper.toEntity(restaurant);

        assertThat(entity.getOwnerUserId()).isEqualTo(7L);
        assertThat(RestaurantEntityMapper.toDomain(entity)).usingRecursiveComparison().isEqualTo(restaurant);
    }

    @Test
    void dishKeepsItsRestaurantAndItsAvailability() {
        Dish dish = new Dish(9L, "Bandeja paisa", 32000, "Con arepa", "https://img.png", "Almuerzos", 3L, false);

        DishEntity entity = DishEntityMapper.toEntity(dish);

        assertThat(entity.getRestaurantId()).isEqualTo(3L);
        assertThat(entity.isActive()).isFalse();
        assertThat(DishEntityMapper.toDomain(entity)).usingRecursiveComparison().isEqualTo(dish);
    }

    @Test
    void orderKeepsItsDishesEmployeeAndPin() {
        Order order = new Order(20L, 5L, 3L, OrderStatus.LISTO, LocalDateTime.of(2026, 11, 15, 12, 30),
                List.of(new OrderItem(9L, 2), new OrderItem(10L, 1)), 8L, "048213");

        OrderEntity entity = OrderEntityMapper.toEntity(order);

        assertThat(entity.getItems()).hasSize(2);
        assertThat(entity.getItems()).allSatisfy(item -> assertThat(item.getDishId()).isIn(9L, 10L));
        assertThat(OrderEntityMapper.toDomain(entity)).usingRecursiveComparison().isEqualTo(order);
    }

    @Test
    void anOrderInProcessKeepsTheClientInActiveClientIdAndAFinishedOneDoesNot() {
        LocalDateTime now = LocalDateTime.of(2026, 11, 15, 12, 30);
        Order pending = new Order(21L, 5L, 3L, OrderStatus.PENDIENTE, now, List.of(new OrderItem(9L, 1)), null, null);
        Order delivered = new Order(22L, 5L, 3L, OrderStatus.ENTREGADO, now, List.of(new OrderItem(9L, 1)), 8L, "048213");

        assertThat(OrderEntityMapper.toEntity(pending).getActiveClientId()).isEqualTo(5L);
        assertThat(OrderEntityMapper.toEntity(delivered).getActiveClientId()).isNull();
    }
}
