package com.plazoleta.infrastructure.output.persistence.adapter;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.plazoleta.application.port.output.RestaurantRepositoryPort;
import com.plazoleta.domain.model.Restaurant;

/** HU-9: el adaptador JPA ordena alfabéticamente y pagina según el tamaño pedido. */
@SpringBootTest
@Transactional
class RestaurantPersistenceAdapterTests {
    @Autowired
    private RestaurantRepositoryPort restaurantRepository;

    private void save(String name) {
        restaurantRepository.save(new Restaurant(null, name, "900123456", "Calle 1", "+573001234567",
                "https://logo.png", 1L));
    }

    @Test
    void listsRestaurantsAlphabeticallyAndPaginated() {
        save("Zeta Grill");
        save("Arepas Doña Ana");
        save("Mar y Tierra");

        var firstPage = restaurantRepository.findAll(0, 2);
        var secondPage = restaurantRepository.findAll(1, 2);

        assertThat(firstPage.getContent()).extracting(Restaurant::getName)
                .containsExactly("Arepas Doña Ana", "Mar y Tierra");
        assertThat(secondPage.getContent()).extracting(Restaurant::getName).containsExactly("Zeta Grill");
        assertThat(firstPage.getTotalElements()).isEqualTo(3);
        assertThat(firstPage.getTotalPages()).isEqualTo(2);
    }
}
