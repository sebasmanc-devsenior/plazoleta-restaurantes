package com.plazoleta.application.handler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.plazoleta.application.dto.CreateDishCommand;
import com.plazoleta.application.dto.CreateOrderCommand;
import com.plazoleta.application.dto.CreateRestaurantCommand;
import com.plazoleta.application.dto.DeliverOrderCommand;
import com.plazoleta.application.dto.DishResponse;
import com.plazoleta.application.dto.OrderItemCommand;
import com.plazoleta.application.dto.OrderResponse;
import com.plazoleta.application.dto.PageResult;
import com.plazoleta.application.dto.RestaurantListItem;
import com.plazoleta.application.dto.RestaurantResponse;
import com.plazoleta.application.dto.UpdateDishCommand;
import com.plazoleta.application.port.input.AssignOrderPort;
import com.plazoleta.application.port.input.CancelOrderPort;
import com.plazoleta.application.port.input.CreateDishPort;
import com.plazoleta.application.port.input.CreateOrderPort;
import com.plazoleta.application.port.input.CreateRestaurantPort;
import com.plazoleta.application.port.input.DeliverOrderPort;
import com.plazoleta.application.port.input.GetRestaurantPort;
import com.plazoleta.application.port.input.ListOrdersByStatusPort;
import com.plazoleta.application.port.input.ListRestaurantDishesPort;
import com.plazoleta.application.port.input.ListRestaurantsPort;
import com.plazoleta.application.port.input.MarkOrderReadyPort;
import com.plazoleta.application.port.input.SetDishAvailabilityPort;
import com.plazoleta.application.port.input.UpdateDishPort;

/**
 * Los handlers no deciden nada: cada método pasa sus datos, sin cambiarlos, al caso de uso que le corresponde y
 * devuelve la misma respuesta.
 */
class HandlersTest {
    private final CreateRestaurantPort createRestaurant = mock(CreateRestaurantPort.class);
    private final ListRestaurantsPort listRestaurants = mock(ListRestaurantsPort.class);
    private final GetRestaurantPort getRestaurant = mock(GetRestaurantPort.class);
    private final RestaurantHandler restaurantHandler =
            new RestaurantHandler(createRestaurant, listRestaurants, getRestaurant);

    private final CreateDishPort createDish = mock(CreateDishPort.class);
    private final UpdateDishPort updateDish = mock(UpdateDishPort.class);
    private final SetDishAvailabilityPort setAvailability = mock(SetDishAvailabilityPort.class);
    private final ListRestaurantDishesPort listDishes = mock(ListRestaurantDishesPort.class);
    private final DishHandler dishHandler = new DishHandler(createDish, updateDish, setAvailability, listDishes);

    private final CreateOrderPort createOrder = mock(CreateOrderPort.class);
    private final ListOrdersByStatusPort listOrders = mock(ListOrdersByStatusPort.class);
    private final AssignOrderPort assignOrder = mock(AssignOrderPort.class);
    private final MarkOrderReadyPort markReady = mock(MarkOrderReadyPort.class);
    private final DeliverOrderPort deliverOrder = mock(DeliverOrderPort.class);
    private final CancelOrderPort cancelOrder = mock(CancelOrderPort.class);
    private final OrderHandler orderHandler =
            new OrderHandler(createOrder, listOrders, assignOrder, markReady, deliverOrder, cancelOrder);

    @Test
    void restaurantHandlerDelegatesEachOperation() {
        CreateRestaurantCommand command = new CreateRestaurantCommand("Sopas", "900", "Calle 1", "3001234567",
                "https://logo.png", 5L);
        RestaurantResponse restaurant = mock(RestaurantResponse.class);
        PageResult<RestaurantListItem> page = new PageResult<>(List.of(), 0, 10, 0);
        when(createRestaurant.execute(command)).thenReturn(restaurant);
        when(listRestaurants.execute(0, 10)).thenReturn(page);
        when(getRestaurant.execute(3L)).thenReturn(restaurant);

        assertThat(restaurantHandler.createRestaurant(command)).isSameAs(restaurant);
        assertThat(restaurantHandler.listRestaurants(0, 10)).isSameAs(page);
        assertThat(restaurantHandler.getRestaurant(3L)).isSameAs(restaurant);
    }

    @Test
    void dishHandlerPassesTheOwnerFromTheTokenToEachUseCase() {
        CreateDishCommand create = new CreateDishCommand("Sopa", 12000, "Caliente", "https://img.png", "Sopas", 3L);
        UpdateDishCommand update = new UpdateDishCommand(15000, "Más grande");
        DishResponse dish = mock(DishResponse.class);
        PageResult<DishResponse> page = new PageResult<>(List.of(dish), 0, 10, 1);
        when(createDish.execute(create, 55L)).thenReturn(dish);
        when(updateDish.execute(9L, update, 55L)).thenReturn(dish);
        when(setAvailability.execute(9L, false, 55L)).thenReturn(dish);
        when(listDishes.execute(3L, "Sopas", 0, 10)).thenReturn(page);

        assertThat(dishHandler.createDish(create, 55L)).isSameAs(dish);
        assertThat(dishHandler.updateDish(9L, update, 55L)).isSameAs(dish);
        assertThat(dishHandler.setDishAvailability(9L, false, 55L)).isSameAs(dish);
        assertThat(dishHandler.listRestaurantDishes(3L, "Sopas", 0, 10)).isSameAs(page);
        verify(setAvailability).execute(9L, false, 55L);
    }

    @Test
    void orderHandlerPassesWhoCallsAndTheirRestaurantToEachUseCase() {
        CreateOrderCommand create = new CreateOrderCommand(3L, List.of(new OrderItemCommand(4L, 2)));
        DeliverOrderCommand deliver = new DeliverOrderCommand("048213");
        OrderResponse order = mock(OrderResponse.class);
        PageResult<OrderResponse> page = new PageResult<>(List.of(order), 0, 10, 1);
        when(createOrder.execute(create, 7L)).thenReturn(order);
        when(listOrders.execute(3L, "PENDIENTE", 0, 10)).thenReturn(page);
        when(assignOrder.execute(20L, 8L, 3L)).thenReturn(order);
        when(markReady.execute(20L, 8L, 3L)).thenReturn(order);
        when(deliverOrder.execute(20L, deliver, 8L, 3L)).thenReturn(order);
        when(cancelOrder.execute(20L, 7L)).thenReturn(order);

        assertThat(orderHandler.createOrder(create, 7L)).isSameAs(order);
        assertThat(orderHandler.listOrdersByStatus(3L, "PENDIENTE", 0, 10)).isSameAs(page);
        assertThat(orderHandler.assignOrder(20L, 8L, 3L)).isSameAs(order);
        assertThat(orderHandler.markOrderReady(20L, 8L, 3L)).isSameAs(order);
        assertThat(orderHandler.deliverOrder(20L, deliver, 8L, 3L)).isSameAs(order);
        assertThat(orderHandler.cancelOrder(20L, 7L)).isSameAs(order);
        verify(deliverOrder).execute(20L, deliver, 8L, 3L);
    }
}
