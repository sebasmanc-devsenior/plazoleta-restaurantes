package com.plazoleta.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
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
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.plazoleta.application.dto.OrderResponse;
import com.plazoleta.application.port.output.OrderRepositoryPort;
import com.plazoleta.application.port.output.PinGeneratorPort;
import com.plazoleta.application.port.output.SmsNotificationPort;
import com.plazoleta.application.port.output.UserRepositoryPort;
import com.plazoleta.domain.exception.ForbiddenOperationException;
import com.plazoleta.domain.exception.IntegrationUnavailableException;
import com.plazoleta.domain.exception.InvalidOrderStatusException;
import com.plazoleta.domain.exception.OrderNotFoundException;
import com.plazoleta.domain.exception.UserNotFoundException;
import com.plazoleta.domain.model.Order;
import com.plazoleta.domain.model.OrderItem;
import com.plazoleta.domain.model.OrderStatus;
import com.plazoleta.domain.model.Role;
import com.plazoleta.domain.model.User;

/** HU-14: Notificar que el pedido está listo. */
@ExtendWith(MockitoExtension.class)
class MarkOrderReadyServiceTest {
    private static final Long ORDER_ID = 50L;
    private static final Long CLIENT_ID = 7L;
    private static final Long RESTAURANT_ID = 1L;
    private static final Long EMPLOYEE_ID = 9L;
    private static final String PHONE = "+573001112233";

    @Mock
    private OrderRepositoryPort orderRepositoryPort;
    @Mock
    private UserRepositoryPort userRepositoryPort;
    @Mock
    private PinGeneratorPort pinGeneratorPort;
    @Mock
    private SmsNotificationPort smsNotificationPort;
    @InjectMocks
    private MarkOrderReadyService service;

    private static Order order(OrderStatus status) {
        return new Order(ORDER_ID, CLIENT_ID, RESTAURANT_ID, status, LocalDateTime.of(2026, 11, 10, 12, 0),
                List.of(new OrderItem(10L, 2)), 9L);
    }

    private void orderInPreparationWithPin(String pin) {
        when(orderRepositoryPort.findById(ORDER_ID)).thenReturn(Optional.of(order(OrderStatus.EN_PREPARACION)));
        when(pinGeneratorPort.generate()).thenReturn(pin);
    }

    private void clientHasPhone(String phone) {
        when(userRepositoryPort.findRegisteredById(CLIENT_ID))
                .thenReturn(Optional.of(new User(CLIENT_ID, null, phone, Role.CLIENTE, null)));
    }

    @Test
    void marksTheOrderAsReadyAndSendsTheClientAnSmsWithThePin() {
        orderInPreparationWithPin("048213");
        clientHasPhone(PHONE);
        when(orderRepositoryPort.save(any())).thenAnswer(call -> call.getArgument(0));

        OrderResponse response = service.execute(ORDER_ID, EMPLOYEE_ID, RESTAURANT_ID);

        ArgumentCaptor<String> message = ArgumentCaptor.forClass(String.class);
        verify(smsNotificationPort).send(org.mockito.ArgumentMatchers.eq(PHONE), message.capture());
        assertThat(message.getValue()).contains("048213").contains("#" + ORDER_ID).contains("listo");
        ArgumentCaptor<Order> saved = ArgumentCaptor.forClass(Order.class);
        verify(orderRepositoryPort).save(saved.capture());
        assertThat(saved.getValue().getStatus()).isEqualTo(OrderStatus.LISTO);
        assertThat(saved.getValue().getSecurityPin()).isEqualTo("048213");
        assertThat(saved.getValue().getEmployeeId()).isEqualTo(9L);
        assertThat(response.getStatus()).isEqualTo("LISTO");
    }

    @Test
    void sendsTheSmsBeforeSavingTheOrder() {
        orderInPreparationWithPin("048213");
        clientHasPhone(PHONE);
        when(orderRepositoryPort.save(any())).thenAnswer(call -> call.getArgument(0));

        service.execute(ORDER_ID, EMPLOYEE_ID, RESTAURANT_ID);

        InOrder order = inOrder(smsNotificationPort, orderRepositoryPort);
        order.verify(smsNotificationPort).send(anyString(), anyString());
        order.verify(orderRepositoryPort).save(any());
    }

