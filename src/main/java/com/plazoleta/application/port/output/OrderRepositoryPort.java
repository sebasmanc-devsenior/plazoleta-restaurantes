package com.plazoleta.application.port.output;

import java.util.Optional;
import java.util.Set;

import com.plazoleta.application.dto.PageResult;
import com.plazoleta.domain.model.Order;
import com.plazoleta.domain.model.OrderStatus;

public interface OrderRepositoryPort {
    Order save(Order order);
    /** HU-13: busca un pedido por su id. */
    Optional<Order> findById(Long id);
    boolean existsByClientIdAndStatusIn(Long clientId, Set<OrderStatus> statuses);
    /** HU-12: pedidos de un restaurante en un estado, del más antiguo al más reciente. */
    PageResult<Order> findByRestaurantAndStatus(Long restaurantId, OrderStatus status, int page, int size);
}
