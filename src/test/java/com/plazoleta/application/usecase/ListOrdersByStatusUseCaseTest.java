package com.plazoleta.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.plazoleta.application.dto.OrderResponse;
import com.plazoleta.application.dto.PageResult;
import com.plazoleta.application.port.output.OrderRepositoryPort;
import com.plazoleta.domain.exception.ForbiddenOperationException;
import com.plazoleta.domain.model.Order;
import com.plazoleta.domain.model.OrderItem;
import com.plazoleta.domain.model.OrderStatus;

/** HU-12: Obtener la lista de pedidos filtrando por estado. */
@ExtendWith(MockitoExtension.class)
class ListOrdersByStatusServiceTest {
    @Mock
    private OrderRepositoryPort orderRepositoryPort;
    @InjectMocks
    private ListOrdersByStatusService service;

    private static Order order(long id) {
        return new Order(id, 7L, 1L, OrderStatus.PENDIENTE, LocalDateTime.of(2026, 11, 10, 12, 0),
                List.of(new OrderItem(10L, 2)));
    }

    @Test
    void listsOrdersOfTheEmployeeRestaurantWithAllFields() {
        when(orderRepositoryPort.findByRestaurantAndStatus(1L, OrderStatus.PENDIENTE, 0, 2))
                .thenReturn(new PageResult<>(List.of(order(5), order(6)), 0, 2, 3));

        PageResult<OrderResponse> page = service.execute(1L, "pendiente", 0, 2);

        assertThat(page.getContent()).extracting(OrderResponse::getId).containsExactly(5L, 6L);
        OrderResponse first = page.getContent().get(0);
        assertThat(first.getClientId()).isEqualTo(7L);
        assertThat(first.getRestaurantId()).isEqualTo(1L);
        assertThat(first.getStatus()).isEqualTo("PENDIENTE");
        assertThat(first.getCreatedAt()).isNotNull();
        assertThat(first.getDishes()).singleElement().satisfies(d -> assertThat(d.getQuantity()).isEqualTo(2));
        assertThat(page.getTotalPages()).isEqualTo(2);
    }

    @Test
    void employeeWithoutRestaurantIsForbidden() {
        assertThatThrownBy(() -> service.execute(null, "PENDIENTE", 0, 10))
                .isInstanceOf(ForbiddenOperationException.class);
        verify(orderRepositoryPort, never()).findByRestaurantAndStatus(anyLong(), any(), anyInt(), anyInt());
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "PAGADO"})
    void rejectsMissingOrUnknownStatus(String status) {
        assertThatThrownBy(() -> service.execute(1L, status, 0, 10)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsInvalidPageSize() {
        assertThatThrownBy(() -> service.execute(1L, "LISTO", 0, 0)).isInstanceOf(IllegalArgumentException.class);
    }
}