    @Test
    void theOrderIsNotSavedAsReadyWhenTheSmsCannotBeSent() {
        orderInPreparationWithPin("048213");
        clientHasPhone(PHONE);
        doThrow(new IntegrationUnavailableException("Messaging service is unavailable", null))
                .when(smsNotificationPort).send(anyString(), anyString());

        assertThatThrownBy(() -> service.execute(ORDER_ID, EMPLOYEE_ID, RESTAURANT_ID))
                .isInstanceOf(IntegrationUnavailableException.class);
        verify(orderRepositoryPort, never()).save(any());
    }

    @ParameterizedTest
    @EnumSource(value = OrderStatus.class, names = "EN_PREPARACION", mode = EnumSource.Mode.EXCLUDE)
    void onlyOrdersInPreparationCanBeMarkedAsReady(OrderStatus status) {
        when(orderRepositoryPort.findById(ORDER_ID)).thenReturn(Optional.of(order(status)));
        when(pinGeneratorPort.generate()).thenReturn("048213");

        assertThatThrownBy(() -> service.execute(ORDER_ID, EMPLOYEE_ID, RESTAURANT_ID))
                .isInstanceOf(InvalidOrderStatusException.class);
        verify(smsNotificationPort, never()).send(anyString(), anyString());
        verify(orderRepositoryPort, never()).save(any());
    }

    @Test
    void rejectsAnOrderOfAnotherRestaurant() {
        when(orderRepositoryPort.findById(ORDER_ID)).thenReturn(Optional.of(order(OrderStatus.EN_PREPARACION)));

        assertThatThrownBy(() -> service.execute(ORDER_ID, EMPLOYEE_ID, 2L)).isInstanceOf(ForbiddenOperationException.class);
        verify(smsNotificationPort, never()).send(anyString(), anyString());
    }

    @Test
    void onlyTheAssignedEmployeeCanMarkTheOrderAsReady() {
        when(orderRepositoryPort.findById(ORDER_ID)).thenReturn(Optional.of(order(OrderStatus.EN_PREPARACION)));

        assertThatThrownBy(() -> service.execute(ORDER_ID, 10L, RESTAURANT_ID))
                .isInstanceOf(ForbiddenOperationException.class).hasMessageContaining("another employee");
        verify(smsNotificationPort, never()).send(anyString(), anyString());
        verify(orderRepositoryPort, never()).save(any());
    }

    @Test
    void rejectsAnUnknownOrder() {
        when(orderRepositoryPort.findById(ORDER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.execute(ORDER_ID, EMPLOYEE_ID, RESTAURANT_ID)).isInstanceOf(OrderNotFoundException.class);
    }

    @Test
    void failsWhenTheClientAccountNoLongerExists() {
        orderInPreparationWithPin("048213");
        when(userRepositoryPort.findRegisteredById(CLIENT_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.execute(ORDER_ID, EMPLOYEE_ID, RESTAURANT_ID)).isInstanceOf(UserNotFoundException.class);
        verify(orderRepositoryPort, never()).save(any());
    }

    @Test
    void failsWhenTheClientHasNoPhone() {
        orderInPreparationWithPin("048213");
        clientHasPhone(" ");

        assertThatThrownBy(() -> service.execute(ORDER_ID, EMPLOYEE_ID, RESTAURANT_ID)).isInstanceOf(IllegalArgumentException.class);
        verify(smsNotificationPort, never()).send(anyString(), anyString());
    }

    @Test
    void theResponseNeverShowsThePin() {
        orderInPreparationWithPin("048213");
        clientHasPhone(PHONE);
        when(orderRepositoryPort.save(any())).thenAnswer(call -> call.getArgument(0));

        OrderResponse response = service.execute(ORDER_ID, EMPLOYEE_ID, RESTAURANT_ID);

        assertThat(OrderResponse.class.getDeclaredFields()).extracting(java.lang.reflect.Field::getName)
                .noneMatch(name -> name.toLowerCase().contains("pin"));
        assertThat(response.getEmployeeId()).isEqualTo(9L);
    }
}
