package com.plazoleta;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

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
}
