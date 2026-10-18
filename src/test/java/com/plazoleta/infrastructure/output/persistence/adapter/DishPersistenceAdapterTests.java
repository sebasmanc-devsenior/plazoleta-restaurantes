package com.plazoleta.infrastructure.output.persistence.adapter;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.plazoleta.application.port.output.DishRepositoryPort;
import com.plazoleta.application.port.output.RestaurantRepositoryPort;
import com.plazoleta.domain.model.Dish;
import com.plazoleta.domain.model.Restaurant;

/** HU-3: el adaptador JPA guarda el plato asociado a su restaurante y lo recupera igual. */
@SpringBootTest
@Transactional
class DishPersistenceAdapterTests {
    @Autowired
    private RestaurantRepositoryPort restaurantRepository;
    @Autowired
    private DishRepositoryPort dishRepository;

    @Test
    void savesDishLinkedToItsRestaurant() {
        Restaurant restaurant = restaurantRepository.save(new Restaurant(null, "Sabor Paisa", "900123456",
                "Calle 10", "+573001112233", "https://logo.png", 2L));

        Dish saved = dishRepository.save(new Dish(null, "Bandeja paisa", 32000, "Plato típico",
                "https://img.png", "Almuerzos", restaurant.getId(), true));
        Dish found = dishRepository.findById(saved.getId()).orElseThrow();

        assertThat(found.getRestaurantId()).isEqualTo(restaurant.getId());
        assertThat(found.getPrice()).isEqualTo(32000);
        assertThat(found.isActive()).isTrue();
    }
}
