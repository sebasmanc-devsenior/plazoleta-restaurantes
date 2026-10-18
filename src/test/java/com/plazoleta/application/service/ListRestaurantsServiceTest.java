package com.plazoleta.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.plazoleta.application.dto.PageResult;
import com.plazoleta.application.dto.RestaurantListItem;
import com.plazoleta.application.port.output.RestaurantRepositoryPort;
import com.plazoleta.domain.model.Restaurant;

/** HU-9: Listar restaurantes (orden alfabético, paginado, solo nombre y logo). */
@ExtendWith(MockitoExtension.class)
class ListRestaurantsServiceTest {
    @Mock
    private RestaurantRepositoryPort restaurantRepositoryPort;
    @InjectMocks
    private ListRestaurantsService service;

    private static Restaurant restaurant(long id, String name) {
        return new Restaurant(id, name, "900123456", "Calle 1", "+573001234567", "https://" + id + ".png", 1L);
    }

    @Test
    void returnsOnlyNameAndLogoInRepositoryOrderWithPagingData() {
        when(restaurantRepositoryPort.findAll(1, 2)).thenReturn(new PageResult<>(
                List.of(restaurant(3, "Arepas"), restaurant(1, "Burgers")), 1, 2, 5));

        PageResult<RestaurantListItem> page = service.execute(1, 2);

        assertThat(page.getContent()).extracting(RestaurantListItem::getName).containsExactly("Arepas", "Burgers");
        assertThat(page.getContent()).extracting(RestaurantListItem::getLogoUrl)
                .containsExactly("https://3.png", "https://1.png");
        assertThat(page.getPage()).isEqualTo(1);
        assertThat(page.getSize()).isEqualTo(2);
        assertThat(page.getTotalElements()).isEqualTo(5);
        assertThat(page.getTotalPages()).isEqualTo(3);
    }

    @Test
    void responseItemsExposeOnlyNameAndLogo() {
        assertThat(RestaurantListItem.class.getDeclaredFields())
                .extracting(java.lang.reflect.Field::getName)
                .containsExactlyInAnyOrder("name", "logoUrl");
    }

    @Test
    void returnsEmptyPageWhenThereAreNoRestaurants() {
        when(restaurantRepositoryPort.findAll(0, 10)).thenReturn(new PageResult<>(List.of(), 0, 10, 0));

        PageResult<RestaurantListItem> page = service.execute(0, 10);

        assertThat(page.getContent()).isEmpty();
        assertThat(page.getTotalPages()).isZero();
    }

    @ParameterizedTest
    @CsvSource({"-1, 10", "0, 0", "0, -5", "0, 101"})
    void rejectsInvalidPageOrSize(int page, int size) {
        assertThatThrownBy(() -> service.execute(page, size)).isInstanceOf(IllegalArgumentException.class);
        verify(restaurantRepositoryPort, never()).findAll(anyInt(), anyInt());
    }
}
