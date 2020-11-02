package com.xendit.Models;

import com.google.gson.annotations.SerializedName;

public class Address {
    @SerializedName("country")
    private String country;

    @SerializedName("street_line_1")
    private String streetLine1;

    @SerializedName("street_line_2")
    private String streetLine2;

    @SerializedName("city")
    private String city;

    @SerializedName("province")
    private String province;

    @SerializedName("state")
    private String state;

    @SerializedName("postal_code")
    private String postalCode;

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

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }
}
