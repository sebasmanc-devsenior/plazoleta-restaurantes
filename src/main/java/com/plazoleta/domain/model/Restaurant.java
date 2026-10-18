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
            throw new IllegalArgumentException("Restaurant name is required");
        }
        if (name.trim().matches("\\d+")) {
            throw new IllegalArgumentException("Restaurant name cannot contain only numbers");
        }
        if (nit == null || !nit.matches("\\d+")) {
            throw new IllegalArgumentException("NIT must contain only digits");
        }
        if (address == null || address.isBlank()) {
            throw new IllegalArgumentException("Address is required");
        }
        if (phone == null || !phone.matches("(?:\\+\\d{7,12}|\\d{7,13})")) {
            throw new IllegalArgumentException("Restaurant phone must be at most 13 characters, digits only, optionally starting with +");
        }
        if (logoUrl == null || logoUrl.isBlank()) {
            throw new IllegalArgumentException("Logo URL is required");
        }
        if (ownerUserId == null) {
            throw new IllegalArgumentException("Restaurant owner is required");
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
