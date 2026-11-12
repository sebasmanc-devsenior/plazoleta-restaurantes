package com.plazoleta.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.plazoleta.application.dto.OrderResponse;
import com.plazoleta.application.port.output.OrderRepositoryPort;
import com.plazoleta.domain.exception.ForbiddenOperationException;
import com.plazoleta.domain.exception.InvalidOrderStatusException;
import com.plazoleta.domain.exception.OrderNotFoundException;
import com.plazoleta.domain.model.Order;
import com.plazoleta.domain.model.OrderItem;
import com.plazoleta.domain.model.OrderStatus;

/** HU-13: Asignarse a un pedido y cambiar su estado a "en preparación". */
@ExtendWith(MockitoExtension.class)
class AssignOrderServiceTest {
    private static final Long ORDER_ID = 50L;
    private static final Long EMPLOYEE_ID = 9L;
    private static final Long RESTAURANT_ID = 1L;

    @Mock
    private OrderRepositoryPort orderRepositoryPort;
    @InjectMocks
    private AssignOrderService service;

    private static Order order(OrderStatus status, Long employeeId) {
        return new Order(ORDER_ID, 7L, RESTAURANT_ID, status, LocalDateTime.of(2026, 11, 10, 12, 0),
                List.of(new OrderItem(10L, 2)), employeeId);
    }

    @Test
    void assignsThePendingOrderToTheEmployeeAndMovesItToInPreparation() {
        when(orderRepositoryPort.findById(ORDER_ID)).thenReturn(Optional.of(order(OrderStatus.PENDIENTE, null)));
        when(orderRepositoryPort.save(any())).thenAnswer(call -> call.getArgument(0));

        OrderResponse response = service.execute(ORDER_ID, EMPLOYEE_ID, RESTAURANT_ID);

        ArgumentCaptor<Order> saved = ArgumentCaptor.forClass(Order.class);
        verify(orderRepositoryPort).save(saved.capture());
        assertThat(saved.getValue().getEmployeeId()).isEqualTo(EMPLOYEE_ID);
        assertThat(saved.getValue().getStatus()).isEqualTo(OrderStatus.EN_PREPARACION);
        assertThat(response.getId()).isEqualTo(ORDER_ID);
        assertThat(response.getEmployeeId()).isEqualTo(EMPLOYEE_ID);
        assertThat(response.getStatus()).isEqualTo("EN_PREPARACION");
        assertThat(response.getClientId()).isEqualTo(7L);
        assertThat(response.getDishes()).singleElement().satisfies(d -> assertThat(d.getQuantity()).isEqualTo(2));
    }

    @ParameterizedTest
    @EnumSource(value = OrderStatus.class, names = "PENDIENTE", mode = EnumSource.Mode.EXCLUDE)
    void onlyPendingOrdersCanBeAssigned(OrderStatus status) {
        when(orderRepositoryPort.findById(ORDER_ID)).thenReturn(Optional.of(order(status, 3L)));

        assertThatThrownBy(() -> service.execute(ORDER_ID, EMPLOYEE_ID, RESTAURANT_ID))
                .isInstanceOf(InvalidOrderStatusException.class);
        verify(orderRepositoryPort, never()).save(any());
    }

    @Test
    void anOrderAlreadyTakenKeepsItsFirstEmployee() {
        Order taken = order(OrderStatus.EN_PREPARACION, 3L);
        when(orderRepositoryPort.findById(ORDER_ID)).thenReturn(Optional.of(taken));

        assertThatThrownBy(() -> service.execute(ORDER_ID, EMPLOYEE_ID, RESTAURANT_ID))
                .isInstanceOf(InvalidOrderStatusException.class);
        assertThat(taken.getEmployeeId()).isEqualTo(3L);
    }

    @Test
    void rejectsAnOrderOfAnotherRestaurant() {
        when(orderRepositoryPort.findById(ORDER_ID)).thenReturn(Optional.of(order(OrderStatus.PENDIENTE, null)));

        assertThatThrownBy(() -> service.execute(ORDER_ID, EMPLOYEE_ID, 2L))
                .isInstanceOf(ForbiddenOperationException.class);
        verify(orderRepositoryPort, never()).save(any());
    }

    @Test
    void rejectsAnUnknownOrder() {
        when(orderRepositoryPort.findById(ORDER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.execute(ORDER_ID, EMPLOYEE_ID, RESTAURANT_ID))
                .isInstanceOf(OrderNotFoundException.class);
    }

    @Test
    void anEmployeeWithoutRestaurantIsForbidden() {
        assertThatThrownBy(() -> service.execute(ORDER_ID, EMPLOYEE_ID, null))
                .isInstanceOf(ForbiddenOperationException.class);
        verify(orderRepositoryPort, never()).findById(anyLong());
    }
}
