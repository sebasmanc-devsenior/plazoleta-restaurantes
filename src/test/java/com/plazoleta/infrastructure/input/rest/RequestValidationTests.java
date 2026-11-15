package com.plazoleta.infrastructure.input.rest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import com.plazoleta.domain.model.Role;
import com.plazoleta.domain.model.User;
import com.plazoleta.domain.util.MensajesError;
import com.plazoleta.infrastructure.configuration.security.JwtTokenService;

/**
 * Validaciones Jakarta de los request DTO: un dato inválido responde 400 antes de llegar al caso de uso, con el
 * mensaje del primer campo inválido y todos los campos en "details".
 */
@SpringBootTest
@AutoConfigureMockMvc
class RequestValidationTests {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private JwtTokenService jwtTokenService;

    private MockHttpServletRequestBuilder as(MockHttpServletRequestBuilder request, long userId, Role role) {
        User user = new User(userId, "user" + userId + "@test.com", null, role, role == Role.EMPLEADO ? 1L : null);
        return request.header("Authorization", "Bearer " + jwtTokenService.generateToken(user))
                .contentType(MediaType.APPLICATION_JSON);
    }

    @Test
    void restaurantWithSeveralInvalidFieldsAnswersTheFirstOneAndListsThemAll() throws Exception {
        mockMvc.perform(as(post("/api/restaurants"), 1L, Role.ADMIN)
                        .content("{\"name\":\"12345\",\"nit\":\"90A\",\"address\":\"Calle 1\",\"phone\":\"+573001234567\","
                                + "\"logoUrl\":\"https://logo.png\",\"ownerUserId\":5}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(MensajesError.NOMBRE_DEL_RESTAURANTE_NO_PUEDE_TENER_SOLO))
                .andExpect(jsonPath("$.details.name").value(MensajesError.NOMBRE_DEL_RESTAURANTE_NO_PUEDE_TENER_SOLO))
                .andExpect(jsonPath("$.details.nit").value(MensajesError.NIT_SOLO_PUEDE_TENER_DIGITOS));
    }

    @Test
    void restaurantWithAPhoneLongerThan13CharactersIsRejected() throws Exception {
        mockMvc.perform(as(post("/api/restaurants"), 1L, Role.ADMIN)
                        .content("{\"name\":\"Sopas\",\"nit\":\"900111222\",\"address\":\"Calle 1\",\"phone\":\"+5730012345678\","
                                + "\"logoUrl\":\"https://logo.png\",\"ownerUserId\":5}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(MensajesError.TELEFONO_DEL_RESTAURANTE_DEBE_TENER_MAXIMO_13));
    }

    @Test
    void dishWithoutNameAndWithNegativePriceIsRejected() throws Exception {
        mockMvc.perform(as(post("/api/dishes"), 55L, Role.PROPIETARIO)
                        .content("{\"name\":\" \",\"price\":-5,\"description\":\"d\",\"imageUrl\":\"i\","
                                + "\"category\":\"c\",\"restaurantId\":1}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(MensajesError.NOMBRE_DEL_PLATO_ES_OBLIGATORIO))
                .andExpect(jsonPath("$.details.price").value(MensajesError.PRECIO_DEBE_SER_UN_NUMERO_ENTERO_MAYOR));
    }

    @Test
    void dishUpdateWithoutDescriptionIsRejected() throws Exception {
        mockMvc.perform(as(put("/api/dishes/1"), 55L, Role.PROPIETARIO).content("{\"price\":15000}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(MensajesError.DESCRIPCION_ES_OBLIGATORIA));
    }

    @Test
    void availabilityWithoutTheActiveFieldIsRejectedInsteadOfDisablingTheDish() throws Exception {
        mockMvc.perform(as(patch("/api/dishes/1/availability"), 55L, Role.PROPIETARIO).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(MensajesError.DISPONIBILIDAD_ES_OBLIGATORIA));
    }

    @Test
    void orderWithoutDishesIsRejected() throws Exception {
        mockMvc.perform(as(post("/api/orders"), 7L, Role.CLIENTE).content("{\"restaurantId\":1,\"dishes\":[]}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(MensajesError.PEDIDO_DEBE_TENER_AL_MENOS_UN_PLATO));
    }

    @Test
    void orderWithAZeroQuantityPointsToThatDish() throws Exception {
        mockMvc.perform(as(post("/api/orders"), 7L, Role.CLIENTE)
                        .content("{\"restaurantId\":1,\"dishes\":[{\"dishId\":3,\"quantity\":2},{\"dishId\":4,\"quantity\":0}]}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(MensajesError.CANTIDAD_DEBE_SER_UN_NUMERO_ENTERO_MAYOR))
                .andExpect(jsonPath("$.details['dishes[1].quantity']")
                        .value(MensajesError.CANTIDAD_DEBE_SER_UN_NUMERO_ENTERO_MAYOR));
    }

    @Test
    void deliveryWithoutPinIsRejected() throws Exception {
        mockMvc.perform(as(patch("/api/orders/1/deliver"), 8L, Role.EMPLEADO).content("{\"pin\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(MensajesError.PIN_DE_SEGURIDAD_ES_OBLIGATORIO));
    }

    @Test
    void errorsThatAreNotAboutFieldsDoNotIncludeDetails() throws Exception {
        mockMvc.perform(as(patch("/api/orders/999999/cancel"), 7L, Role.CLIENTE))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(MensajesError.PEDIDO_NO_EXISTE))
                .andExpect(jsonPath("$.details").doesNotExist());
    }
}
