package com.xendit.Models;

import com.google.gson.annotations.SerializedName;

public class CardMetadata {
    @SerializedName("bank")
    private String bank;

    @SerializedName("country_code")
    private String country_code;

    @SerializedName("type")
    private String type;

    @SerializedName("brand")
    private String brand;

    @SerializedName("card_art_url")
    private String card_art_url;

    public String getBank() {
        return bank;
    }

    public String getCountry_code() {
        return country_code;
    }

    public String getType() {
        return type;
    }

    public String getBrand() {
        return brand;
    }

    public String getCard_art_url() {
        return card_art_url;
    }
}