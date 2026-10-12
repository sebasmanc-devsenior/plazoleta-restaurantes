package com.plazoleta.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.plazoleta.application.ports.input.CreateRestaurantUseCase;
import com.plazoleta.application.ports.output.RestaurantRepositoryPort;
import com.plazoleta.application.ports.output.UserRepositoryPort;
import com.plazoleta.application.services.CreateRestaurantService;

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
}
