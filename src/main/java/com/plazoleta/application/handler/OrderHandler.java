package com.plazoleta.application.handler;

import com.plazoleta.application.dto.CreateOrderCommand;
import com.plazoleta.application.dto.DeliverOrderCommand;
import com.plazoleta.application.dto.OrderResponse;
import com.plazoleta.application.dto.PageResult;
import com.plazoleta.application.port.input.AssignOrderUseCase;
import com.plazoleta.application.port.input.CancelOrderUseCase;
import com.plazoleta.application.port.input.CreateOrderUseCase;
import com.plazoleta.application.port.input.DeliverOrderUseCase;
import com.plazoleta.application.port.input.ListOrdersByStatusUseCase;
import com.plazoleta.application.port.input.MarkOrderReadyUseCase;

/** Delega en los casos de uso de pedidos. Se registra como @Bean en BeanConfiguration (sin Spring aquí). */
public class OrderHandler implements IOrderHandler {
    private final CreateOrderUseCase createOrderUseCase;
    private final ListOrdersByStatusUseCase listOrdersByStatusUseCase;
    private final AssignOrderUseCase assignOrderUseCase;
    private final MarkOrderReadyUseCase markOrderReadyUseCase;
    private final DeliverOrderUseCase deliverOrderUseCase;
    private final CancelOrderUseCase cancelOrderUseCase;

    public OrderHandler(CreateOrderUseCase createOrderUseCase, ListOrdersByStatusUseCase listOrdersByStatusUseCase,
                        AssignOrderUseCase assignOrderUseCase, MarkOrderReadyUseCase markOrderReadyUseCase,
                        DeliverOrderUseCase deliverOrderUseCase, CancelOrderUseCase cancelOrderUseCase) {
        this.createOrderUseCase = createOrderUseCase;
        this.listOrdersByStatusUseCase = listOrdersByStatusUseCase;
        this.assignOrderUseCase = assignOrderUseCase;
        this.markOrderReadyUseCase = markOrderReadyUseCase;
        this.deliverOrderUseCase = deliverOrderUseCase;
        this.cancelOrderUseCase = cancelOrderUseCase;
    }

    @Override
    public OrderResponse createOrder(CreateOrderCommand command, Long clientId) {
        return createOrderUseCase.execute(command, clientId);
    }

    @Override
    public PageResult<OrderResponse> listOrdersByStatus(Long employeeRestaurantId, String status, int page, int size) {
        return listOrdersByStatusUseCase.execute(employeeRestaurantId, status, page, size);
    }

    @Override
    public OrderResponse assignOrder(Long orderId, Long employeeId, Long employeeRestaurantId) {
        return assignOrderUseCase.execute(orderId, employeeId, employeeRestaurantId);
    }

    @Override
    public OrderResponse markOrderReady(Long orderId, Long employeeId, Long employeeRestaurantId) {
        return markOrderReadyUseCase.execute(orderId, employeeId, employeeRestaurantId);
    }

    @Override
    public OrderResponse deliverOrder(Long orderId, DeliverOrderCommand command, Long employeeId,
                                      Long employeeRestaurantId) {
        return deliverOrderUseCase.execute(orderId, command, employeeId, employeeRestaurantId);
    }

    @Override
    public OrderResponse cancelOrder(Long orderId, Long clientId) {
        return cancelOrderUseCase.execute(orderId, clientId);
    }
}
