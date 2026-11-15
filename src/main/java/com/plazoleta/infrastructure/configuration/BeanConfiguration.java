package com.plazoleta.infrastructure.configuration;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.plazoleta.application.handler.DishHandler;
import com.plazoleta.application.handler.IDishHandler;
import com.plazoleta.application.handler.IOrderHandler;
import com.plazoleta.application.handler.IRestaurantHandler;
import com.plazoleta.application.handler.OrderHandler;
import com.plazoleta.application.handler.RestaurantHandler;
import com.plazoleta.application.port.input.CreateDishPort;
import com.plazoleta.application.port.input.AssignOrderPort;
import com.plazoleta.application.port.input.CancelOrderPort;
import com.plazoleta.application.port.input.CreateOrderPort;
import com.plazoleta.application.port.input.DeliverOrderPort;
import com.plazoleta.application.port.input.CreateRestaurantPort;
import com.plazoleta.application.port.input.GetRestaurantPort;
import com.plazoleta.application.port.input.ListOrdersByStatusPort;
import com.plazoleta.application.port.input.MarkOrderReadyPort;
import com.plazoleta.application.port.input.ListRestaurantDishesPort;
import com.plazoleta.application.port.input.ListRestaurantsPort;
import com.plazoleta.application.port.input.SetDishAvailabilityPort;
import com.plazoleta.application.port.input.UpdateDishPort;
import com.plazoleta.application.port.output.DishRepositoryPort;
import com.plazoleta.application.port.output.OrderRepositoryPort;
import com.plazoleta.application.port.output.OrderTraceabilityPort;
import com.plazoleta.application.port.output.PinGeneratorPort;
import com.plazoleta.application.port.output.SmsNotificationPort;
import com.plazoleta.application.port.output.RestaurantRepositoryPort;
import com.plazoleta.application.port.output.UserRepositoryPort;
import com.plazoleta.application.usecase.CreateDishUseCase;
import com.plazoleta.application.usecase.AssignOrderUseCase;
import com.plazoleta.application.usecase.CancelOrderUseCase;
import com.plazoleta.application.usecase.CreateOrderUseCase;
import com.plazoleta.application.usecase.DeliverOrderUseCase;
import com.plazoleta.application.usecase.CreateRestaurantUseCase;
import com.plazoleta.application.usecase.GetRestaurantUseCase;
import com.plazoleta.application.usecase.ListOrdersByStatusUseCase;
import com.plazoleta.application.usecase.MarkOrderReadyUseCase;
import com.plazoleta.application.usecase.ListRestaurantDishesUseCase;
import com.plazoleta.application.usecase.ListRestaurantsUseCase;
import com.plazoleta.application.usecase.SetDishAvailabilityUseCase;
import com.plazoleta.application.usecase.UpdateDishUseCase;

/**
 * Inyección de dependencias: conecta los casos de uso (application, sin Spring) con los
 * adaptadores de salida (infrastructure). Es el único lugar que conoce ambos lados.
 */
@Configuration
public class BeanConfiguration {

    /** HU-2 */
    @Bean
    public CreateRestaurantPort createRestaurantUseCase(RestaurantRepositoryPort restaurants,
                                                           UserRepositoryPort users) {
        return new CreateRestaurantUseCase(restaurants, users);
    }

    /** HU-3 */
    @Bean
    public CreateDishPort createDishUseCase(DishRepositoryPort dishes, RestaurantRepositoryPort restaurants,
                                               UserRepositoryPort users) {
        return new CreateDishUseCase(dishes, restaurants, users);
    }

    /** HU-4 */
    @Bean
    public UpdateDishPort updateDishUseCase(DishRepositoryPort dishes, RestaurantRepositoryPort restaurants,
                                               UserRepositoryPort users) {
        return new UpdateDishUseCase(dishes, restaurants, users);
    }

    /** HU-6: consulta interna que usa users-service para validar al dueño del restaurante. */
    @Bean
    public GetRestaurantPort getRestaurantUseCase(RestaurantRepositoryPort restaurants) {
        return new GetRestaurantUseCase(restaurants);
    }

