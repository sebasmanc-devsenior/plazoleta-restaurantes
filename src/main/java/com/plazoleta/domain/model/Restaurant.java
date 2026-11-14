package com.plazoleta.domain.model;

import java.util.Objects;

public class Restaurant {
    private Long id;
    private String name;
    private String nit;
    private String address;
    private String phone;
    private String logoUrl;
    private Long ownerUserId;

    public Restaurant() {
    }

    public Restaurant(Long id, String name, String nit, String address, String phone, String logoUrl, Long ownerUserId) {
        this.id = id;
        this.name = name;
        this.nit = nit;
        this.address = address;
        this.phone = phone;
        this.logoUrl = logoUrl;
        this.ownerUserId = ownerUserId;
        validate();
    }

    public void validate() {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("El nombre del restaurante es obligatorio");
        }
        if (name.trim().matches("\\d+")) {
            throw new IllegalArgumentException("El nombre del restaurante no puede tener solo números");
        }
        if (nit == null || !nit.matches("\\d+")) {
            throw new IllegalArgumentException("El NIT solo puede tener dígitos");
        }
        if (address == null || address.isBlank()) {
            throw new IllegalArgumentException("La dirección es obligatoria");
        }
        if (phone == null || !phone.matches("(?:\\+\\d{7,12}|\\d{7,13})")) {
            throw new IllegalArgumentException("El teléfono del restaurante debe tener máximo 13 caracteres, solo dígitos y opcionalmente un + al inicio");
        }
        if (logoUrl == null || logoUrl.isBlank()) {
            throw new IllegalArgumentException("La URL del logo es obligatoria");
        }
        if (ownerUserId == null) {
            throw new IllegalArgumentException("El propietario del restaurante es obligatorio");
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNit() {
        return nit;
    }

    public void setNit(String nit) {
        this.nit = nit;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }

    public Long getOwnerUserId() {
        return ownerUserId;
    }

    public void setOwnerUserId(Long ownerUserId) {
        this.ownerUserId = ownerUserId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Restaurant that = (Restaurant) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
