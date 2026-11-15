package com.plazoleta.infrastructure.input.rest.exceptionhandler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.HttpRequestMethodNotSupportedException;

import com.plazoleta.domain.util.MensajesError;

/** Un error no previsto responde 500 con el formato del proyecto, sin detalles internos. */
class UnexpectedErrorHandlerTest {
    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();
    private final MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/algo");

    @Test
    void anUnexpectedErrorAnswers500WithoutInternalDetails() throws Exception {
        var response = handler.handleUnexpected(new RuntimeException("NullPointer en la línea 42 con SQL interno"), request);

        assertThat(response.getStatusCode().value()).isEqualTo(500);
        assertThat(response.getBody().getMessage()).isEqualTo(MensajesError.ERROR_INTERNO_DEL_SERVIDOR);
        assertThat(response.getBody().getMessage()).doesNotContain("SQL").doesNotContain("42");
    }

    @Test
    void aMethodThatIsNotAllowedKeepsItsOwnCode() throws Exception {
        var response = handler.handleUnexpected(new HttpRequestMethodNotSupportedException("PATCH"), request);

        assertThat(response.getStatusCode().value()).isEqualTo(405);
        assertThat(response.getBody().getMessage()).isEqualTo(MensajesError.PETICION_NO_SOPORTADA);
    }

    @Test
    void anAccessDeniedIsLeftToSpringSecurity() {
        assertThatThrownBy(() -> handler.handleUnexpected(new org.springframework.security.access.AccessDeniedException("x"), request))
                .isInstanceOf(org.springframework.security.access.AccessDeniedException.class);
    }
}