    /** HU-7 */
    @Bean
    public SetDishAvailabilityPort setDishAvailabilityUseCase(DishRepositoryPort dishes,
                                                                 RestaurantRepositoryPort restaurants,
                                                                 UserRepositoryPort users) {
        return new SetDishAvailabilityUseCase(dishes, restaurants, users);
    }

    /** HU-9 */
    @Bean
    public ListRestaurantsPort listRestaurantsUseCase(RestaurantRepositoryPort restaurants) {
        return new ListRestaurantsUseCase(restaurants);
    }

    /** HU-10 */
    @Bean
    public ListRestaurantDishesPort listRestaurantDishesUseCase(DishRepositoryPort dishes,
                                                                   RestaurantRepositoryPort restaurants) {
        return new ListRestaurantDishesUseCase(dishes, restaurants);
    }

    /** Reloj del sistema: los casos de uso lo reciben para que las pruebas puedan fijar la hora. */
    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }

    /** HU-11 */
    @Bean
    public CreateOrderPort createOrderUseCase(OrderRepositoryPort orders, DishRepositoryPort dishes,
                                                 RestaurantRepositoryPort restaurants, UserRepositoryPort users,
                                                 OrderTraceabilityPort traceability, Clock clock) {
        return new CreateOrderUseCase(orders, dishes, restaurants, users, traceability, clock);
    }

    /** HU-12 */
    @Bean
    public ListOrdersByStatusPort listOrdersByStatusUseCase(OrderRepositoryPort orders) {
        return new ListOrdersByStatusUseCase(orders);
    }

    /** HU-13 */
    @Bean
    public AssignOrderPort assignOrderUseCase(OrderRepositoryPort orders, OrderTraceabilityPort traceability) {
        return new AssignOrderUseCase(orders, traceability);
    }

    /** HU-14 */
    @Bean
    public MarkOrderReadyPort markOrderReadyUseCase(OrderRepositoryPort orders, UserRepositoryPort users,
                                                       PinGeneratorPort pins, SmsNotificationPort sms,
                                                       OrderTraceabilityPort traceability) {
        return new MarkOrderReadyUseCase(orders, users, pins, sms, traceability);
    }

    /** HU-15 */
    @Bean
    public DeliverOrderPort deliverOrderUseCase(OrderRepositoryPort orders, OrderTraceabilityPort traceability) {
        return new DeliverOrderUseCase(orders, traceability);
    }

    /** HU-16 */
    @Bean
    public CancelOrderPort cancelOrderUseCase(OrderRepositoryPort orders, OrderTraceabilityPort traceability) {
        return new CancelOrderUseCase(orders, traceability);
    }

    /** Handler de restaurantes (HU 2 y 9, y la consulta interna). */
    @Bean
    public IRestaurantHandler restaurantHandler(CreateRestaurantPort createRestaurantUseCase,
                                                ListRestaurantsPort listRestaurantsUseCase,
                                                GetRestaurantPort getRestaurantUseCase) {
        return new RestaurantHandler(createRestaurantUseCase, listRestaurantsUseCase, getRestaurantUseCase);
    }

    /** Handler de platos (HU 3, 4, 7 y 10). */
    @Bean
    public IDishHandler dishHandler(CreateDishPort createDishUseCase, UpdateDishPort updateDishUseCase,
                                    SetDishAvailabilityPort setDishAvailabilityUseCase,
                                    ListRestaurantDishesPort listRestaurantDishesUseCase) {
        return new DishHandler(createDishUseCase, updateDishUseCase, setDishAvailabilityUseCase,
                listRestaurantDishesUseCase);
    }

    /** Handler de pedidos (HU 11 a 16). */
    @Bean
    public IOrderHandler orderHandler(CreateOrderPort createOrderUseCase,
                                      ListOrdersByStatusPort listOrdersByStatusUseCase,
                                      AssignOrderPort assignOrderUseCase, MarkOrderReadyPort markOrderReadyUseCase,
                                      DeliverOrderPort deliverOrderUseCase, CancelOrderPort cancelOrderUseCase) {
        return new OrderHandler(createOrderUseCase, listOrdersByStatusUseCase, assignOrderUseCase,
                markOrderReadyUseCase, deliverOrderUseCase, cancelOrderUseCase);
    }
}
