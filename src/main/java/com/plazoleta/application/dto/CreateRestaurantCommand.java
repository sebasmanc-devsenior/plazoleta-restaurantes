package com.plazoleta.application.dto;

public class CreateRestaurantCommand {
    private String name;
    private String nit;
    private String address;
    private String phone;
    private String logoUrl;
    private Long ownerUserId;

    public CreateRestaurantCommand() {
    }

    public CreateRestaurantCommand(String name, String nit, String address, String phone, String logoUrl, Long ownerUserId) {
        this.name = name;
        this.nit = nit;
        this.address = address;
        this.phone = phone;
        this.logoUrl = logoUrl;
        this.ownerUserId = ownerUserId;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getNit() { return nit; }
    public void setNit(String nit) { this.nit = nit; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getLogoUrl() { return logoUrl; }
    public void setLogoUrl(String logoUrl) { this.logoUrl = logoUrl; }
    public Long getOwnerUserId() { return ownerUserId; }
    public void setOwnerUserId(Long ownerUserId) { this.ownerUserId = ownerUserId; }
}
