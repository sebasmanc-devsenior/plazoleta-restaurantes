package com.plazoleta.application.port.output;

import java.util.Optional;
import java.util.Set;

import com.plazoleta.application.dto.PageResult;
import com.plazoleta.domain.model.Order;
import com.plazoleta.domain.model.OrderStatus;

public interface OrderRepositoryPort {
    /** HU-11: guarda un pedido nuevo. Si el cliente ya tiene otro en proceso, lanza ActiveOrderExistsException. */
    Order save(Order order);

    /**
     * HU-13 a HU-16: guarda el nuevo estado, el empleado y el PIN del pedido solo si en la base el pedido sigue
     * en expectedStatus. Devuelve false si otra petición lo cambió antes (el cambio no se aplica).
     */
    boolean updateIfStatus(Order order, OrderStatus expectedStatus);
    /** HU-13: busca un pedido por su id. */
    Optional<Order> findById(Long id);
    boolean existsByClientIdAndStatusIn(Long clientId, Set<OrderStatus> statuses);
    /** HU-12: pedidos de un restaurante en un estado, del más antiguo al más reciente. */
    PageResult<Order> findByRestaurantAndStatus(Long restaurantId, OrderStatus status, int page, int size);
}
