package com.plazoleta.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.plazoleta.application.ports.input.CreateDishUseCase;
import com.plazoleta.application.ports.input.CreateRestaurantUseCase;
import com.plazoleta.application.ports.input.GetRestaurantUseCase;
import com.plazoleta.application.ports.input.UpdateDishUseCase;
import com.plazoleta.application.ports.output.DishRepositoryPort;
import com.plazoleta.application.ports.output.RestaurantRepositoryPort;
import com.plazoleta.application.ports.output.UserRepositoryPort;
import com.plazoleta.application.services.CreateDishService;
import com.plazoleta.application.services.CreateRestaurantService;
import com.plazoleta.application.services.GetRestaurantService;
import com.plazoleta.application.services.UpdateDishService;

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
}
