package com.plazoleta.application.port.output;

/** HU-14: genera el PIN de seguridad con el que el cliente reclama su pedido. */
public interface PinGeneratorPort {
    String generate();
}
