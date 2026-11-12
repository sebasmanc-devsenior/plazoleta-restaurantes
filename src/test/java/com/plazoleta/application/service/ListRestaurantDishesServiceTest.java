package com.plazoleta.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.plazoleta.application.dto.DishResponse;
import com.plazoleta.application.dto.PageResult;
import com.plazoleta.application.port.output.DishRepositoryPort;
import com.plazoleta.application.port.output.RestaurantRepositoryPort;
import com.plazoleta.domain.exception.RestaurantNotFoundException;
import com.plazoleta.domain.model.Dish;
import com.plazoleta.domain.model.Restaurant;

/** HU-10: Listar los platos de un restaurante (paginado y filtrable por categoría). */
@ExtendWith(MockitoExtension.class)
class ListRestaurantDishesServiceTest {
    @Mock
    private DishRepositoryPort dishRepositoryPort;
    @Mock
    private RestaurantRepositoryPort restaurantRepositoryPort;
    @InjectMocks
    private ListRestaurantDishesService service;

    private static Dish dish(long id, String name, String category) {
        return dish(id, name, category, true);
    }

    private static Dish dish(long id, String name, String category, boolean active) {
        return new Dish(id, name, 15000, "Descripción", "https://" + id + ".png", category, 1L, active);
    }

    private void restaurantExists() {
        when(restaurantRepositoryPort.findById(1L)).thenReturn(Optional.of(
                new Restaurant(1L, "Sabor Paisa", "900123456", "Calle 1", "+573001234567", "https://logo.png", 2L)));
    }

    @Test
    void returnsDishesOfTheRestaurantWithPagingData() {
        restaurantExists();
        when(dishRepositoryPort.findByRestaurant(1L, null, 0, 2)).thenReturn(new PageResult<>(
                List.of(dish(1, "Arepa", "Entradas"), dish(2, "Bandeja", "Almuerzos")), 0, 2, 3));

        PageResult<DishResponse> page = service.execute(1L, null, 0, 2);

        assertThat(page.getContent()).extracting(DishResponse::getName).containsExactly("Arepa", "Bandeja");
        assertThat(page.getTotalElements()).isEqualTo(3);
        assertThat(page.getTotalPages()).isEqualTo(2);
    }

    @Test
    void disabledDishesAreListedAndMarkedAsNotAvailable() {
        restaurantExists();
        when(dishRepositoryPort.findByRestaurant(1L, null, 0, 10)).thenReturn(new PageResult<>(
                List.of(dish(1, "Ajiaco", "Almuerzos", false), dish(2, "Bandeja", "Almuerzos", true)), 0, 10, 2));

        PageResult<DishResponse> page = service.execute(1L, null, 0, 10);

        assertThat(page.getContent()).extracting(DishResponse::getName).containsExactly("Ajiaco", "Bandeja");
        assertThat(page.getContent()).extracting(DishResponse::isActive).containsExactly(false, true);
    }

    @Test
    void passesTheTrimmedCategoryFilterToTheRepository() {
        restaurantExists();
        when(dishRepositoryPort.findByRestaurant(1L, "Postres", 0, 10))
                .thenReturn(new PageResult<>(List.of(dish(3, "Flan", "Postres")), 0, 10, 1));

        PageResult<DishResponse> page = service.execute(1L, "  Postres ", 0, 10);

        assertThat(page.getContent()).extracting(DishResponse::getCategory).containsExactly("Postres");
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "   "})
    void emptyCategoryMeansAllCategories(String category) {
        restaurantExists();
        when(dishRepositoryPort.findByRestaurant(1L, null, 0, 10))
                .thenReturn(new PageResult<>(List.of(), 0, 10, 0));

        service.execute(1L, category, 0, 10);

        verify(dishRepositoryPort).findByRestaurant(1L, null, 0, 10);
    }

    @Test
    void rejectsUnknownRestaurant() {
        when(restaurantRepositoryPort.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.execute(99L, null, 0, 10)).isInstanceOf(RestaurantNotFoundException.class);
        verify(dishRepositoryPort, never()).findByRestaurant(anyLong(), any(), anyInt(), anyInt());
    }

    @ParameterizedTest
    @CsvSource({"-1, 10", "0, 0", "0, 101"})
    void rejectsInvalidPageOrSize(int page, int size) {
        assertThatThrownBy(() -> service.execute(1L, null, page, size)).isInstanceOf(IllegalArgumentException.class);
        verify(dishRepositoryPort, never()).findByRestaurant(anyLong(), any(), anyInt(), anyInt());
    }
}
