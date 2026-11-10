package com.plazoleta.application.port.output;

import java.util.Set;

import com.plazoleta.domain.model.Order;
import com.plazoleta.domain.model.OrderStatus;

public interface OrderRepositoryPort {
    Order save(Order order);
    boolean existsByClientIdAndStatusIn(Long clientId, Set<OrderStatus> statuses);
}
