package com.plazoleta.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.plazoleta.application.dto.DeliverOrderCommand;
import com.plazoleta.application.dto.OrderResponse;
import com.plazoleta.application.port.output.OrderRepositoryPort;
import com.plazoleta.application.port.output.OrderTraceabilityPort;
import com.plazoleta.domain.exception.ForbiddenOperationException;
import com.plazoleta.domain.exception.InvalidOrderStatusException;
import com.plazoleta.domain.exception.InvalidSecurityPinException;
import com.plazoleta.domain.exception.OrderNotFoundException;
import com.plazoleta.domain.model.Order;
import com.plazoleta.domain.model.OrderItem;
import com.plazoleta.domain.model.OrderStatus;

/** HU-15: Entregar pedido (marcarlo como entregado). */
@ExtendWith(MockitoExtension.class)
class DeliverOrderServiceTest {
    private static final Long ORDER_ID = 50L;
    private static final Long EMPLOYEE_ID = 9L;
    private static final Long RESTAURANT_ID = 1L;
    private static final String PIN = "048213";

    @Mock
    private OrderRepositoryPort orderRepositoryPort;
    @Mock
    private OrderTraceabilityPort orderTraceabilityPort;
    @InjectMocks
    private DeliverOrderService service;

    private static Order order(OrderStatus status) {
        return new Order(ORDER_ID, 7L, RESTAURANT_ID, status, LocalDateTime.of(2026, 11, 10, 12, 0),
                List.of(new OrderItem(10L, 2)), EMPLOYEE_ID, PIN);
    }

    private void orderIs(OrderStatus status) {
        when(orderRepositoryPort.findById(ORDER_ID)).thenReturn(Optional.of(order(status)));
    }

    @Test
    void deliversTheReadyOrderWhenThePinIsCorrect() {
        orderIs(OrderStatus.LISTO);
        when(orderRepositoryPort.save(any())).thenAnswer(call -> call.getArgument(0));

        OrderResponse response = service.execute(ORDER_ID, new DeliverOrderCommand(PIN), EMPLOYEE_ID, RESTAURANT_ID);

        ArgumentCaptor<Order> saved = ArgumentCaptor.forClass(Order.class);
        verify(orderRepositoryPort).save(saved.capture());
        assertThat(saved.getValue().getStatus()).isEqualTo(OrderStatus.ENTREGADO);
        assertThat(response.getStatus()).isEqualTo("ENTREGADO");
        assertThat(response.getEmployeeId()).isEqualTo(EMPLOYEE_ID);
    }

    @Test
    void acceptsThePinWithSpacesAround() {
        orderIs(OrderStatus.LISTO);
        when(orderRepositoryPort.save(any())).thenAnswer(call -> call.getArgument(0));

        OrderResponse response = service.execute(ORDER_ID, new DeliverOrderCommand(" " + PIN + " "), EMPLOYEE_ID,
                RESTAURANT_ID);

        assertThat(response.getStatus()).isEqualTo("ENTREGADO");
    }

