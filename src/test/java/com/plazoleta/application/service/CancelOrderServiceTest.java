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
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.plazoleta.application.dto.OrderResponse;
import com.plazoleta.application.port.output.OrderRepositoryPort;
import com.plazoleta.application.port.output.OrderTraceabilityPort;
import com.plazoleta.domain.exception.ForbiddenOperationException;
import com.plazoleta.domain.exception.InvalidOrderStatusException;
import com.plazoleta.domain.exception.OrderNotFoundException;
import com.plazoleta.domain.model.Order;
import com.plazoleta.domain.model.OrderItem;
import com.plazoleta.domain.model.OrderStatus;

/** HU-16: Cancelar pedido. */
@ExtendWith(MockitoExtension.class)
class CancelOrderServiceTest {
    private static final Long ORDER_ID = 50L;
    private static final Long CLIENT_ID = 7L;

    @Mock
    private OrderRepositoryPort orderRepositoryPort;
    @Mock
    private OrderTraceabilityPort orderTraceabilityPort;
    @InjectMocks
    private CancelOrderService service;

    private static Order order(OrderStatus status) {
        return new Order(ORDER_ID, CLIENT_ID, 1L, status, LocalDateTime.of(2026, 11, 10, 12, 0),
                List.of(new OrderItem(10L, 2)));
    }

    @Test
    void cancelsThePendingOrderOfTheClient() {
        when(orderRepositoryPort.findById(ORDER_ID)).thenReturn(Optional.of(order(OrderStatus.PENDIENTE)));
        when(orderRepositoryPort.updateIfStatus(any(), any())).thenReturn(true);

        OrderResponse response = service.execute(ORDER_ID, CLIENT_ID);

        ArgumentCaptor<Order> saved = ArgumentCaptor.forClass(Order.class);
        verify(orderRepositoryPort).updateIfStatus(saved.capture(), any());
        assertThat(saved.getValue().getStatus()).isEqualTo(OrderStatus.CANCELADO);
        assertThat(response.getStatus()).isEqualTo("CANCELADO");
        assertThat(response.getId()).isEqualTo(ORDER_ID);
    }

    @ParameterizedTest
    @EnumSource(value = OrderStatus.class, names = "PENDIENTE", mode = EnumSource.Mode.EXCLUDE)
    void anOrderThatIsNotPendingCannotBeCancelledAndTheClientGetsTheRequiredMessage(OrderStatus status) {
        Order order = order(status);
        when(orderRepositoryPort.findById(ORDER_ID)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> service.execute(ORDER_ID, CLIENT_ID))
                .isInstanceOf(InvalidOrderStatusException.class)
                .hasMessage("Lo sentimos, tu pedido ya está en preparación y no puede cancelarse");
        assertThat(order.getStatus()).isEqualTo(status);
        verify(orderRepositoryPort, never()).updateIfStatus(any(), any());
    }

    @Test
    void aClientCannotCancelTheOrderOfAnotherClient() {
        when(orderRepositoryPort.findById(ORDER_ID)).thenReturn(Optional.of(order(OrderStatus.PENDIENTE)));

        assertThatThrownBy(() -> service.execute(ORDER_ID, 8L)).isInstanceOf(ForbiddenOperationException.class);
        verify(orderRepositoryPort, never()).updateIfStatus(any(), any());
    }

    @Test
    void rejectsAnUnknownOrder() {
        when(orderRepositoryPort.findById(ORDER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.execute(ORDER_ID, CLIENT_ID)).isInstanceOf(OrderNotFoundException.class);
    }

    @Test
    void aCancelledOrderCannotBeTakenByAnEmployee() {
        Order cancelled = order(OrderStatus.PENDIENTE);
        cancelled.cancel();

        assertThatThrownBy(() -> cancelled.assignTo(9L)).isInstanceOf(InvalidOrderStatusException.class);
        assertThat(cancelled.getStatus()).isEqualTo(OrderStatus.CANCELADO);
    }

    /** HU-17: el cambio PENDIENTE -> CANCELADO queda registrado. */
    @Test
    void recordsTheChangeFromPendingToCancelled() {
        when(orderRepositoryPort.findById(ORDER_ID)).thenReturn(Optional.of(order(OrderStatus.PENDIENTE)));
        when(orderRepositoryPort.updateIfStatus(any(), any())).thenReturn(true);

        service.execute(ORDER_ID, CLIENT_ID);

        ArgumentCaptor<Order> recorded = ArgumentCaptor.forClass(Order.class);
        verify(orderTraceabilityPort).record(recorded.capture(), org.mockito.ArgumentMatchers.eq(OrderStatus.PENDIENTE));
        assertThat(recorded.getValue().getStatus()).isEqualTo(OrderStatus.CANCELADO);
    }

    @Test
    void aRejectedCancellationLeavesNoTraceabilityLog() {
        when(orderRepositoryPort.findById(ORDER_ID)).thenReturn(Optional.of(order(OrderStatus.LISTO)));

        assertThatThrownBy(() -> service.execute(ORDER_ID, CLIENT_ID)).isInstanceOf(InvalidOrderStatusException.class);
        verifyNoInteractions(orderTraceabilityPort);
    }

    /** Concurrencia: si un empleado lo tomó en el mismo instante, el cliente recibe el mensaje del Excel. */
    @Test
    void whenAnEmployeeTookTheOrderAtTheSameTimeTheClientGetsTheRequiredMessage() {
        when(orderRepositoryPort.findById(ORDER_ID)).thenReturn(Optional.of(order(OrderStatus.PENDIENTE)));
        when(orderRepositoryPort.updateIfStatus(any(), any())).thenReturn(false);

        assertThatThrownBy(() -> service.execute(ORDER_ID, CLIENT_ID))
                .isInstanceOf(InvalidOrderStatusException.class)
                .hasMessage("Lo sentimos, tu pedido ya está en preparación y no puede cancelarse");
        verifyNoInteractions(orderTraceabilityPort);
    }
}
