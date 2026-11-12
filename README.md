# Plazoleta · Microservicio de restaurantes

Backend de la plazoleta de comidas: **restaurantes y platos**. Las cuentas, el login y el
token JWT viven en el microservicio de usuarios
([plazoleta-usuarios](https://github.com/sebasmanc-devsenior/plazoleta-usuarios)).

- Java 25 · Spring Boot 4.1.1 · MySQL 8 · Puerto **8080**
- Arquitectura hexagonal · Pruebas con JUnit 5, Mockito y H2 · Documentación OpenAPI (Swagger)

## Historias de usuario

Cada historia se desarrolló en su propia rama y se unió a `main` en orden.

| HU | Funcionalidad | Rama |
| --- | --- | --- |
| 2 | Crear restaurante (solo ADMIN) | `historia2-crear-restaurante` |
| 3 | Crear plato (solo el dueño del restaurante) | `historia3-crear-plato` |
| 4 | Modificar plato: solo precio y descripción | `historia4-modificar-plato` |
| 6 | Consulta interna del dueño del restaurante (la usa usuarios al crear empleados) | `historia6-dueno-del-restaurante` |
| 7 | Habilitar / deshabilitar plato | `historia7-habilitar-deshabilitar-plato` |
| 9 | Listar restaurantes: orden alfabético, paginado, nombre y logo | `historia9-listar-restaurantes` |
| 10 | Listar platos de un restaurante: paginado y filtro por categoría; los deshabilitados salen como no disponibles | `historia10-listar-platos` |
| 11 | Realizar pedido: platos de un solo restaurante, queda PENDIENTE, un pedido en proceso por cliente; valida la cuenta del cliente en el microservicio de usuarios | `historia11-realizar-pedido` |
| 12 | Listar pedidos por estado: solo los del restaurante del empleado, paginados | `historia12-listar-pedidos-por-estado` |
| 13 | Asignarse a un pedido: queda el empleado asignado y pasa a EN_PREPARACION | `historia13-asignarse-pedido` |
| 14 | Notificar pedido listo: el empleado asignado lo pasa a LISTO y el microservicio de mensajería envía un SMS con el PIN | `historia14-notificar-pedido-listo` |
| 15 | Entregar pedido: el empleado asignado digita el PIN del cliente y pasa de LISTO a ENTREGADO | `historia15-entregar-pedido` |

## Arquitectura hexagonal (arquetipo del curso)

```
src/main/java/com/plazoleta/
├── domain/                     ← núcleo, sin dependencias de frameworks
│   ├── model/                  Restaurant, Dish, User, Role (reglas de validación)
│   └── exception/
├── application/                ← casos de uso y puertos, sin Spring
│   ├── port/
│   │   ├── input/              un caso de uso por historia (CreateRestaurantUseCase…)
│   │   └── output/             RestaurantRepositoryPort, DishRepositoryPort, UserRepositoryPort
│   ├── service/                implementación de cada caso de uso
│   └── dto/                    datos que entran y salen de los casos de uso
└── infrastructure/             ← adaptadores y frameworks
    ├── input/
    │   └── rest/               controladores REST y exceptionhandler/
    ├── output/
    │   ├── persistence/
    │   │   ├── entity/         RestaurantEntity, DishEntity (tablas)
    │   │   ├── repository/     RestaurantJpaRepository, DishJpaRepository (Spring Data JPA)
    │   │   └── adapter/        RestaurantPersistenceAdapter, DishPersistenceAdapter (implementan los puertos)
    │   └── http/               consulta de usuarios al microservicio de usuarios
    └── configuration/          BeanConfiguration (inyección), seguridad, OpenAPI; security/ = filtros JWT
```

## Cómo ejecutar

1. **Base de datos:** ejecuta `db/mysql-init.sql` en MySQL Workbench (usuario root).
2. **Variables de entorno:** copia la plantilla en la raíz del proyecto:
   ```powershell
   Copy-Item .env.example .env
   ```
3. **Levanta el servicio** (el de usuarios también debe estar corriendo en el 8081):
   ```powershell
   .\mvnw.cmd spring-boot:run
   ```
4. **Swagger:** http://localhost:8080/swagger-ui.html — obtén el token con `POST /api/login`
   en el servicio de usuarios y pégalo con el botón **Authorize**.

## Pruebas

```powershell
.\mvnw.cmd test
```

Las pruebas usan H2 en memoria: no necesitan MySQL ni tocan la base real.
