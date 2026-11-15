package com.plazoleta.application.service;

import java.util.Locale;

import com.plazoleta.application.dto.OrderResponse;
import com.plazoleta.application.dto.PageResult;
import com.plazoleta.application.port.input.ListOrdersByStatusUseCase;
import com.plazoleta.application.port.output.OrderRepositoryPort;
import com.plazoleta.domain.exception.ForbiddenOperationException;
import com.plazoleta.domain.model.OrderStatus;

public class ListOrdersByStatusService implements ListOrdersByStatusUseCase {
    private final OrderRepositoryPort orderRepositoryPort;

    public ListOrdersByStatusService(OrderRepositoryPort orderRepositoryPort) {
        this.orderRepositoryPort = orderRepositoryPort;
    }

    @Override
    public PageResult<OrderResponse> execute(Long employeeRestaurantId, String status, int page, int size) {
        // El restaurante sale del token del empleado: solo ve los pedidos del restaurante donde trabaja.
        if (employeeRestaurantId == null) {
            throw new ForbiddenOperationException("El empleado no está vinculado a un restaurante");
        }
        ListRestaurantsService.validatePage(page, size);
        OrderStatus orderStatus = parseStatus(status);
        var result = orderRepositoryPort.findByRestaurantAndStatus(employeeRestaurantId, orderStatus, page, size);
        return new PageResult<>(result.getContent().stream().map(OrderResponse::from).toList(),
                page, size, result.getTotalElements());
    }

    static OrderStatus parseStatus(String status) {
        if (status == null || status.isBlank()) {
            throw new IllegalArgumentException("El estado es obligatorio");
        }
        try {
            return OrderStatus.valueOf(status.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException(
                    "Estado desconocido. Usa PENDIENTE, EN_PREPARACION, LISTO, ENTREGADO o CANCELADO");
        }
    }
}
