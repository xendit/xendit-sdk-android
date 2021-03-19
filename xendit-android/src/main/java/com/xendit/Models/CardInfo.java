package com.xendit.Models;

import com.google.gson.annotations.SerializedName;

public class CardInfo {
    @SerializedName("bank")
    private String bank;

    @SerializedName("country")
    private String country;

    @SerializedName("type")
    private String type;

    @SerializedName("brand")
    private String brand;

    @SerializedName("card_art_url")
    private String card_art_url;

    @SerializedName("fingerprint")
    private String fingerprint;

    public String getBank() {
        return bank;
    }

    public String getCountry() {
        return country;
    }

    public String getType() {
        return type;
    }

    public String getBrand() {
        return brand;
    }

    public String getCardArtUrl() {
        return card_art_url;
    }

    public String getFingerprint() { return fingerprint; }
}
