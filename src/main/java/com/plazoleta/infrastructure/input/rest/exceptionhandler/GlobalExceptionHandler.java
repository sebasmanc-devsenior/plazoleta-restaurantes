package com.plazoleta.infrastructure.input.rest.exceptionhandler;

import com.plazoleta.domain.util.MensajesError;

import java.lang.reflect.Field;
import java.lang.reflect.RecordComponent;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.plazoleta.domain.exception.ActiveOrderExistsException;
import com.plazoleta.domain.exception.DishNotFoundException;
import com.plazoleta.domain.exception.ForbiddenOperationException;
import com.plazoleta.domain.exception.IntegrationUnavailableException;
import com.plazoleta.domain.exception.InvalidOrderStatusException;
import com.plazoleta.domain.exception.InvalidPriceException;
import com.plazoleta.domain.exception.InvalidSecurityPinException;
import com.plazoleta.domain.exception.MissingAuthenticatedUserException;
import com.plazoleta.domain.exception.OrderNotFoundException;
import com.plazoleta.domain.exception.RestaurantNotFoundException;
import com.plazoleta.domain.exception.UserNotFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler({InvalidPriceException.class, IllegalArgumentException.class, InvalidSecurityPinException.class})
    public ResponseEntity<ErrorResponse> handleBadRequest(Exception ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request.getRequestURI());
    }

    /**
     * Validaciones Jakarta de los request DTO: 400 con el mensaje del primer campo inválido (en el orden en que
     * el request declara sus campos, el mismo orden en que valida el dominio) y en "details" todos los campos.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleInvalidRequest(MethodArgumentNotValidException ex,
                                                              HttpServletRequest request) {
        List<String> order = declaredFields(ex.getParameter().getParameterType());
        List<FieldError> errors = new ArrayList<>(ex.getBindingResult().getFieldErrors());
        errors.sort(Comparator.comparingInt((FieldError error) -> position(order, error.getField()))
                .thenComparing(FieldError::getField));
        Map<String, String> details = new LinkedHashMap<>();
        errors.forEach(error -> details.putIfAbsent(error.getField(), error.getDefaultMessage()));
        String message = errors.isEmpty() ? MensajesError.PETICION_TIENE_DATOS_INVALIDOS
                : errors.get(0).getDefaultMessage();
        ResponseEntity<ErrorResponse> response = buildResponse(HttpStatus.BAD_REQUEST, message, request.getRequestURI());
        response.getBody().setDetails(details);
        return response;
    }

    @ExceptionHandler({
        HttpMessageNotReadableException.class,
        MissingServletRequestParameterException.class,
        MethodArgumentTypeMismatchException.class
    })
    public ResponseEntity<ErrorResponse> handleMalformedRequest(Exception ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.BAD_REQUEST, MensajesError.PETICION_ESTA_MAL_FORMADA_O_LE_FALTAN,
                request.getRequestURI());
    }

    @ExceptionHandler({UserNotFoundException.class, RestaurantNotFoundException.class, DishNotFoundException.class,
            OrderNotFoundException.class})
    public ResponseEntity<ErrorResponse> handleNotFound(Exception ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler({ActiveOrderExistsException.class, InvalidOrderStatusException.class})
    public ResponseEntity<ErrorResponse> handleConflict(Exception ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(ForbiddenOperationException.class)
    public ResponseEntity<ErrorResponse> handleForbidden(ForbiddenOperationException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.FORBIDDEN, ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(MissingAuthenticatedUserException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorized(Exception ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.UNAUTHORIZED, ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(IntegrationUnavailableException.class)
    public ResponseEntity<ErrorResponse> handleUnavailableIntegration(IntegrationUnavailableException ex,
                                                                      HttpServletRequest request) {
        log.warn("Integration unavailable at {}: {}", request.getRequestURI(), ex.getMessage(), ex.getCause());
        return buildResponse(HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage(), request.getRequestURI());
    }

    /** Error no previsto: 500 con el formato del proyecto y sin detalles internos (el detalle queda en el log). */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex, HttpServletRequest request) throws Exception {
        if (ex instanceof org.springframework.security.access.AccessDeniedException
                || ex instanceof org.springframework.security.core.AuthenticationException) {
            throw ex; // los resuelve Spring Security: 403 o 401
        }
        if (ex instanceof org.springframework.web.ErrorResponse frameworkError) {
            HttpStatus status = HttpStatus.valueOf(frameworkError.getStatusCode().value());
            return buildResponse(status, status == HttpStatus.NOT_FOUND ? MensajesError.RUTA_NO_EXISTE
                    : MensajesError.PETICION_NO_SOPORTADA, request.getRequestURI());
        }
        log.error("Unexpected error at {}", request.getRequestURI(), ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, MensajesError.ERROR_INTERNO_DEL_SERVIDOR, request.getRequestURI());
    }

    private static List<String> declaredFields(Class<?> type) {
        if (type.isRecord()) {
            return Arrays.stream(type.getRecordComponents()).map(RecordComponent::getName).toList();
        }
        return Arrays.stream(type.getDeclaredFields()).map(Field::getName).toList();
    }

    /** Posición del campo en el request; "dishes[0].quantity" cuenta como "dishes". */
    private static int position(List<String> order, String field) {
        int index = order.indexOf(field.split("[.\\[]", 2)[0]);
        return index < 0 ? Integer.MAX_VALUE : index;
    }

    private ResponseEntity<ErrorResponse> buildResponse(HttpStatus status, String message, String path) {
        ErrorResponse response = new ErrorResponse(Instant.now(), status.value(), status.getReasonPhrase(), message, path);
        return ResponseEntity.status(status).body(response);
    }
}