    @ParameterizedTest
    @ValueSource(strings = {"000000", "48213", "0482130"})
    void rejectsAWrongPinAndKeepsTheOrderReady(String wrongPin) {
        Order ready = order(OrderStatus.LISTO);
        when(orderRepositoryPort.findById(ORDER_ID)).thenReturn(Optional.of(ready));

        assertThatThrownBy(() -> service.execute(ORDER_ID, new DeliverOrderCommand(wrongPin), EMPLOYEE_ID,
                RESTAURANT_ID)).isInstanceOf(InvalidSecurityPinException.class);
        assertThat(ready.getStatus()).isEqualTo(OrderStatus.LISTO);
        verify(orderRepositoryPort, never()).save(any());
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "   "})
    void requiresThePin(String pin) {
        orderIs(OrderStatus.LISTO);

        assertThatThrownBy(() -> service.execute(ORDER_ID, new DeliverOrderCommand(pin), EMPLOYEE_ID, RESTAURANT_ID))
                .isInstanceOf(IllegalArgumentException.class);
        verify(orderRepositoryPort, never()).save(any());
    }

    @Test
    void requiresTheBody() {
        assertThatThrownBy(() -> service.execute(ORDER_ID, null, EMPLOYEE_ID, RESTAURANT_ID))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @EnumSource(value = OrderStatus.class, names = "LISTO", mode = EnumSource.Mode.EXCLUDE)
    void onlyReadyOrdersCanBeDeliveredEvenWithTheRightPin(OrderStatus status) {
        orderIs(status);

        assertThatThrownBy(() -> service.execute(ORDER_ID, new DeliverOrderCommand(PIN), EMPLOYEE_ID, RESTAURANT_ID))
                .isInstanceOf(InvalidOrderStatusException.class);
        verify(orderRepositoryPort, never()).save(any());
    }

    /** Criterio 2: un pedido entregado no vuelve a cambiar de estado por ninguna operación. */
    @Test
    void aDeliveredOrderCannotChangeItsStatusAnymore() {
        Order delivered = order(OrderStatus.ENTREGADO);

        assertThatThrownBy(() -> delivered.assignTo(EMPLOYEE_ID)).isInstanceOf(InvalidOrderStatusException.class);
        assertThatThrownBy(() -> delivered.markReady("111111")).isInstanceOf(InvalidOrderStatusException.class);
        assertThatThrownBy(() -> delivered.deliver(PIN)).isInstanceOf(InvalidOrderStatusException.class);
        assertThat(delivered.getStatus()).isEqualTo(OrderStatus.ENTREGADO);
        assertThat(delivered.getSecurityPin()).isEqualTo(PIN);
    }

    @Test
    void onlyTheAssignedEmployeeCanDeliverTheOrder() {
        orderIs(OrderStatus.LISTO);

        assertThatThrownBy(() -> service.execute(ORDER_ID, new DeliverOrderCommand(PIN), 10L, RESTAURANT_ID))
                .isInstanceOf(ForbiddenOperationException.class).hasMessageContaining("another employee");
        verify(orderRepositoryPort, never()).save(any());
    }

    @Test
    void rejectsAnOrderOfAnotherRestaurant() {
        orderIs(OrderStatus.LISTO);

        assertThatThrownBy(() -> service.execute(ORDER_ID, new DeliverOrderCommand(PIN), EMPLOYEE_ID, 2L))
                .isInstanceOf(ForbiddenOperationException.class);
    }

    @Test
    void rejectsAnUnknownOrder() {
        when(orderRepositoryPort.findById(ORDER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.execute(ORDER_ID, new DeliverOrderCommand(PIN), EMPLOYEE_ID, RESTAURANT_ID))
                .isInstanceOf(OrderNotFoundException.class);
    }

    /** HU-17: el cambio LISTO -> ENTREGADO queda registrado. */
    @Test
    void recordsTheChangeFromReadyToDelivered() {
        orderIs(OrderStatus.LISTO);
        when(orderRepositoryPort.save(any())).thenAnswer(call -> call.getArgument(0));

        service.execute(ORDER_ID, new DeliverOrderCommand(PIN), EMPLOYEE_ID, RESTAURANT_ID);

        ArgumentCaptor<Order> recorded = ArgumentCaptor.forClass(Order.class);
        verify(orderTraceabilityPort).record(recorded.capture(), org.mockito.ArgumentMatchers.eq(OrderStatus.LISTO));
        assertThat(recorded.getValue().getStatus()).isEqualTo(OrderStatus.ENTREGADO);
    }

    @Test
    void aWrongPinLeavesNoTraceabilityLog() {
        orderIs(OrderStatus.LISTO);

        assertThatThrownBy(() -> service.execute(ORDER_ID, new DeliverOrderCommand("000000"), EMPLOYEE_ID,
                RESTAURANT_ID)).isInstanceOf(InvalidSecurityPinException.class);
        verifyNoInteractions(orderTraceabilityPort);
    }
}
