package com.plazoleta.infrastructure.output.persistence.adapter;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.plazoleta.application.port.output.DishRepositoryPort;
import com.plazoleta.application.port.output.OrderRepositoryPort;
import com.plazoleta.application.port.output.RestaurantRepositoryPort;
import com.plazoleta.domain.model.Dish;
import com.plazoleta.domain.model.Order;
import com.plazoleta.domain.model.OrderItem;
import com.plazoleta.domain.model.OrderStatus;
import com.plazoleta.domain.model.Restaurant;

/** HU-11: el adaptador guarda el pedido con sus platos y detecta pedidos en proceso del cliente. */
@SpringBootTest
@Transactional
class OrderPersistenceAdapterTests {
    @Autowired
    private RestaurantRepositoryPort restaurants;
    @Autowired
    private DishRepositoryPort dishes;
    @Autowired
    private OrderRepositoryPort orders;

    @Test
    void savesOrderWithItemsAndFindsClientOrdersInProcess() {
        Long restaurantId = restaurants.save(new Restaurant(null, "Sabor Paisa", "900123456", "Calle 10",
                "+573001112233", "https://logo.png", 2L)).getId();
        Long dishId = dishes.save(new Dish(null, "Bandeja", 32000, "d", "https://i.png", "Almuerzos",
                restaurantId, true)).getId();

        Order saved = orders.save(Order.create(7L, restaurantId, List.of(new OrderItem(dishId, 3)),
                LocalDateTime.of(2026, 11, 10, 12, 0)));

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getStatus()).isEqualTo(OrderStatus.PENDIENTE);
        assertThat(saved.getItems()).singleElement().satisfies(item -> {
            assertThat(item.getDishId()).isEqualTo(dishId);
            assertThat(item.getQuantity()).isEqualTo(3);
        });
        assertThat(orders.existsByClientIdAndStatusIn(7L, OrderStatus.IN_PROCESS)).isTrue();
        assertThat(orders.existsByClientIdAndStatusIn(8L, OrderStatus.IN_PROCESS)).isFalse();
        assertThat(orders.existsByClientIdAndStatusIn(7L, Set.of(OrderStatus.ENTREGADO))).isFalse();
    }
}
