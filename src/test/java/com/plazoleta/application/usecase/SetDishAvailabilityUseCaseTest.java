package com.plazoleta.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.plazoleta.application.dto.DishResponse;
import com.plazoleta.application.port.output.DishRepositoryPort;
import com.plazoleta.application.port.output.RestaurantRepositoryPort;
import com.plazoleta.application.port.output.UserRepositoryPort;
import com.plazoleta.domain.model.Role;
import com.plazoleta.domain.exception.DishNotFoundException;
import com.plazoleta.domain.exception.RestaurantOwnershipException;
import com.plazoleta.domain.model.Dish;
import com.plazoleta.domain.model.Restaurant;
import com.plazoleta.domain.model.User;

/** HU-7: Habilitar / deshabilitar plato. */
@ExtendWith(MockitoExtension.class)
class SetDishAvailabilityServiceTest {
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
    private SetDishAvailabilityService service;

    private Dish givenDish(boolean active, long restaurantOwnerId) {
        Dish dish = new Dish(DISH_ID, "Bandeja paisa", 25000, "Plato típico", "https://img.png", "Almuerzos",
                RESTAURANT_ID, active);
        when(userRepositoryPort.findById(OWNER_ID))
                .thenReturn(Optional.of(new User(OWNER_ID, "owner@test.com", null, Role.PROPIETARIO, null)));
        when(dishRepositoryPort.findById(DISH_ID)).thenReturn(Optional.of(dish));
        when(restaurantRepositoryPort.findById(RESTAURANT_ID)).thenReturn(Optional.of(new Restaurant(
                RESTAURANT_ID, "Restaurante", "900123456", "Calle 1", "+573001234567", "logo.png", restaurantOwnerId)));
        return dish;
    }

    @Test
    void deactivatesDishOfOwnRestaurant() {
        givenDish(true, OWNER_ID);
        when(dishRepositoryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));

        DishResponse response = service.execute(DISH_ID, false, OWNER_ID);

        assertThat(response.isActive()).isFalse();
        verify(dishRepositoryPort).save(any());
    }

    @Test
    void activatesDishOfOwnRestaurant() {
        givenDish(false, OWNER_ID);
        when(dishRepositoryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));

        DishResponse response = service.execute(DISH_ID, true, OWNER_ID);

        assertThat(response.isActive()).isTrue();
    }

    @Test
    void changesOnlyTheActiveFlag() {
        givenDish(true, OWNER_ID);
        when(dishRepositoryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));

        DishResponse response = service.execute(DISH_ID, false, OWNER_ID);

        assertThat(response.getName()).isEqualTo("Bandeja paisa");
        assertThat(response.getPrice()).isEqualTo(25000);
        assertThat(response.getDescription()).isEqualTo("Plato típico");
    }

    @Test
    void rejectsDishFromAnotherRestaurant() {
        Dish dish = givenDish(true, 99L);

        assertThatThrownBy(() -> service.execute(DISH_ID, false, OWNER_ID))
                .isInstanceOf(RestaurantOwnershipException.class);
        assertThat(dish.isActive()).isTrue();
        verify(dishRepositoryPort, never()).save(any());
    }

    @Test
    void rejectsUserThatIsNotAnOwner() {
        when(userRepositoryPort.findById(OWNER_ID))
                .thenReturn(Optional.of(new User(OWNER_ID, "client@test.com", null, Role.CLIENTE, null)));

        assertThatThrownBy(() -> service.execute(DISH_ID, false, OWNER_ID))
                .isInstanceOf(RestaurantOwnershipException.class);
        verify(dishRepositoryPort, never()).save(any());
    }

    @Test
    void rejectsDishThatDoesNotExist() {
        when(userRepositoryPort.findById(OWNER_ID))
                .thenReturn(Optional.of(new User(OWNER_ID, "owner@test.com", null, Role.PROPIETARIO, null)));
        when(dishRepositoryPort.findById(DISH_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.execute(DISH_ID, false, OWNER_ID))
                .isInstanceOf(DishNotFoundException.class);
    }
}
