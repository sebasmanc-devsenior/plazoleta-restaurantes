package com.plazoleta.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.plazoleta.application.dto.CreateOrderCommand;
import com.plazoleta.application.dto.OrderItemCommand;
import com.plazoleta.application.dto.OrderResponse;
import com.plazoleta.application.port.output.DishRepositoryPort;
import com.plazoleta.application.port.output.OrderRepositoryPort;
import com.plazoleta.application.port.output.RestaurantRepositoryPort;
import com.plazoleta.application.port.output.UserRepositoryPort;
import com.plazoleta.domain.exception.ActiveOrderExistsException;
import com.plazoleta.domain.exception.DishNotFoundException;
import com.plazoleta.domain.exception.ForbiddenOperationException;
import com.plazoleta.domain.exception.RestaurantNotFoundException;
import com.plazoleta.domain.exception.UserNotFoundException;
import com.plazoleta.domain.model.Dish;
import com.plazoleta.domain.model.Order;
import com.plazoleta.domain.model.OrderStatus;
import com.plazoleta.domain.model.Restaurant;
import com.plazoleta.domain.model.Role;
import com.plazoleta.domain.model.User;

/** HU-11: Realizar pedido. */
@ExtendWith(MockitoExtension.class)
class CreateOrderServiceTest {
    private static final Long CLIENT_ID = 7L;
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-11-10T17:00:00Z"), ZoneId.of("America/Bogota"));

    @Mock
    private OrderRepositoryPort orderRepositoryPort;
    @Mock
    private DishRepositoryPort dishRepositoryPort;
    @Mock
    private RestaurantRepositoryPort restaurantRepositoryPort;
    @Mock
    private UserRepositoryPort userRepositoryPort;

    private CreateOrderService service;

    @BeforeEach
    void setUp() {
        service = new CreateOrderService(orderRepositoryPort, dishRepositoryPort, restaurantRepositoryPort,
                userRepositoryPort, CLOCK);
    }

    private static Dish dish(long id, long restaurantId, boolean active) {
        return new Dish(id, "Plato " + id, 15000, "d", "https://i.png", "Almuerzos", restaurantId, active);
    }

    /** Respuesta del microservicio de usuarios para el cliente que hace el pedido. */
    private void clientIsRegisteredAs(Role role) {
        when(userRepositoryPort.findRegisteredById(CLIENT_ID))
                .thenReturn(Optional.of(new User(CLIENT_ID, null, "+573001112233", role, null)));
    }

    private void restaurantExists(long id) {
        clientIsRegisteredAs(Role.CLIENTE);
        when(restaurantRepositoryPort.findById(id)).thenReturn(Optional.of(
                new Restaurant(id, "Sabor Paisa", "900123456", "Calle 1", "+573001234567", "https://l.png", 2L)));
    }

    private static CreateOrderCommand order(OrderItemCommand... items) {
        return new CreateOrderCommand(1L, Arrays.asList(items));
    }

    @Test
    void createsPendingOrderWithTheDishesAndQuantities() {
        restaurantExists(1L);
        when(orderRepositoryPort.existsByClientIdAndStatusIn(CLIENT_ID, OrderStatus.IN_PROCESS)).thenReturn(false);
        when(dishRepositoryPort.findById(10L)).thenReturn(Optional.of(dish(10, 1, true)));
        when(dishRepositoryPort.findById(11L)).thenReturn(Optional.of(dish(11, 1, true)));
        when(orderRepositoryPort.save(any())).thenAnswer(call -> {
            Order o = call.getArgument(0);
            return new Order(50L, o.getClientId(), o.getRestaurantId(), o.getStatus(), o.getCreatedAt(), o.getItems());
        });

        OrderResponse response = service.execute(order(new OrderItemCommand(10L, 2), new OrderItemCommand(11L, 1)),
                CLIENT_ID);

        ArgumentCaptor<Order> saved = ArgumentCaptor.forClass(Order.class);
        verify(orderRepositoryPort).save(saved.capture());
        assertThat(saved.getValue().getStatus()).isEqualTo(OrderStatus.PENDIENTE);
        assertThat(saved.getValue().getClientId()).isEqualTo(CLIENT_ID);
        assertThat(saved.getValue().getCreatedAt()).isEqualTo(LocalDateTime.of(2026, 11, 10, 12, 0));
        assertThat(response.getId()).isEqualTo(50L);
        assertThat(response.getStatus()).isEqualTo("PENDIENTE");
        assertThat(response.getDishes()).extracting(OrderItemCommand::getQuantity).containsExactly(2, 1);
    }

