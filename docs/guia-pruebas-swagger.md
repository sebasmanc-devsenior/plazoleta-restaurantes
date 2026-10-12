# Guía para probar las HU 1 a 9 en Swagger

Recorrido completo, en orden, con los datos exactos que hay que pegar y la respuesta esperada.
Todos los pasos fueron verificados de punta a punta con los dos servicios encendidos.

---

## 0. Antes de empezar

Se necesitan los **dos repositorios**, cada uno en su propia carpeta:
[plazoleta-usuarios](https://github.com/sebasmanc-devsenior/plazoleta-usuarios) (puerto 8081) y
[plazoleta-restaurantes](https://github.com/sebasmanc-devsenior/plazoleta-restaurantes) (puerto 8080).

1. **Bases de datos:** en MySQL Workbench (usuario root) ejecuta `db/mysql-init.sql` de **cada**
   repositorio. El de usuarios crea también el administrador inicial.
2. **Archivo `.env`** en la raíz de **cada** repositorio (copia de su `.env.example`):

```powershell
Copy-Item .env.example .env
```

3. **Levantar los dos servicios**, cada uno en su propia terminal de PowerShell, dentro de la
   carpeta de su repositorio:

```powershell
.\mvnw.cmd spring-boot:run
```

Espera a ver `Started UsersServiceApplication` (usuarios) y `Started PlazoletaApplication` (restaurantes).

4. **Abrir los dos Swagger** en el navegador, cada uno en una pestaña:

| Servicio | Dirección | Historias |
| --- | --- | --- |
| Usuarios | http://localhost:8081/swagger-ui.html | 1, 5, 6, 8 |
| Restaurantes | http://localhost:8080/swagger-ui.html | 2, 3, 4, 7, 9 |

### Cómo usar Swagger (sirve para todos los pasos)

- Haz clic en el endpoint para desplegarlo, luego **Try it out**, pega el JSON en el cuadro
  *Request body* y pulsa **Execute**. La respuesta aparece abajo, en *Server response*.
- **Para enviar el token:** arriba a la derecha pulsa **Authorize**, pega **solo el token**
  (sin la palabra `Bearer`), pulsa **Authorize** y luego **Close**.
- El token del login se pega en **cualquiera de los dos Swagger**: ambos servicios lo aceptan.
- Para cambiar de usuario: **Authorize → Logout** y pega el token nuevo.
- Los tokens duran **1 hora**. Si de pronto todo da `401`, vuelve a hacer login.

---

## Paso 1 · HU-5 Login del administrador · Swagger **Usuarios**

`POST /api/login`

```json
{
  "email": "admin@plazoleta.com",
  "password": "AdminSecurePass123!"
}
```

**Esperado: `200`** con `"role": "ADMIN"`. Copia el valor de `token`.

➡️ **Authorize** con el token del admin en el Swagger de Usuarios **y** en el de Restaurantes.

---

## Paso 2 · HU-1 Crear propietario · Swagger **Usuarios** (token admin)

`POST /api/owners`

```json
{
  "name": "Carlos",
  "lastName": "Gomez",
  "identityDocument": "1020304050",
  "phone": "+573005698325",
  "birthDate": "1990-05-14",
  "email": "carlos.propietario@plazoleta.com",
  "password": "Propietario123"
}
```

**Esperado: `201`** con `"role": "PROPIETARIO"`. **Anota el `id`** (lo usarás como `ownerUserId`).

---

## Paso 3 · HU-2 Crear restaurante · Swagger **Restaurantes** (token admin)

`POST /api/restaurants` — cambia `ownerUserId` por el `id` del paso 2.

```json
{
  "name": "Sabor Paisa",
  "nit": "900123456",
  "address": "Calle 10 # 20-30",
  "phone": "+573001112233",
  "logoUrl": "https://ejemplo.com/logo-sabor-paisa.png",
  "ownerUserId": 2
}
```

**Esperado: `201`**. **Anota el `id` del restaurante.**

Crea un segundo restaurante para ver luego el orden alfabético (HU-9):

```json
{
  "name": "Arepas La 70",
  "nit": "900654321",
  "address": "Carrera 70",
  "phone": "3009998877",
  "logoUrl": "https://ejemplo.com/arepas.png",
  "ownerUserId": 2
}
```

---

## Paso 4 · HU-5 Login del propietario · Swagger **Usuarios**

`POST /api/login`

```json
{
  "email": "carlos.propietario@plazoleta.com",
  "password": "Propietario123"
}
```

**Esperado: `200`** con `"role": "PROPIETARIO"`.

➡️ **Authorize → Logout** y pega el token del **propietario** en ambos Swagger.

---

## Paso 5 · HU-3 Crear plato · Swagger **Restaurantes** (token propietario)

`POST /api/dishes` — `restaurantId` = el `id` del paso 3.

```json
{
  "name": "Bandeja paisa",
  "price": 32000,
  "description": "Frijoles, arroz, chicharron y huevo",
  "imageUrl": "https://ejemplo.com/bandeja.png",
  "category": "Almuerzos",
  "restaurantId": 1
}
```

**Esperado: `201`** con `"active": true`. **Anota el `id` del plato.**

---

## Paso 6 · HU-4 Modificar plato · Swagger **Restaurantes** (token propietario)

`PUT /api/dishes/{dishId}` — en `dishId` escribe el `id` del paso 5.

```json
{
  "price": 35000,
  "description": "Con aguacate y arepa"
}
```

**Esperado: `200`**. Cambian precio y descripción; nombre, imagen y categoría siguen iguales.

---

## Paso 7 · HU-7 Deshabilitar / habilitar plato · Swagger **Restaurantes** (token propietario)

`PATCH /api/dishes/{dishId}/availability`

```json
{ "active": false }
```

**Esperado: `200`** con `"active": false`. Repite con `{ "active": true }` para volver a activarlo.

---

## Paso 8 · HU-6 Crear empleado · Swagger **Usuarios** (token propietario)

`POST /api/employees` — `restaurantId` = el `id` del paso 3.

```json
{
  "name": "Ana",
  "lastName": "Perez",
  "identityDocument": "1122334455",
  "phone": "+573104445566",
  "email": "ana.empleada@plazoleta.com",
  "password": "Empleado123",
  "restaurantId": 1
}
```

**Esperado: `201`** con `"role": "EMPLEADO"` y el `restaurantId`.
Por dentro, Usuarios le preguntó a Restaurantes quién es el dueño del restaurante.

---

## Paso 9 · HU-8 Crear cuenta cliente · Swagger **Usuarios** (sin token)

`POST /api/clients` — es público, no necesita Authorize.

```json
{
  "name": "Laura",
  "lastName": "Diaz",
  "identityDocument": "5566778899",
  "phone": "+573201112233",
  "email": "laura.cliente@plazoleta.com",
  "password": "Cliente123"
}
```

**Esperado: `201`** con `"role": "CLIENTE"`.

Luego haz login con ella (`POST /api/login`, correo `laura.cliente@plazoleta.com`,
clave `Cliente123`) y pon **su token** en el Swagger de Restaurantes.

---

## Paso 10 · HU-9 Listar restaurantes · Swagger **Restaurantes** (token cliente)

`GET /api/restaurants` con `page = 0` y `size = 10`.

**Esperado: `200`**, en orden alfabético y solo con nombre y logo:

```json
{
  "content": [
    { "name": "Arepas La 70", "logoUrl": "https://ejemplo.com/arepas.png" },
    { "name": "Sabor Paisa", "logoUrl": "https://ejemplo.com/logo-sabor-paisa.png" }
  ],
  "page": 0,
  "size": 10,
  "totalElements": 2,
  "totalPages": 1
}
```

Prueba también `size = 1`: verás un restaurante por página y `totalPages: 2`.

---

## Pruebas de validación (lo que debe fallar)

Son las que más preguntan en la evaluación: demuestran que se cumplen los criterios de aceptación.

| HU | Qué probar | Cómo | Esperado |
| --- | --- | --- | --- |
| 5 | Clave incorrecta | Login del admin con `"password": "claveIncorrecta"` | `401` |
| 1 | Menor de edad | Paso 2 con `"birthDate": "2012-01-01"` y otro correo/documento | `400` "at least 18 years old" |
| 1 | Teléfono de más de 13 caracteres | Paso 2 con `"phone": "+57300569832599"` | `400` |
| 1 | Documento con letras | Paso 2 con `"identityDocument": "ABC123"` | `400` |
| 1 | Correo inválido | Paso 2 con `"email": "carlos-sin-arroba"` | `400` |
| 1 | Correo repetido | Repetir el paso 2 tal cual | `409` |
| 2 | Nombre solo con números | Paso 3 con `"name": "12345"` | `400` |
| 2 | NIT con letras | Paso 3 con `"nit": "900ABC"` | `400` |
| 2 | Dueño que no es propietario | Paso 3 con `"ownerUserId": 1` (el admin) | `400` |
| 3 | Precio 0 o negativo | Paso 5 con `"price": 0` | `400` |
| 5 | Propietario crea restaurante | Paso 3 con el token del propietario | `403` |
| 5 | Cliente crea plato | Paso 5 con el token del cliente | `403` |
| 5 | Sin token | Cualquier endpoint protegido tras **Logout** | `401` |
| 6 | Empleado en restaurante ajeno | Paso 8 con `"restaurantId": 999` | `404` (o `403` si existe y es de otro) |
| 9 | Listar sin ser cliente | Paso 10 con el token del propietario | `403` |

---

## Si algo sale mal

| Ves esto | Significa | Qué hacer |
| --- | --- | --- |
| `401` en todo | Falta el token o venció (dura 1 hora) | Login otra vez y **Authorize** |
| `403` | El token es de un rol que no tiene permiso | Revisa con qué usuario hiciste Authorize |
| `409` al crear | Ese correo o documento ya existe (por ejemplo, si repites la guía) | Cambia correo y documento, o reinicia la base |
| `503` | El otro servicio está apagado | Verifica que las dos terminales estén corriendo |
| La app no arranca | Falta el `.env` o MySQL está apagado | Revisa el paso 0 |

**Reiniciar la base para repetir la guía desde cero:** en MySQL Workbench, descomenta la
línea `DROP DATABASE` al inicio de `db/mysql-init.sql` de cada repositorio y ejecútalo completo. Vuelve a comentarla
después.
