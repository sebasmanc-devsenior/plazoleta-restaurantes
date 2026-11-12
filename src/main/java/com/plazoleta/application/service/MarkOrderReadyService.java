package com.plazoleta.application.service;

import com.plazoleta.application.dto.OrderResponse;
import com.plazoleta.application.port.input.MarkOrderReadyUseCase;
import com.plazoleta.application.port.output.OrderRepositoryPort;
import com.plazoleta.application.port.output.PinGeneratorPort;
import com.plazoleta.application.port.output.SmsNotificationPort;
import com.plazoleta.application.port.output.UserRepositoryPort;
import com.plazoleta.domain.exception.UserNotFoundException;
import com.plazoleta.domain.model.Order;
import com.plazoleta.domain.model.User;

public class MarkOrderReadyService implements MarkOrderReadyUseCase {
    private final OrderRepositoryPort orderRepositoryPort;
    private final UserRepositoryPort userRepositoryPort;
    private final PinGeneratorPort pinGeneratorPort;
    private final SmsNotificationPort smsNotificationPort;

    public MarkOrderReadyService(OrderRepositoryPort orderRepositoryPort, UserRepositoryPort userRepositoryPort,
                                 PinGeneratorPort pinGeneratorPort, SmsNotificationPort smsNotificationPort) {
        this.orderRepositoryPort = orderRepositoryPort;
        this.userRepositoryPort = userRepositoryPort;
        this.pinGeneratorPort = pinGeneratorPort;
        this.smsNotificationPort = smsNotificationPort;
    }

    @Override
    public OrderResponse execute(Long orderId, Long employeeRestaurantId) {
        Order order = AssignOrderService.findOrderOfEmployeeRestaurant(orderRepositoryPort, orderId,
                employeeRestaurantId);
        String pin = pinGeneratorPort.generate();
        order.markReady(pin);

        // El teléfono del cliente vive en el microservicio de usuarios.
        User client = userRepositoryPort.findRegisteredById(order.getClientId())
                .orElseThrow(() -> new UserNotFoundException("Client account was not found"));
        if (client.getPhone() == null || client.getPhone().isBlank()) {
            throw new IllegalArgumentException("The client has no phone to be notified");
        }
        // Primero se envía el SMS: si falla, el pedido no se guarda como listo y se puede reintentar.
        smsNotificationPort.send(client.getPhone(), readyMessage(order.getId(), pin));
        return OrderResponse.from(orderRepositoryPort.save(order));
    }

    static String readyMessage(Long orderId, String pin) {
        return "Plazoleta: tu pedido #" + orderId + " está listo. PIN para reclamarlo: " + pin;
    }
}
