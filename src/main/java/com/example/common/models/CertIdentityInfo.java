package com.example.common.models;

public class CertIdentityInfo {
    private final String commonName;
    private final String organization;
    private final String organizationalUnit;
    private final String city;
    private final String state;
    private final String country;

    public CertIdentityInfo(String commonName, String organization, String organizationalUnit, String city, String state, String country) {
        this.commonName = commonName;
        this.organization = organization;
        this.organizationalUnit = organizationalUnit;
        this.city = city;
        this.state = state;
        this.country = country;
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
}
