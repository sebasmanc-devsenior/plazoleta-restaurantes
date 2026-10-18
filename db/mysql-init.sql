-- =============================================================================
-- PLAZOLETA DE COMIDAS - BASE DE DATOS DEL MICROSERVICIO DE RESTAURANTES (MySQL 8)
-- =============================================================================
-- En MySQL Workbench: conéctate como root, File > Open SQL Script, selecciona este
-- archivo y ejecútalo completo con el rayo (Ctrl+Shift+Enter).
-- Es idempotente: se puede ejecutar varias veces sin borrar datos.
--
-- owner_user_id es el id del propietario en el microservicio de usuarios
-- (otra base de datos), por eso NO tiene llave foránea.
-- =============================================================================

-- (OPCIONAL) Reiniciar desde cero. Descomenta solo si quieres borrar TODO.
-- DROP DATABASE IF EXISTS plazoleta;

CREATE DATABASE IF NOT EXISTS plazoleta
    CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;

USE plazoleta;

-- HU-2 Crear restaurante / HU-9 Listar restaurantes (orden alfabético por name)
CREATE TABLE IF NOT EXISTS restaurants (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    name          VARCHAR(255) NOT NULL,
    nit           VARCHAR(255) NOT NULL,
    address       VARCHAR(255) NOT NULL,
    phone         VARCHAR(13)  NOT NULL,
    logo_url      VARCHAR(255) NOT NULL,
    owner_user_id BIGINT       NOT NULL,
    PRIMARY KEY (id),
    KEY idx_restaurants_owner_user_id (owner_user_id),
    KEY idx_restaurants_nit (nit),
    KEY idx_restaurants_name (name)
) ENGINE = InnoDB;

-- HU-3 Crear plato / HU-4 Modificar plato / HU-7 Habilitar-deshabilitar plato
CREATE TABLE IF NOT EXISTS dishes (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    name          VARCHAR(255) NOT NULL,
    price         INT          NOT NULL,
    description   VARCHAR(255) NOT NULL,
    image_url     VARCHAR(255) NOT NULL,
    category      VARCHAR(255) NOT NULL,
    restaurant_id BIGINT       NOT NULL,
    active        BOOLEAN      NOT NULL DEFAULT TRUE,
    PRIMARY KEY (id),
    CONSTRAINT fk_dishes_restaurant FOREIGN KEY (restaurant_id) REFERENCES restaurants (id),
    CONSTRAINT chk_dishes_price_positive CHECK (price > 0),
    KEY idx_dishes_restaurant_active (restaurant_id, active)
) ENGINE = InnoDB;

-- Usuario de conexión del microservicio (debe coincidir con .env)
CREATE USER IF NOT EXISTS 'plazoleta_user'@'%'         IDENTIFIED BY 'plazoleta_pass';
CREATE USER IF NOT EXISTS 'plazoleta_user'@'localhost' IDENTIFIED BY 'plazoleta_pass';
ALTER USER 'plazoleta_user'@'%'         IDENTIFIED BY 'plazoleta_pass';
ALTER USER 'plazoleta_user'@'localhost' IDENTIFIED BY 'plazoleta_pass';
GRANT ALL PRIVILEGES ON plazoleta.* TO 'plazoleta_user'@'%';
GRANT ALL PRIVILEGES ON plazoleta.* TO 'plazoleta_user'@'localhost';
FLUSH PRIVILEGES;

SELECT table_name AS tabla FROM information_schema.tables WHERE table_schema = 'plazoleta';
