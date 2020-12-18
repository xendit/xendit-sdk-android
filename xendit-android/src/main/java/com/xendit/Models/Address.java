package com.xendit.Models;

import com.google.gson.annotations.SerializedName;

public class Address {
    @SerializedName("country")
    private String country;

    @SerializedName("street_line1")
    private String streetLine1;

    @SerializedName("street_line2")
    private String streetLine2;

    @SerializedName("city")
    private String city;

    @SerializedName("province_state")
    private String provinceState;

    @SerializedName("postal_code")
    private String postalCode;

    @SerializedName("category")
    private String category;

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getStreetLine1() {
        return streetLine1;
    }

    public void setStreetLine1(String streetLine1) {
        this.streetLine1 = streetLine1;
    }

    public String getStreetLine2() {
        return streetLine2;
    }

    public void setStreetLine2(String streetLine2) {
        this.streetLine2 = streetLine2;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getProvinceState() {
        return provinceState;
    }

    public void setProvinceState(String provinceState) {
        this.provinceState = provinceState;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}
