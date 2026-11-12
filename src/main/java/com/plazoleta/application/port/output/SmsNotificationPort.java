package com.plazoleta.application.port.output;

/** HU-14: envío de mensajes SMS. Lo resuelve el microservicio de mensajería. */
public interface SmsNotificationPort {
    void send(String phone, String message);
}
