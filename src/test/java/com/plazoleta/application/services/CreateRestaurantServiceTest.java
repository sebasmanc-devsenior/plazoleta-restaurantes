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

import com.plazoleta.application.dto.CreateRestaurantCommand;
import com.plazoleta.application.dto.RestaurantResponse;
import com.plazoleta.application.ports.output.RestaurantRepositoryPort;
import com.plazoleta.application.ports.output.UserRepositoryPort;
import com.plazoleta.domain.models.Role;
import com.plazoleta.domain.exceptions.UserNotFoundException;
import com.plazoleta.domain.models.Restaurant;
import com.plazoleta.domain.models.User;

/** HU-2: Crear restaurante. */
@ExtendWith(MockitoExtension.class)
class CreateRestaurantServiceTest {
    private static final long OWNER_ID = 5L;

    @Mock
    private RestaurantRepositoryPort restaurantRepositoryPort;
    @Mock
    private UserRepositoryPort userRepositoryPort;
    @InjectMocks
    private CreateRestaurantService service;

    private static CreateRestaurantCommand command(String name, String nit, String phone) {
        return new CreateRestaurantCommand(name, nit, "Calle 10 # 20-30", phone, "https://logo.png", OWNER_ID);
    }

    private void ownerExistsWithRole(Role role) {
        when(userRepositoryPort.findById(OWNER_ID))
                .thenReturn(Optional.of(new User(OWNER_ID, "owner@test.com", "+573001234567", role, null)));
    }

    @Test
    void createsRestaurantWhenUserHasOwnerRole() {
        ownerExistsWithRole(Role.PROPIETARIO);
        when(restaurantRepositoryPort.save(any())).thenAnswer(inv -> {
            Restaurant r = inv.getArgument(0);
            r.setId(1L);
            return r;
        });

        RestaurantResponse response = service.execute(command("Restaurante 123", "900123456", "+573005698325"));

        ArgumentCaptor<Restaurant> saved = ArgumentCaptor.forClass(Restaurant.class);
        verify(restaurantRepositoryPort).save(saved.capture());
        assertThat(saved.getValue().getOwnerUserId()).isEqualTo(OWNER_ID);
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("Restaurante 123");
        assertThat(response.getNit()).isEqualTo("900123456");
        assertThat(response.getOwnerUserId()).isEqualTo(OWNER_ID);
    }

    @Test
    void rejectsUserThatIsNotAnOwner() {
        ownerExistsWithRole(Role.CLIENTE);

        assertThatThrownBy(() -> service.execute(command("Restaurante", "900123456", "+573005698325")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("PROPIETARIO");
        verify(restaurantRepositoryPort, never()).save(any());
    }

    @Test
    void rejectsOwnerThatDoesNotExist() {
        when(userRepositoryPort.findById(OWNER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.execute(command("Restaurante", "900123456", "+573005698325")))
                .isInstanceOf(UserNotFoundException.class);
        verify(restaurantRepositoryPort, never()).save(any());
    }

    @Test
    void rejectsMissingOwnerId() {
        CreateRestaurantCommand withoutOwner = new CreateRestaurantCommand("Restaurante", "900123456",
                "Calle 1", "+573005698325", "https://logo.png", null);

        assertThatThrownBy(() -> service.execute(withoutOwner)).isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"90012345A", "900-123-456", "NIT900", ""})
    void rejectsNonNumericNit(String nit) {
        ownerExistsWithRole(Role.PROPIETARIO);

        assertThatThrownBy(() -> service.execute(command("Restaurante", nit, "+573005698325")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("NIT");
        verify(restaurantRepositoryPort, never()).save(any());
    }

    @ParameterizedTest
    @ValueSource(strings = {"+5730056983251", "30056983A5", "573-005-6983", "+57 300 569"})
    void rejectsPhoneLongerThan13CharactersOrNotNumeric(String phone) {
        ownerExistsWithRole(Role.PROPIETARIO);

        assertThatThrownBy(() -> service.execute(command("Restaurante", "900123456", phone)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("phone");
    }

    @ParameterizedTest
    @ValueSource(strings = {"12345", " 2024 "})
    void rejectsNameWithOnlyNumbers(String name) {
        ownerExistsWithRole(Role.PROPIETARIO);

        assertThatThrownBy(() -> service.execute(command(name, "900123456", "+573005698325")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("only numbers");
    }

    @Test
    void rejectsMissingRequiredFields() {
        ownerExistsWithRole(Role.PROPIETARIO);
        CreateRestaurantCommand noLogo = new CreateRestaurantCommand("Restaurante", "900123456",
                "Calle 1", "+573005698325", " ", OWNER_ID);

        assertThatThrownBy(() -> service.execute(noLogo)).isInstanceOf(IllegalArgumentException.class);
    }
}
