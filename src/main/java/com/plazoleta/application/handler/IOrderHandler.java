package com.plazoleta.application.handler;

import com.plazoleta.application.dto.CreateOrderCommand;
import com.plazoleta.application.dto.DeliverOrderCommand;
import com.plazoleta.application.dto.OrderResponse;
import com.plazoleta.application.dto.PageResult;

/**
 * Handler de pedidos (HU 11 a 16). No tiene reglas de negocio, solo delega en los casos de uso por sus
 * interfaces. Los ids del cliente, del empleado y de su restaurante los pone el controlador a partir del token.
 */
public interface IOrderHandler {
    OrderResponse createOrder(CreateOrderCommand command, Long clientId);

    PageResult<OrderResponse> listOrdersByStatus(Long employeeRestaurantId, String status, int page, int size);

    OrderResponse assignOrder(Long orderId, Long employeeId, Long employeeRestaurantId);

    OrderResponse markOrderReady(Long orderId, Long employeeId, Long employeeRestaurantId);

    OrderResponse deliverOrder(Long orderId, DeliverOrderCommand command, Long employeeId, Long employeeRestaurantId);

    OrderResponse cancelOrder(Long orderId, Long clientId);
}
