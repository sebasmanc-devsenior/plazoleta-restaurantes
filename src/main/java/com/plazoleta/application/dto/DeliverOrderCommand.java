package com.plazoleta.application.dto;

/** HU-15: PIN de seguridad que el cliente le dicta al empleado para reclamar su pedido. */
public class DeliverOrderCommand {
    private String pin;

    public DeliverOrderCommand() {}

    public DeliverOrderCommand(String pin) {
        this.pin = pin;
    }

    public String getPin() { return pin; }
    public void setPin(String pin) { this.pin = pin; }
}
