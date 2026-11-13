package com.plazoleta.infrastructure.configuration;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.plazoleta.application.port.input.CreateDishUseCase;
import com.plazoleta.application.port.input.AssignOrderUseCase;
import com.plazoleta.application.port.input.CancelOrderUseCase;
import com.plazoleta.application.port.input.CreateOrderUseCase;
import com.plazoleta.application.port.input.DeliverOrderUseCase;
import com.plazoleta.application.port.input.CreateRestaurantUseCase;
import com.plazoleta.application.port.input.GetRestaurantUseCase;
import com.plazoleta.application.port.input.ListOrdersByStatusUseCase;
import com.plazoleta.application.port.input.MarkOrderReadyUseCase;
import com.plazoleta.application.port.input.ListRestaurantDishesUseCase;
import com.plazoleta.application.port.input.ListRestaurantsUseCase;
import com.plazoleta.application.port.input.SetDishAvailabilityUseCase;
import com.plazoleta.application.port.input.UpdateDishUseCase;
import com.plazoleta.application.port.output.DishRepositoryPort;
import com.plazoleta.application.port.output.OrderRepositoryPort;
import com.plazoleta.application.port.output.OrderTraceabilityPort;
import com.plazoleta.application.port.output.PinGeneratorPort;
import com.plazoleta.application.port.output.SmsNotificationPort;
import com.plazoleta.application.port.output.RestaurantRepositoryPort;
import com.plazoleta.application.port.output.UserRepositoryPort;
import com.plazoleta.application.service.CreateDishService;
import com.plazoleta.application.service.AssignOrderService;
import com.plazoleta.application.service.CancelOrderService;
import com.plazoleta.application.service.CreateOrderService;
import com.plazoleta.application.service.DeliverOrderService;
import com.plazoleta.application.service.CreateRestaurantService;
import com.plazoleta.application.service.GetRestaurantService;
import com.plazoleta.application.service.ListOrdersByStatusService;
import com.plazoleta.application.service.MarkOrderReadyService;
import com.plazoleta.application.service.ListRestaurantDishesService;
import com.plazoleta.application.service.ListRestaurantsService;
import com.plazoleta.application.service.SetDishAvailabilityService;
import com.plazoleta.application.service.UpdateDishService;

/**
 * Inyección de dependencias: conecta los casos de uso (application, sin Spring) con los
 * adaptadores de salida (infrastructure). Es el único lugar que conoce ambos lados.
 */
@Configuration
public class BeanConfiguration {

    /** HU-2 */
    @Bean
    public CreateRestaurantUseCase createRestaurantUseCase(RestaurantRepositoryPort restaurants,
                                                           UserRepositoryPort users) {
        return new CreateRestaurantService(restaurants, users);
    }

    /** HU-3 */
    @Bean
    public CreateDishUseCase createDishUseCase(DishRepositoryPort dishes, RestaurantRepositoryPort restaurants,
                                               UserRepositoryPort users) {
        return new CreateDishService(dishes, restaurants, users);
    }

    /** HU-4 */
    @Bean
    public UpdateDishUseCase updateDishUseCase(DishRepositoryPort dishes, RestaurantRepositoryPort restaurants,
                                               UserRepositoryPort users) {
        return new UpdateDishService(dishes, restaurants, users);
    }

    /** HU-6: consulta interna que usa users-service para validar al dueño del restaurante. */
    @Bean
    public GetRestaurantUseCase getRestaurantUseCase(RestaurantRepositoryPort restaurants) {
        return new GetRestaurantService(restaurants);
    }

    /** HU-7 */
    @Bean
    public SetDishAvailabilityUseCase setDishAvailabilityUseCase(DishRepositoryPort dishes,
                                                                 RestaurantRepositoryPort restaurants,
                                                                 UserRepositoryPort users) {
        return new SetDishAvailabilityService(dishes, restaurants, users);
    }

    /** HU-9 */
    @Bean
    public ListRestaurantsUseCase listRestaurantsUseCase(RestaurantRepositoryPort restaurants) {
        return new ListRestaurantsService(restaurants);
    }

    /** HU-10 */
    @Bean
    public ListRestaurantDishesUseCase listRestaurantDishesUseCase(DishRepositoryPort dishes,
                                                                   RestaurantRepositoryPort restaurants) {
        return new ListRestaurantDishesService(dishes, restaurants);
    }

    /** Reloj del sistema: los casos de uso lo reciben para que las pruebas puedan fijar la hora. */
    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }

    /** HU-11 */
    @Bean
    public CreateOrderUseCase createOrderUseCase(OrderRepositoryPort orders, DishRepositoryPort dishes,
                                                 RestaurantRepositoryPort restaurants, UserRepositoryPort users,
                                                 OrderTraceabilityPort traceability, Clock clock) {
        return new CreateOrderService(orders, dishes, restaurants, users, traceability, clock);
    }

    /** HU-12 */
    @Bean
    public ListOrdersByStatusUseCase listOrdersByStatusUseCase(OrderRepositoryPort orders) {
        return new ListOrdersByStatusService(orders);
    }

    /** HU-13 */
    @Bean
    public AssignOrderUseCase assignOrderUseCase(OrderRepositoryPort orders, OrderTraceabilityPort traceability) {
        return new AssignOrderService(orders, traceability);
    }

    /** HU-14 */
    @Bean
    public MarkOrderReadyUseCase markOrderReadyUseCase(OrderRepositoryPort orders, UserRepositoryPort users,
                                                       PinGeneratorPort pins, SmsNotificationPort sms,
                                                       OrderTraceabilityPort traceability) {
        return new MarkOrderReadyService(orders, users, pins, sms, traceability);
    }

    /** HU-15 */
    @Bean
    public DeliverOrderUseCase deliverOrderUseCase(OrderRepositoryPort orders, OrderTraceabilityPort traceability) {
        return new DeliverOrderService(orders, traceability);
    }

    /** HU-16 */
    @Bean
    public CancelOrderUseCase cancelOrderUseCase(OrderRepositoryPort orders, OrderTraceabilityPort traceability) {
        return new CancelOrderService(orders, traceability);
    }
}
