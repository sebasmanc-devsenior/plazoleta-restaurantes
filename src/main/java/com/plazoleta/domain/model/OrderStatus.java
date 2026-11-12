package com.plazoleta.domain.model;

import java.util.EnumSet;
import java.util.Set;

/** Estados de un pedido (HU-11 a HU-16). */
public enum OrderStatus {
    PENDIENTE,
    EN_PREPARACION,
    LISTO,
    ENTREGADO,
    CANCELADO;

    /** HU-11: un cliente no puede pedir de nuevo mientras tenga un pedido en alguno de estos estados. */
    public static final Set<OrderStatus> IN_PROCESS = EnumSet.of(PENDIENTE, EN_PREPARACION, LISTO);
}
