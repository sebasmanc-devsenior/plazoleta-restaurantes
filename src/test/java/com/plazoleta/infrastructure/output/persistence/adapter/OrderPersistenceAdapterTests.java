package com.plazoleta.infrastructure.output.persistence.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
import com.plazoleta.domain.exception.ActiveOrderExistsException;
import com.plazoleta.domain.model.Dish;
import com.plazoleta.domain.model.Order;
import com.plazoleta.domain.model.OrderItem;
import com.plazoleta.domain.model.OrderStatus;
import com.plazoleta.domain.model.Restaurant;

import jakarta.persistence.EntityManager;

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
    @Autowired
    private EntityManager entityManager;

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

    /** HU-12: solo pedidos del restaurante y del estado pedido, del más antiguo al más reciente. */
    @Test
    void findsOrdersOfOneRestaurantByStatusOldestFirst() {
        Long restaurantId = restaurants.save(new Restaurant(null, "Sabor Paisa", "900123456", "Calle 10",
                "+573001112233", "https://logo.png", 2L)).getId();
        Long otherId = restaurants.save(new Restaurant(null, "Otro", "900999999", "Calle 20",
                "+573001112244", "https://otro.png", 3L)).getId();
        Long dishId = dishes.save(new Dish(null, "Bandeja", 32000, "d", "https://i.png", "Almuerzos",
                restaurantId, true)).getId();
        Long otherDishId = dishes.save(new Dish(null, "Sancocho", 20000, "d", "https://i.png", "Almuerzos",
                otherId, true)).getId();
        Long newer = orders.save(Order.create(1L, restaurantId, List.of(new OrderItem(dishId, 1)),
                LocalDateTime.of(2026, 11, 10, 13, 0))).getId();
        Long older = orders.save(Order.create(2L, restaurantId, List.of(new OrderItem(dishId, 1)),
                LocalDateTime.of(2026, 11, 10, 12, 0))).getId();
        orders.save(Order.create(3L, otherId, List.of(new OrderItem(otherDishId, 1)),
                LocalDateTime.of(2026, 11, 10, 11, 0)));

        var pending = orders.findByRestaurantAndStatus(restaurantId, OrderStatus.PENDIENTE, 0, 10);
        var ready = orders.findByRestaurantAndStatus(restaurantId, OrderStatus.LISTO, 0, 10);

        assertThat(pending.getContent()).extracting(Order::getId).containsExactly(older, newer);
        assertThat(pending.getTotalElements()).isEqualTo(2);
        assertThat(ready.getContent()).isEmpty();
    }

    /** HU-13: al asignarse, la fila del pedido guarda el empleado y el nuevo estado, y conserva sus platos. */
    @Test
    void savesTheAssignedEmployeeAndTheNewStatusKeepingTheDishes() {
        Long restaurantId = restaurants.save(new Restaurant(null, "Sabor Paisa", "900123456", "Calle 10",
                "+573001112233", "https://logo.png", 2L)).getId();
        Long dishId = dishes.save(new Dish(null, "Bandeja", 32000, "d", "https://i.png", "Almuerzos",
                restaurantId, true)).getId();
        Long orderId = orders.save(Order.create(7L, restaurantId, List.of(new OrderItem(dishId, 3)),
                LocalDateTime.of(2026, 11, 10, 12, 0))).getId();
        assertThat(orders.findById(orderId).orElseThrow().getEmployeeId()).isNull();

        Order order = orders.findById(orderId).orElseThrow();
        order.assignTo(9L);
        assertThat(orders.updateIfStatus(order, OrderStatus.PENDIENTE)).isTrue();
        entityManager.flush();
        entityManager.clear();

        Order found = orders.findById(orderId).orElseThrow();
        assertThat(found.getEmployeeId()).isEqualTo(9L);
        assertThat(found.getStatus()).isEqualTo(OrderStatus.EN_PREPARACION);
        assertThat(found.getItems()).singleElement().satisfies(item -> {
            assertThat(item.getDishId()).isEqualTo(dishId);
            assertThat(item.getQuantity()).isEqualTo(3);
        });
        assertThat(orders.findByRestaurantAndStatus(restaurantId, OrderStatus.PENDIENTE, 0, 10).getContent()).isEmpty();
        assertThat(orders.findByRestaurantAndStatus(restaurantId, OrderStatus.EN_PREPARACION, 0, 10).getContent())
                .singleElement().satisfies(listed -> assertThat(listed.getEmployeeId()).isEqualTo(9L));
        assertThat(orders.findById(orderId + 1000)).isEmpty();
    }

    /** HU-14: al quedar listo, la fila guarda el PIN y el estado LISTO, y conserva el empleado asignado. */
    @Test
    void savesTheSecurityPinWhenTheOrderIsReady() {
        Long restaurantId = restaurants.save(new Restaurant(null, "Sabor Paisa", "900123456", "Calle 10",
                "+573001112233", "https://logo.png", 2L)).getId();
        Long dishId = dishes.save(new Dish(null, "Bandeja", 32000, "d", "https://i.png", "Almuerzos",
                restaurantId, true)).getId();
        Long orderId = orders.save(Order.create(7L, restaurantId, List.of(new OrderItem(dishId, 3)),
                LocalDateTime.of(2026, 11, 10, 12, 0))).getId();
        Order order = orders.findById(orderId).orElseThrow();
        assertThat(order.getSecurityPin()).isNull();
        order.assignTo(9L);
        order.markReady("048213");
        assertThat(orders.updateIfStatus(order, OrderStatus.PENDIENTE)).isTrue();
        entityManager.flush();
        entityManager.clear();

        Order found = orders.findById(orderId).orElseThrow();
        assertThat(found.getStatus()).isEqualTo(OrderStatus.LISTO);
        assertThat(found.getSecurityPin()).isEqualTo("048213");
        assertThat(found.getEmployeeId()).isEqualTo(9L);
        assertThat(orders.findByRestaurantAndStatus(restaurantId, OrderStatus.LISTO, 0, 10).getContent())
                .singleElement().satisfies(listed -> assertThat(listed.getId()).isEqualTo(orderId));
    }

    /** HU-15: al entregarse, la fila queda en ENTREGADO y deja de contar como pedido en proceso del cliente. */
    @Test
    void savesTheDeliveredOrderAndFreesTheClientToOrderAgain() {
        Long restaurantId = restaurants.save(new Restaurant(null, "Sabor Paisa", "900123456", "Calle 10",
                "+573001112233", "https://logo.png", 2L)).getId();
        Long dishId = dishes.save(new Dish(null, "Bandeja", 32000, "d", "https://i.png", "Almuerzos",
                restaurantId, true)).getId();
        Long orderId = orders.save(Order.create(7L, restaurantId, List.of(new OrderItem(dishId, 3)),
                LocalDateTime.of(2026, 11, 10, 12, 0))).getId();
        Order order = orders.findById(orderId).orElseThrow();
        order.assignTo(9L);
        order.markReady("048213");
        order.deliver("048213");
        assertThat(orders.updateIfStatus(order, OrderStatus.PENDIENTE)).isTrue();
        entityManager.flush();
        entityManager.clear();

        Order found = orders.findById(orderId).orElseThrow();
        assertThat(found.getStatus()).isEqualTo(OrderStatus.ENTREGADO);
        assertThat(found.getEmployeeId()).isEqualTo(9L);
        assertThat(orders.existsByClientIdAndStatusIn(7L, OrderStatus.IN_PROCESS)).isFalse();
        assertThat(orders.findByRestaurantAndStatus(restaurantId, OrderStatus.LISTO, 0, 10).getContent()).isEmpty();
    }

    /** HU-16: al cancelarse, la fila queda en CANCELADO y el cliente puede volver a pedir. */
    @Test
    void savesTheCancelledOrderAndFreesTheClientToOrderAgain() {
        Long restaurantId = restaurants.save(new Restaurant(null, "Sabor Paisa", "900123456", "Calle 10",
                "+573001112233", "https://logo.png", 2L)).getId();
        Long dishId = dishes.save(new Dish(null, "Bandeja", 32000, "d", "https://i.png", "Almuerzos",
                restaurantId, true)).getId();
        Long orderId = orders.save(Order.create(7L, restaurantId, List.of(new OrderItem(dishId, 3)),
                LocalDateTime.of(2026, 11, 10, 12, 0))).getId();
        assertThat(orders.existsByClientIdAndStatusIn(7L, OrderStatus.IN_PROCESS)).isTrue();

        Order order = orders.findById(orderId).orElseThrow();
        order.cancel();
        assertThat(orders.updateIfStatus(order, OrderStatus.PENDIENTE)).isTrue();
        entityManager.flush();
        entityManager.clear();

        assertThat(orders.findById(orderId).orElseThrow().getStatus()).isEqualTo(OrderStatus.CANCELADO);
        assertThat(orders.existsByClientIdAndStatusIn(7L, OrderStatus.IN_PROCESS)).isFalse();
        assertThat(orders.findByRestaurantAndStatus(restaurantId, OrderStatus.PENDIENTE, 0, 10).getContent()).isEmpty();
    }

    private Long[] restaurantWithDish() {
        Long restaurantId = restaurants.save(new Restaurant(null, "Sabor Paisa", "900123456", "Calle 10",
                "+573001112233", "https://logo.png", 2L)).getId();
        Long dishId = dishes.save(new Dish(null, "Bandeja", 32000, "d", "https://i.png", "Almuerzos",
                restaurantId, true)).getId();
        return new Long[] {restaurantId, dishId};
    }

    /** Concurrencia: dos peticiones leyeron el mismo pedido PENDIENTE; solo la primera en guardar gana. */
    @Test
    void aChangeBasedOnAnOutdatedReadIsRejected() {
        Long[] ids = restaurantWithDish();
        Long orderId = orders.save(Order.create(7L, ids[0], List.of(new OrderItem(ids[1], 1)),
                LocalDateTime.of(2026, 11, 13, 12, 0))).getId();
        Order readByEmployee = orders.findById(orderId).orElseThrow();
        Order readByClient = orders.findById(orderId).orElseThrow();

        readByEmployee.assignTo(9L);
        readByClient.cancel();
        boolean employeeWon = orders.updateIfStatus(readByEmployee, OrderStatus.PENDIENTE);
        boolean clientWon = orders.updateIfStatus(readByClient, OrderStatus.PENDIENTE);
        entityManager.clear();

        assertThat(employeeWon).isTrue();
        assertThat(clientWon).isFalse();
        Order found = orders.findById(orderId).orElseThrow();
        assertThat(found.getStatus()).isEqualTo(OrderStatus.EN_PREPARACION);
        assertThat(found.getEmployeeId()).isEqualTo(9L);
    }

    /** Concurrencia: aunque las dos peticiones pasen la revisión previa, MySQL no deja guardar el segundo pedido. */
    @Test
    void theDatabaseRejectsASecondOrderInProcessForTheSameClient() {
        Long[] ids = restaurantWithDish();
        orders.save(Order.create(7L, ids[0], List.of(new OrderItem(ids[1], 1)), LocalDateTime.of(2026, 11, 13, 12, 0)));

        assertThatThrownBy(() -> orders.save(Order.create(7L, ids[0], List.of(new OrderItem(ids[1], 2)),
                LocalDateTime.of(2026, 11, 13, 12, 0)))).isInstanceOf(ActiveOrderExistsException.class);
    }

    /** Al terminar (entregado o cancelado) el pedido deja de ocupar el lugar, y el cliente puede volver a pedir. */
    @Test
    void aFinishedOrderFreesTheClientForANewOne() {
        Long[] ids = restaurantWithDish();
        Long firstId = orders.save(Order.create(7L, ids[0], List.of(new OrderItem(ids[1], 1)),
                LocalDateTime.of(2026, 11, 13, 12, 0))).getId();
        Order first = orders.findById(firstId).orElseThrow();
        first.cancel();
        assertThat(orders.updateIfStatus(first, OrderStatus.PENDIENTE)).isTrue();

        Order second = orders.save(Order.create(7L, ids[0], List.of(new OrderItem(ids[1], 1)),
                LocalDateTime.of(2026, 11, 13, 12, 5)));
        Order third = orders.save(Order.create(8L, ids[0], List.of(new OrderItem(ids[1], 1)),
                LocalDateTime.of(2026, 11, 13, 12, 6)));

        assertThat(second.getId()).isNotNull();
        assertThat(third.getId()).isNotNull();
    }

    /** HU-14: si el SMS falla, el pedido vuelve de LISTO a EN_PREPARACION y pierde el PIN. */
    @Test
    void anOrderCanGoBackFromReadyToInPreparation() {
        Long[] ids = restaurantWithDish();
        Long orderId = orders.save(Order.create(7L, ids[0], List.of(new OrderItem(ids[1], 1)),
                LocalDateTime.of(2026, 11, 13, 12, 0))).getId();
        Order order = orders.findById(orderId).orElseThrow();
        order.assignTo(9L);
        order.markReady("048213");
        assertThat(orders.updateIfStatus(order, OrderStatus.PENDIENTE)).isTrue();
        order.undoReady();
        assertThat(orders.updateIfStatus(order, OrderStatus.LISTO)).isTrue();
        entityManager.clear();

        Order found = orders.findById(orderId).orElseThrow();
        assertThat(found.getStatus()).isEqualTo(OrderStatus.EN_PREPARACION);
        assertThat(found.getSecurityPin()).isNull();
        assertThat(found.getEmployeeId()).isEqualTo(9L);
    }
}
