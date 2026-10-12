package com.plazoleta.application.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.plazoleta.application.ports.output.RestaurantRepositoryPort;
import com.plazoleta.domain.exceptions.RestaurantNotFoundException;
import com.plazoleta.domain.models.Restaurant;

/** Consulta interna usada por users-service para validar la HU-6. */
@ExtendWith(MockitoExtension.class)
class GetRestaurantServiceTest {
    @Mock
    private RestaurantRepositoryPort restaurantRepositoryPort;
    @InjectMocks
    private GetRestaurantService service;

    @Test
    void returnsRestaurantWithItsOwner() {
        when(restaurantRepositoryPort.findById(10L)).thenReturn(Optional.of(
                new Restaurant(10L, "Restaurante", "900123456", "Calle 1", "+573001234567", "logo.png", 5L)));

        assertThat(service.execute(10L).getOwnerUserId()).isEqualTo(5L);
    }

    @Test
    void failsWhenRestaurantDoesNotExist() {
        when(restaurantRepositoryPort.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.execute(10L)).isInstanceOf(RestaurantNotFoundException.class);
    }
}
