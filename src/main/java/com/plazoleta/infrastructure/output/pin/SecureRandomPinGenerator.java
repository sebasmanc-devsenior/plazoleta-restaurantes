package com.plazoleta.infrastructure.output.pin;

import java.security.SecureRandom;

import org.springframework.stereotype.Component;

import com.plazoleta.application.port.output.PinGeneratorPort;

/** HU-14: PIN de 6 dígitos al azar (puede empezar por cero). */
@Component
public class SecureRandomPinGenerator implements PinGeneratorPort {
    private final SecureRandom random = new SecureRandom();

    @Override
    public String generate() {
        return String.format("%06d", random.nextInt(1_000_000));
    }
}
