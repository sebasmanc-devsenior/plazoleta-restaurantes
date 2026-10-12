package com.plazoleta.application.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.plazoleta.application.dto.CreateDishCommand;
import com.plazoleta.application.dto.DishResponse;
import com.plazoleta.application.ports.output.DishRepositoryPort;
import com.plazoleta.application.ports.output.RestaurantRepositoryPort;
import com.plazoleta.application.ports.output.UserRepositoryPort;
import com.plazoleta.domain.models.Role;
import com.plazoleta.domain.exceptions.InvalidPriceException;
import com.plazoleta.domain.exceptions.RestaurantNotFoundException;
import com.plazoleta.domain.exceptions.RestaurantOwnershipException;
import com.plazoleta.domain.models.Dish;
import com.plazoleta.domain.models.Restaurant;
import com.plazoleta.domain.models.User;

/** HU-3: Crear plato. */
@ExtendWith(MockitoExtension.class)
class CreateDishServiceTest {
    private static final long OWNER_ID = 1L;
    private static final long RESTAURANT_ID = 10L;

    @Mock
    private DishRepositoryPort dishRepositoryPort;
    @Mock
    private RestaurantRepositoryPort restaurantRepositoryPort;
    @Mock
    private UserRepositoryPort userRepositoryPort;
    @InjectMocks
    private CreateDishService service;

    private static CreateDishCommand command(Integer price) {
        return new CreateDishCommand("Bandeja paisa", price, "Plato típico", "https://img.png", "Almuerzos", RESTAURANT_ID);
    }

    private void authenticatedAs(Role role) {
        when(userRepositoryPort.findById(OWNER_ID))
                .thenReturn(Optional.of(new User(OWNER_ID, "owner@test.com", null, role, null)));
    }

    private void restaurantOwnedBy(long ownerId) {
        when(restaurantRepositoryPort.findById(RESTAURANT_ID)).thenReturn(Optional.of(
                new Restaurant(RESTAURANT_ID, "Restaurante", "900123456", "Calle 1", "+573001234567", "logo.png", ownerId)));
    }

    @Test
    void createsActiveDishAssociatedToOwnersRestaurant() {
        authenticatedAs(Role.PROPIETARIO);
        restaurantOwnedBy(OWNER_ID);
        when(dishRepositoryPort.save(any())).thenAnswer(inv -> {
            Dish d = inv.getArgument(0);
            d.setId(100L);
            return d;
        });

        DishResponse response = service.execute(command(25000), OWNER_ID);

        ArgumentCaptor<Dish> saved = ArgumentCaptor.forClass(Dish.class);
        verify(dishRepositoryPort).save(saved.capture());
        assertThat(saved.getValue().isActive()).isTrue();
        assertThat(saved.getValue().getRestaurantId()).isEqualTo(RESTAURANT_ID);
        assertThat(response.getId()).isEqualTo(100L);
        assertThat(response.getPrice()).isEqualTo(25000);
        assertThat(response.isActive()).isTrue();
    }

    @Test
    void rejectsUserThatIsNotAnOwner() {
        authenticatedAs(Role.EMPLEADO);

        assertThatThrownBy(() -> service.execute(command(25000), OWNER_ID))
                .isInstanceOf(RestaurantOwnershipException.class);
        verify(dishRepositoryPort, never()).save(any());
    }

    @Test
    void rejectsOwnerOfAnotherRestaurant() {
        authenticatedAs(Role.PROPIETARIO);
        restaurantOwnedBy(99L);

        assertThatThrownBy(() -> service.execute(command(25000), OWNER_ID))
                .isInstanceOf(RestaurantOwnershipException.class);
        verify(dishRepositoryPort, never()).save(any());
    }

    @Test
    void rejectsDishForRestaurantThatDoesNotExist() {
        authenticatedAs(Role.PROPIETARIO);
        when(restaurantRepositoryPort.findById(RESTAURANT_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.execute(command(25000), OWNER_ID))
                .isInstanceOf(RestaurantNotFoundException.class);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, -5000})
    void rejectsPriceThatIsNotPositive(int price) {
        authenticatedAs(Role.PROPIETARIO);
        restaurantOwnedBy(OWNER_ID);

        assertThatThrownBy(() -> service.execute(command(price), OWNER_ID))
                .isInstanceOf(InvalidPriceException.class);
        verify(dishRepositoryPort, never()).save(any());
    }

    @Test
    void rejectsMissingPrice() {
        authenticatedAs(Role.PROPIETARIO);
        restaurantOwnedBy(OWNER_ID);

        assertThatThrownBy(() -> service.execute(command(null), OWNER_ID))
                .isInstanceOf(InvalidPriceException.class);
    }

    @Test
    void rejectsMissingCategory() {
        authenticatedAs(Role.PROPIETARIO);
        restaurantOwnedBy(OWNER_ID);
        CreateDishCommand noCategory = new CreateDishCommand("Bandeja", 25000, "Plato", "https://img.png", " ", RESTAURANT_ID);

        assertThatThrownBy(() -> service.execute(noCategory, OWNER_ID))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Category");
    }
}
