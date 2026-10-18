package com.plazoleta;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.plazoleta.application.port.output.RestaurantRepositoryPort;
import com.plazoleta.domain.model.Restaurant;
import com.plazoleta.domain.model.Role;
import com.plazoleta.domain.model.User;
import com.plazoleta.infrastructure.configuration.security.JwtTokenService;

/**
 * Pruebas de la API completa (seguridad + controladores + H2): cada rol solo accede a lo que
 * le corresponde (HU-5) y las reglas de cada historia se respetan de punta a punta.
 */
@SpringBootTest
@AutoConfigureMockMvc
class PlazoletaApplicationTests {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private JwtTokenService jwtTokenService;
    @Autowired
    private RestaurantRepositoryPort restaurantRepository;
    @Value("${app.internal.token}")
    private String internalToken;

    private String bearerFor(long userId, Role role) {
        User user = new User(userId, "user" + userId + "@test.com", null, role,
                role == Role.EMPLEADO ? 1L : null); // un token de empleado sin restaurante es inválido
        return "Bearer " + jwtTokenService.generateToken(user);
    }

    @Test
    void contextLoads() {
    }

    @Test
    void undeclaredRoutesAreDeniedEvenWithAValidToken() throws Exception {
        mockMvc.perform(get("/h2-console").header("Authorization", bearerFor(1L, Role.ADMIN)))
                .andExpect(status().isForbidden());
    }

    @Test
    void userAccountRoutesBelongToUsersServiceNotPlazoleta() throws Exception {
        // HU 1, 5, 6 y 8 viven en users-service (puerto 8081); plazoleta no las expone.
        for (String route : List.of("/api/owners", "/api/clients", "/api/employees", "/api/login")) {
            mockMvc.perform(post(route).header("Authorization", bearerFor(1L, Role.ADMIN))
                            .contentType(MediaType.APPLICATION_JSON).content("{}"))
                    .andExpect(status().isForbidden());
        }
    }

    // ---------- HU-2 Crear restaurante ----------

    @Test
    void creatingRestaurantWithoutTokenReturnsUnauthorized() throws Exception {
        mockMvc.perform(post("/api/restaurants").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void onlyAdminCanCreateRestaurants() throws Exception {
        mockMvc.perform(post("/api/restaurants").header("Authorization", bearerFor(5L, Role.PROPIETARIO))
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isForbidden());
    }

    // ---------- HU-3 Crear plato ----------

    @Test
    void onlyOwnerCanCreateDishes() throws Exception {
        mockMvc.perform(post("/api/dishes").header("Authorization", bearerFor(5L, Role.CLIENTE))
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void dishWithZeroPriceIsRejectedAsBadRequest() throws Exception {
        long ownerId = 55L;
        Restaurant restaurant = restaurantRepository.save(new Restaurant(null, "Sopas", "900111222", "Calle 1",
                "+573001234567", "https://logo.png", ownerId));
        mockMvc.perform(post("/api/dishes").header("Authorization", bearerFor(ownerId, Role.PROPIETARIO))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Sopa\",\"price\":0,\"description\":\"d\",\"imageUrl\":\"i\","
                                + "\"category\":\"c\",\"restaurantId\":" + restaurant.getId() + "}"))
                .andExpect(status().isBadRequest());
    }

    // ---------- HU-4 Modificar plato ----------

    @Test
    void onlyOwnerCanModifyDishes() throws Exception {
        mockMvc.perform(put("/api/dishes/1").header("Authorization", bearerFor(5L, Role.ADMIN))
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isForbidden());
    }

    // ---------- HU-6 Consulta interna del dueño (la usa users-service) ----------

    @Test
    void internalRestaurantLookupRequiresInternalToken() throws Exception {
        mockMvc.perform(get("/internal/restaurants/1"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/internal/restaurants/1").header("Authorization", bearerFor(5L, Role.PROPIETARIO)))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/internal/restaurants/999999").header("Authorization", "Bearer " + internalToken))
                .andExpect(status().isNotFound());
    }

    // ---------- HU-7 Habilitar / deshabilitar plato ----------

    @Test
    void onlyOwnerCanEnableOrDisableDishes() throws Exception {
        mockMvc.perform(patch("/api/dishes/1/availability").header("Authorization", bearerFor(5L, Role.EMPLEADO))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"active\":false}"))
                .andExpect(status().isForbidden());
    }

    // ---------- HU-9 Listar restaurantes ----------

    @Test
    void listingRestaurantsRequiresLoggedInClient() throws Exception {
        mockMvc.perform(get("/api/restaurants"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/restaurants").header("Authorization", bearerFor(5L, Role.PROPIETARIO)))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/restaurants").param("page", "0").param("size", "5")
                        .header("Authorization", bearerFor(7L, Role.CLIENTE)))
                .andExpect(status().isOk());
    }
}
