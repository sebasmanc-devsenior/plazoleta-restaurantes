package com.plazoleta.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.plazoleta.application.dto.DishResponse;
import com.plazoleta.application.dto.UpdateDishCommand;
import com.plazoleta.application.port.output.DishRepositoryPort;
import com.plazoleta.application.port.output.RestaurantRepositoryPort;
import com.plazoleta.application.port.output.UserRepositoryPort;
import com.plazoleta.domain.model.Role;
import com.plazoleta.domain.exception.DishNotFoundException;
import com.plazoleta.domain.exception.InvalidPriceException;
import com.plazoleta.domain.exception.RestaurantOwnershipException;
import com.plazoleta.domain.model.Dish;
import com.plazoleta.domain.model.Restaurant;
import com.plazoleta.domain.model.User;

/** HU-4: Modificar plato (solo precio y descripción). */
@ExtendWith(MockitoExtension.class)
class UpdateDishServiceTest {
    private static final long OWNER_ID = 1L;
    private static final long RESTAURANT_ID = 10L;
    private static final long DISH_ID = 100L;

    @Mock
    private DishRepositoryPort dishRepositoryPort;
    @Mock
    private RestaurantRepositoryPort restaurantRepositoryPort;
    @Mock
    private UserRepositoryPort userRepositoryPort;
    @InjectMocks
    private UpdateDishService service;

    private Dish dish;

    @BeforeEach
    void setUp() {
        dish = new Dish(DISH_ID, "Bandeja paisa", 25000, "Plato típico", "https://img.png", "Almuerzos", RESTAURANT_ID, true);
    }

    private void ownerOwnsRestaurant(long restaurantOwnerId) {
        when(userRepositoryPort.findById(OWNER_ID))
                .thenReturn(Optional.of(new User(OWNER_ID, "owner@test.com", null, Role.PROPIETARIO, null)));
        when(dishRepositoryPort.findById(DISH_ID)).thenReturn(Optional.of(dish));
        when(restaurantRepositoryPort.findById(RESTAURANT_ID)).thenReturn(Optional.of(new Restaurant(
                RESTAURANT_ID, "Restaurante", "900123456", "Calle 1", "+573001234567", "logo.png", restaurantOwnerId)));
    }

    @Test
    void updatesOnlyPriceAndDescription() {
        ownerOwnsRestaurant(OWNER_ID);
        when(dishRepositoryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));

        DishResponse response = service.execute(DISH_ID, new UpdateDishCommand(30000, "Nueva descripción"), OWNER_ID);

        assertThat(response.getPrice()).isEqualTo(30000);
        assertThat(response.getDescription()).isEqualTo("Nueva descripción");
        assertThat(response.getName()).isEqualTo("Bandeja paisa");
        assertThat(response.getImageUrl()).isEqualTo("https://img.png");
        assertThat(response.getCategory()).isEqualTo("Almuerzos");
        assertThat(response.getRestaurantId()).isEqualTo(RESTAURANT_ID);
        assertThat(response.isActive()).isTrue();
    }

    @Test
    void rejectsPriceThatIsNotPositive() {
        ownerOwnsRestaurant(OWNER_ID);

        assertThatThrownBy(() -> service.execute(DISH_ID, new UpdateDishCommand(0, "Descripción"), OWNER_ID))
                .isInstanceOf(InvalidPriceException.class);
        verify(dishRepositoryPort, never()).save(any());
    }

    @Test
    void rejectsBlankDescription() {
        ownerOwnsRestaurant(OWNER_ID);

        assertThatThrownBy(() -> service.execute(DISH_ID, new UpdateDishCommand(30000, " "), OWNER_ID))
                .isInstanceOf(IllegalArgumentException.class);
        verify(dishRepositoryPort, never()).save(any());
    }

    @Test
    void rejectsOwnerOfAnotherRestaurant() {
        ownerOwnsRestaurant(99L);

        assertThatThrownBy(() -> service.execute(DISH_ID, new UpdateDishCommand(30000, "Descripción"), OWNER_ID))
                .isInstanceOf(RestaurantOwnershipException.class);
        verify(dishRepositoryPort, never()).save(any());
    }

    @Test
    void rejectsDishThatDoesNotExist() {
        when(userRepositoryPort.findById(OWNER_ID))
                .thenReturn(Optional.of(new User(OWNER_ID, "owner@test.com", null, Role.PROPIETARIO, null)));
        when(dishRepositoryPort.findById(DISH_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.execute(DISH_ID, new UpdateDishCommand(30000, "Descripción"), OWNER_ID))
                .isInstanceOf(DishNotFoundException.class);
    }
}
