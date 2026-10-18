package com.plazoleta.infrastructure.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.plazoleta.application.port.input.CreateRestaurantUseCase;
import com.plazoleta.application.port.output.RestaurantRepositoryPort;
import com.plazoleta.application.port.output.UserRepositoryPort;
import com.plazoleta.application.service.CreateRestaurantService;

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
