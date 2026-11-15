package com.plazoleta.domain.util;

/**
 * Mensajes de error del microservicio, en un solo lugar. Se usan en el dominio, la aplicación y la
 * infraestructura (validaciones, excepciones y manejador de errores). Clase de Java puro, sin Spring.
 */
public final class MensajesError {
    public static final String CADA_PLATO_DEL_PEDIDO_NECESITA_UN_DISHID = "Cada plato del pedido necesita un dishId";
    public static final String CADA_PLATO_PUEDE_APARECER_UNA_SOLA_VEZ = "Cada plato puede aparecer una sola vez; usa el campo quantity para pedir más unidades";
    public static final String CANTIDAD_DEBE_SER_UN_NUMERO_ENTERO_MAYOR = "La cantidad debe ser un número entero mayor que 0";
    public static final String CATEGORIA_ES_OBLIGATORIA = "La categoría es obligatoria";
    public static final String CLIENTE_ES_OBLIGATORIO = "El cliente es obligatorio";
    public static final String CLIENTE_NO_TIENE_UN_TELEFONO_PARA_NOTIFICARLE = "El cliente no tiene un teléfono para notificarle";
    public static final String CONFIGURA_USERS_INTERNAL_TOKEN_PARA_PROTEGER_LAS = "Configura USERS_INTERNAL_TOKEN para proteger las rutas internas";
    public static final String CUENTA_DEL_CLIENTE_NO_EXISTE = "La cuenta del cliente no existe";
    public static final String CUERPO_DE_LA_PETICION_ES_OBLIGATORIO = "El cuerpo de la petición es obligatorio";
    public static final String DESCRIPCION_ES_OBLIGATORIA = "La descripción es obligatoria";
    public static final String DIRECCION_ES_OBLIGATORIA = "La dirección es obligatoria";
    public static final String EMPLEADO_ES_OBLIGATORIO = "El empleado es obligatorio";
    public static final String EMPLEADO_NO_ESTA_VINCULADO_A_UN_RESTAURANTE = "El empleado no está vinculado a un restaurante";
    public static final String ERROR_INTERNO_DEL_SERVIDOR = "Error interno del servidor";
    public static final String ESTADO_DESCONOCIDO_USA_PENDIENTE_EN_PREPARACION_LISTO = "Estado desconocido. Usa PENDIENTE, EN_PREPARACION, LISTO, ENTREGADO o CANCELADO";
    public static final String ESTADO_ES_OBLIGATORIO = "El estado es obligatorio";
    public static final String ID_DEL_RESTAURANTE_ES_OBLIGATORIO = "El id del restaurante es obligatorio";
    public static final String PEDIDO_NO_CANCELABLE = "Lo sentimos, tu pedido ya está en preparación y no puede cancelarse";
    public static final String NIT_SOLO_PUEDE_TENER_DIGITOS = "El NIT solo puede tener dígitos";
    public static final String NO_HAY_UN_USUARIO_AUTENTICADO = "No hay un usuario autenticado";
    public static final String NO_PUEDES_MODIFICAR_PLATOS_DE_OTRO_RESTAURANTE = "No puedes modificar platos de otro restaurante";
    public static final String NO_TIENES_PERMISOS_PARA_ESTA_OPERACION = "No tienes permisos para esta operación";
    public static final String NOMBRE_DEL_PLATO_ES_OBLIGATORIO = "El nombre del plato es obligatorio";
    public static final String NOMBRE_DEL_RESTAURANTE_ES_OBLIGATORIO = "El nombre del restaurante es obligatorio";
    public static final String NOMBRE_DEL_RESTAURANTE_NO_PUEDE_TENER_SOLO = "El nombre del restaurante no puede tener solo números";
    public static final String PAGINA_DEBE_SER_0_O_MAYOR_Y = "La página debe ser 0 o mayor y el tamaño debe estar entre 1 y 100";
    public static final String PEDIDO_ACABA_DE_CAMBIAR_SOLO_SE_ENTREGAN = "El pedido acaba de cambiar; solo se entregan pedidos en estado LISTO";
    public static final String PEDIDO_ACABA_DE_CAMBIAR_SOLO_SE_MARCAN = "El pedido acaba de cambiar; solo se marcan como listos los pedidos EN_PREPARACION";
    public static final String PEDIDO_ACABA_DE_SER_TOMADO_O_CANCELADO = "El pedido acaba de ser tomado o cancelado; solo se pueden asignar pedidos PENDIENTE";
    public static final String PEDIDO_DEBE_TENER_AL_MENOS_UN_PLATO = "El pedido debe tener al menos un plato";
    public static final String PEDIDO_ESTA_ASIGNADO_A_OTRO_EMPLEADO = "El pedido está asignado a otro empleado";
    public static final String PEDIDO_NO_EXISTE = "El pedido no existe";
    public static final String PEDIDO_PERTENECE_A_OTRO_CLIENTE = "El pedido pertenece a otro cliente";
    public static final String PEDIDO_PERTENECE_A_OTRO_RESTAURANTE = "El pedido pertenece a otro restaurante";
    public static final String PETICION_ESTA_MAL_FORMADA_O_LE_FALTAN = "La petición está mal formada o le faltan parámetros";
    public static final String PIN_DE_SEGURIDAD_ES_OBLIGATORIO = "El PIN de seguridad es obligatorio";
    public static final String PIN_DE_SEGURIDAD_NO_ES_CORRECTO = "El PIN de seguridad no es correcto";
    public static final String PLATO_NO_DISPONIBLE = "El plato %d no está disponible";
    public static final String PLATO_NO_EXISTE = "El plato no existe";
    public static final String PLATO_CON_ID_NO_EXISTE = "El plato %d no existe";
    public static final String PRECIO_DEBE_SER_MAYOR_QUE_0 = "El precio debe ser mayor que 0";
    public static final String PRECIO_DEBE_SER_UN_NUMERO_ENTERO_MAYOR = "El precio debe ser un número entero mayor que 0";
    public static final String PROPIETARIO_DEL_RESTAURANTE_ES_OBLIGATORIO = "El propietario del restaurante es obligatorio";
    public static final String PROPIETARIO_NO_ES_DUENO_DE_ESTE_RESTAURANTE = "El propietario no es dueño de este restaurante";
    public static final String REQUIERE_AUTENTICACION = "Se requiere autenticación";
    public static final String RESTAURANTE_DEL_PLATO_NO_EXISTE = "El restaurante del plato no existe";
    public static final String RESTAURANTE_ES_OBLIGATORIO = "El restaurante es obligatorio";
    public static final String RESTAURANTE_NO_EXISTE = "El restaurante no existe";
    public static final String ROL_INSUFICIENTE = "Rol insuficiente";
    public static final String SAVE_SOLO_CREA_PEDIDOS_NUEVOS_PARA_CAMBIAR = "save solo crea pedidos nuevos; para cambiar un pedido usa updateIfStatus";
    public static final String SERVICIO_DE_MENSAJERIA_NO_ESTA_DISPONIBLE = "El servicio de mensajería no está disponible";
    public static final String SERVICIO_DE_MENSAJERIA_RECHAZO_EL_SMS_HTTP = "El servicio de mensajería rechazó el SMS (HTTP ";
    public static final String SERVICIO_DE_USUARIOS_NO_ESTA_DISPONIBLE = "El servicio de usuarios no está disponible";
    public static final String SERVICIO_DE_USUARIOS_RECHAZO_LA_CONSULTA_HTTP = "El servicio de usuarios rechazó la consulta (HTTP ";
    public static final String SOLO_SE_PUEDEN_ASIGNAR_PEDIDOS_PENDIENTE_ESTE = "Solo se pueden asignar pedidos PENDIENTE; este pedido está ";
    public static final String SOLO_SE_PUEDEN_ENTREGAR_PEDIDOS_LISTO_ESTE = "Solo se pueden entregar pedidos LISTO; este pedido está ";
    public static final String SOLO_SE_PUEDEN_MARCAR_COMO_LISTOS_LOS = "Solo se pueden marcar como listos los pedidos EN_PREPARACION; este pedido está ";
    public static final String SOLO_UN_CLIENTE_PUEDE_HACER_PEDIDOS = "Solo un cliente puede hacer pedidos";
    public static final String SOLO_UN_PEDIDO_LISTO_PUEDE_VOLVER_A = "Solo un pedido LISTO puede volver a EN_PREPARACION; este pedido está ";
    public static final String SOLO_UN_PROPIETARIO_PUEDE_CREAR_PLATOS = "Solo un propietario puede crear platos";
    public static final String SOLO_UN_PROPIETARIO_PUEDE_HABILITAR_O_DESHABILITAR = "Solo un propietario puede habilitar o deshabilitar platos";
    public static final String SOLO_UN_PROPIETARIO_PUEDE_MODIFICAR_PLATOS = "Solo un propietario puede modificar platos";
    public static final String TELEFONO_DEL_RESTAURANTE_DEBE_TENER_MAXIMO_13 = "El teléfono del restaurante debe tener máximo 13 caracteres, solo dígitos y opcionalmente un + al inicio";
    public static final String TODOS_LOS_PLATOS_DEBEN_SER_DEL_RESTAURANTE = "Todos los platos deben ser del restaurante seleccionado";
    public static final String URL_DE_LA_IMAGEN_ES_OBLIGATORIA = "La URL de la imagen es obligatoria";
    public static final String URL_DEL_LOGO_ES_OBLIGATORIA = "La URL del logo es obligatoria";
    public static final String USUARIO_AUTENTICADO_NO_EXISTE = "El usuario autenticado no existe";
    public static final String USUARIO_DEBE_TENER_EL_ROL_PROPIETARIO = "El usuario debe tener el rol PROPIETARIO";
    public static final String USUARIO_PROPIETARIO_NO_EXISTE = "El usuario propietario no existe";
    public static final String YA_TIENES_UN_PEDIDO_EN_PROCESO_PENDIENTE = "Ya tienes un pedido en proceso (pendiente, en preparación o listo)";
    public static final String RUTA_NO_EXISTE = "La ruta solicitada no existe";
    public static final String PETICION_NO_SOPORTADA = "La petición no se puede procesar con ese método o formato";

    private MensajesError() {
    }
}
