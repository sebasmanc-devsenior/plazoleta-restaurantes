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

    /** HU-10: platos del restaurante (también los deshabilitados), por nombre, paginados y por categoría. */
    @Test
    void listsDishesOfTheRestaurantIncludingDisabledOnesFilteredByCategory() {
        Long restaurantId = restaurantRepository.save(new Restaurant(null, "Sabor Paisa", "900123456",
                "Calle 10", "+573001112233", "https://logo.png", 2L)).getId();
        Long otherRestaurantId = restaurantRepository.save(new Restaurant(null, "Otro", "900999999",
                "Calle 20", "+573001112244", "https://otro.png", 3L)).getId();
        dishRepository.save(new Dish(null, "Tamal", 12000, "d", "https://i.png", "Almuerzos", restaurantId, true));
        dishRepository.save(new Dish(null, "Arroz con leche", 6000, "d", "https://i.png", "Postres", restaurantId, true));
        dishRepository.save(new Dish(null, "Bandeja", 32000, "d", "https://i.png", "almuerzos", restaurantId, true));
        dishRepository.save(new Dish(null, "Ajiaco", 25000, "d", "https://i.png", "Almuerzos", restaurantId, false));
        dishRepository.save(new Dish(null, "Sancocho", 20000, "d", "https://i.png", "Almuerzos", otherRestaurantId, true));

        var firstPage = dishRepository.findByRestaurant(restaurantId, null, 0, 3);
        var secondPage = dishRepository.findByRestaurant(restaurantId, null, 1, 3);
        var lunch = dishRepository.findByRestaurant(restaurantId, "ALMUERZOS", 0, 10);

        assertThat(firstPage.getContent()).extracting(Dish::getName)
                .containsExactly("Ajiaco", "Arroz con leche", "Bandeja");
        assertThat(firstPage.getContent()).extracting(Dish::isActive).containsExactly(false, true, true);
        assertThat(secondPage.getContent()).extracting(Dish::getName).containsExactly("Tamal");
        assertThat(firstPage.getTotalElements()).isEqualTo(4);
        assertThat(lunch.getContent()).extracting(Dish::getName).containsExactly("Ajiaco", "Bandeja", "Tamal");
    }
}