    @Test
    void rejectsSecondOrderWhileTheClientHasOneInProcess() {
        restaurantExists(1L);
        when(orderRepositoryPort.existsByClientIdAndStatusIn(CLIENT_ID, OrderStatus.IN_PROCESS)).thenReturn(true);

        assertThatThrownBy(() -> service.execute(order(new OrderItemCommand(10L, 1)), CLIENT_ID))
                .isInstanceOf(ActiveOrderExistsException.class);
        verify(orderRepositoryPort, never()).save(any());
    }

    @Test
    void inProcessMeansPendingInPreparationOrReady() {
        assertThat(OrderStatus.IN_PROCESS).containsExactlyInAnyOrder(
                OrderStatus.PENDIENTE, OrderStatus.EN_PREPARACION, OrderStatus.LISTO);
    }

    @Test
    void rejectsDishFromAnotherRestaurant() {
        restaurantExists(1L);
        when(dishRepositoryPort.findById(10L)).thenReturn(Optional.of(dish(10, 2, true)));

        assertThatThrownBy(() -> service.execute(order(new OrderItemCommand(10L, 1)), CLIENT_ID))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("selected restaurant")
                .isNotInstanceOf(DishNotFoundException.class);
        verify(orderRepositoryPort, never()).save(any());
    }

    @Test
    void rejectsDisabledDish() {
        restaurantExists(1L);
        when(dishRepositoryPort.findById(10L)).thenReturn(Optional.of(dish(10, 1, false)));

        assertThatThrownBy(() -> service.execute(order(new OrderItemCommand(10L, 1)), CLIENT_ID))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("not available");
    }

    @Test
    void rejectsUnknownDish() {
        restaurantExists(1L);
        when(dishRepositoryPort.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.execute(order(new OrderItemCommand(10L, 1)), CLIENT_ID))
                .isInstanceOf(DishNotFoundException.class);
    }

    @Test
    void rejectsUnknownRestaurant() {
        clientIsRegisteredAs(Role.CLIENTE);
        when(restaurantRepositoryPort.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.execute(order(new OrderItemCommand(10L, 1)), CLIENT_ID))
                .isInstanceOf(RestaurantNotFoundException.class);
    }

    @Test
    void asksTheUsersServiceForTheClientAccount() {
        when(userRepositoryPort.findRegisteredById(CLIENT_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.execute(order(new OrderItemCommand(10L, 1)), CLIENT_ID))
                .isInstanceOf(UserNotFoundException.class);
        verify(userRepositoryPort).findRegisteredById(CLIENT_ID);
        verify(orderRepositoryPort, never()).save(any());
    }

    @Test
    void rejectsAccountThatIsNotAClient() {
        clientIsRegisteredAs(Role.EMPLEADO);

        assertThatThrownBy(() -> service.execute(order(new OrderItemCommand(10L, 1)), CLIENT_ID))
                .isInstanceOf(ForbiddenOperationException.class);
        verify(orderRepositoryPort, never()).save(any());
    }

    @Test
    void requiresRestaurant() {
        assertThatThrownBy(() -> service.execute(new CreateOrderCommand(null, List.of(new OrderItemCommand(10L, 1))),
                CLIENT_ID)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void requiresAtLeastOneDish() {
        assertThatThrownBy(() -> service.execute(new CreateOrderCommand(1L, List.of()), CLIENT_ID))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(ints = {0, -2})
    void requiresPositiveQuantity(Integer quantity) {
        assertThatThrownBy(() -> service.execute(order(new OrderItemCommand(10L, quantity)), CLIENT_ID))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Quantity");
    }

    @Test
    void rejectsTheSameDishTwice() {
        assertThatThrownBy(() -> service.execute(order(new OrderItemCommand(10L, 1), new OrderItemCommand(10L, 2)),
                CLIENT_ID)).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("only once");
    }
}
