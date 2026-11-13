package com.plazoleta.application.port.output;

import com.plazoleta.domain.model.Order;
import com.plazoleta.domain.model.OrderStatus;

/** HU-17: registro de cada cambio de estado de un pedido. Lo resuelve el microservicio de trazabilidad. */
public interface OrderTraceabilityPort {
    /**
     * Registra que el pedido (ya guardado, con su estado nuevo) cambió desde previousStatus.
     * previousStatus es null cuando el pedido se acaba de crear.
     */
    void record(Order order, OrderStatus previousStatus);
}
