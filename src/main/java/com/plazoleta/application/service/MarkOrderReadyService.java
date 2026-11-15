package com.plazoleta.application.service;

import com.plazoleta.domain.util.MensajesError;

import com.plazoleta.application.dto.OrderResponse;
import com.plazoleta.application.port.input.MarkOrderReadyUseCase;
import com.plazoleta.application.port.output.OrderRepositoryPort;
import com.plazoleta.application.port.output.OrderTraceabilityPort;
import com.plazoleta.application.port.output.PinGeneratorPort;
import com.plazoleta.application.port.output.SmsNotificationPort;
import com.plazoleta.application.port.output.UserRepositoryPort;
import com.plazoleta.domain.exception.InvalidOrderStatusException;
import com.plazoleta.domain.exception.UserNotFoundException;
import com.plazoleta.domain.model.Order;
import com.plazoleta.domain.model.OrderStatus;
import com.plazoleta.domain.model.User;

public class MarkOrderReadyService implements MarkOrderReadyUseCase {
    private final OrderRepositoryPort orderRepositoryPort;
    private final OrderTraceabilityPort orderTraceabilityPort;
    private final UserRepositoryPort userRepositoryPort;
    private final PinGeneratorPort pinGeneratorPort;
    private final SmsNotificationPort smsNotificationPort;

    public MarkOrderReadyService(OrderRepositoryPort orderRepositoryPort, UserRepositoryPort userRepositoryPort,
                                 PinGeneratorPort pinGeneratorPort, SmsNotificationPort smsNotificationPort,
                                 OrderTraceabilityPort orderTraceabilityPort) {
        this.orderRepositoryPort = orderRepositoryPort;
        this.userRepositoryPort = userRepositoryPort;
        this.pinGeneratorPort = pinGeneratorPort;
        this.smsNotificationPort = smsNotificationPort;
        this.orderTraceabilityPort = orderTraceabilityPort;
    }

    @Override
    public OrderResponse execute(Long orderId, Long employeeId, Long employeeRestaurantId) {
        Order order = AssignOrderService.findOrderOfEmployeeRestaurant(orderRepositoryPort, orderId,
                employeeRestaurantId);
        AssignOrderService.requireAssignedEmployee(order, employeeId);
        String pin = pinGeneratorPort.generate();
        OrderStatus previousStatus = order.getStatus();
        order.markReady(pin);

        // El teléfono del cliente vive en el microservicio de usuarios.
        User client = userRepositoryPort.findRegisteredById(order.getClientId())
                .orElseThrow(() -> new UserNotFoundException(MensajesError.CUENTA_DEL_CLIENTE_NO_EXISTE));
        if (client.getPhone() == null || client.getPhone().isBlank()) {
            throw new IllegalArgumentException(MensajesError.CLIENTE_NO_TIENE_UN_TELEFONO_PARA_NOTIFICARLE);
        }
        // 1) Se marca LISTO solo si sigue EN_PREPARACION en la base: un doble clic no manda dos SMS con PIN distintos.
        if (!orderRepositoryPort.updateIfStatus(order, previousStatus)) {
            throw new InvalidOrderStatusException(
                    MensajesError.PEDIDO_ACABA_DE_CAMBIAR_SOLO_SE_MARCAN);
        }
        // 2) Se envía el SMS. Si falla, el pedido vuelve a EN_PREPARACION sin PIN y el empleado puede reintentar.
        try {
            smsNotificationPort.send(client.getPhone(), readyMessage(order.getId(), pin));
        } catch (RuntimeException smsFailure) {
            order.undoReady();
            orderRepositoryPort.updateIfStatus(order, OrderStatus.LISTO);
            throw smsFailure;
        }
        orderTraceabilityPort.record(order, previousStatus);
        return OrderResponse.from(order);
    }

    static String readyMessage(Long orderId, String pin) {
        return "Plazoleta: tu pedido #" + orderId + " está listo. PIN para reclamarlo: " + pin;
    }
}
