package com.plazoleta.application.usecase;

import com.plazoleta.domain.util.MensajesError;

import java.util.Locale;

import com.plazoleta.application.dto.OrderResponse;
import com.plazoleta.application.dto.PageResult;
import com.plazoleta.application.port.input.ListOrdersByStatusPort;
import com.plazoleta.application.port.output.OrderRepositoryPort;
import com.plazoleta.domain.exception.ForbiddenOperationException;
import com.plazoleta.domain.model.OrderStatus;

public class ListOrdersByStatusUseCase implements ListOrdersByStatusPort {
    private final OrderRepositoryPort orderRepositoryPort;

    public ListOrdersByStatusUseCase(OrderRepositoryPort orderRepositoryPort) {
        this.orderRepositoryPort = orderRepositoryPort;
    }

    @Override
    public PageResult<OrderResponse> execute(Long employeeRestaurantId, String status, int page, int size) {
        // El restaurante sale del token del empleado: solo ve los pedidos del restaurante donde trabaja.
        if (employeeRestaurantId == null) {
            throw new ForbiddenOperationException(MensajesError.EMPLEADO_NO_ESTA_VINCULADO_A_UN_RESTAURANTE);
        }
        ListRestaurantsUseCase.validatePage(page, size);
        OrderStatus orderStatus = parseStatus(status);
        var result = orderRepositoryPort.findByRestaurantAndStatus(employeeRestaurantId, orderStatus, page, size);
        return new PageResult<>(result.getContent().stream().map(OrderResponse::from).toList(),
                page, size, result.getTotalElements());
    }

    static OrderStatus parseStatus(String status) {
        if (status == null || status.isBlank()) {
            throw new IllegalArgumentException(MensajesError.ESTADO_ES_OBLIGATORIO);
        }
        try {
            return OrderStatus.valueOf(status.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException(
                    MensajesError.ESTADO_DESCONOCIDO_USA_PENDIENTE_EN_PREPARACION_LISTO);
        }
    }
}
