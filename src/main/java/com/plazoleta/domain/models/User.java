package com.plazoleta.domain.models;

import java.util.Objects;

/**
 * Referencia de solo lectura a un usuario cuyo registro vive en users-service.
 * Este servicio no crea ni valida cuentas: solo necesita el id y el rol de quien opera
 * (por ejemplo, para comprobar que el dueño de un restaurante tiene rol PROPIETARIO).
 */
public class User {
    private Long id;
    private String email;
    private String phone;
    private Role role;
    private Long restaurantId;

    public User() {
    }

    public User(Long id, String email, String phone, Role role, Long restaurantId) {
        this.id = id;
        this.email = email;
        this.phone = phone;
        this.role = role;
        this.restaurantId = restaurantId;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
    public Long getRestaurantId() { return restaurantId; }
    public void setRestaurantId(Long restaurantId) { this.restaurantId = restaurantId; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
