package com.example.common.models;

public class CertIdentityInfo {
    private final String commonName;
    private final String organization;
    private final String organizationalUnit;
    private final String street;
    private final String city;
    private final String state;
    private final String country;
    private final String userId;

    public CertIdentityInfo(
            String commonName, String organization, String organizationalUnit,
            String street, String city, String state, String country,
            String userId) {
        this.commonName = commonName;
        this.organization = organization;
        this.organizationalUnit = organizationalUnit;
        this.street = street;
        this.city = city;
        this.state = state;
        this.country = country;
        this.userId = userId;
    }

    public String getCommonName() {
        return commonName;
    }

    public String getOrganization() {
        return organization;
    }

    public String getOrganizationalUnit() {
        return organizationalUnit;
    }

    public String getCity() {
        return city;
    }

    public String getState() {
        return state;
    }

    public String getCountry() {
        return country;
    }

    public String getUserId() {
        return userId;
    }

    public String getStreet() {
        return street;
    }
}
