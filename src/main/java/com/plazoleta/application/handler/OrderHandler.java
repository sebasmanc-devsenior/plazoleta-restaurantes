package com.plazoleta.application.handler;

import com.plazoleta.application.dto.CreateOrderCommand;
import com.plazoleta.application.dto.DeliverOrderCommand;
import com.plazoleta.application.dto.OrderResponse;
import com.plazoleta.application.dto.PageResult;
import com.plazoleta.application.port.input.AssignOrderPort;
import com.plazoleta.application.port.input.CancelOrderPort;
import com.plazoleta.application.port.input.CreateOrderPort;
import com.plazoleta.application.port.input.DeliverOrderPort;
import com.plazoleta.application.port.input.ListOrdersByStatusPort;
import com.plazoleta.application.port.input.MarkOrderReadyPort;

/** Delega en los casos de uso de pedidos. Se registra como @Bean en BeanConfiguration (sin Spring aquí). */
public class OrderHandler implements IOrderHandler {
    private final CreateOrderPort createOrderUseCase;
    private final ListOrdersByStatusPort listOrdersByStatusUseCase;
    private final AssignOrderPort assignOrderUseCase;
    private final MarkOrderReadyPort markOrderReadyUseCase;
    private final DeliverOrderPort deliverOrderUseCase;
    private final CancelOrderPort cancelOrderUseCase;

    public OrderHandler(CreateOrderPort createOrderUseCase, ListOrdersByStatusPort listOrdersByStatusUseCase,
                        AssignOrderPort assignOrderUseCase, MarkOrderReadyPort markOrderReadyUseCase,
                        DeliverOrderPort deliverOrderUseCase, CancelOrderPort cancelOrderUseCase) {
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
