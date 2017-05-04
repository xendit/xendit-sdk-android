package com.xendit.Models;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Sergey on 3/22/17.
 */

public class TokenConfiguration {

    @SerializedName("tokenization_auth_key_id")
    private String tokenizationAuthKeyId;

    @SerializedName("flex_api_key")
    private String flexApiKey;

    @SerializedName("flex_production_url")
    private String flexProductionUrl;

    @SerializedName("flex_development_url")
    private String flexDevelopmentUrl;

    public String getTokenizationAuthKeyId() {
        return tokenizationAuthKeyId;
    }

    public String getFlexApiKey() {
        return flexApiKey;
    }

    public String getFlexProductionUrl() {
        return flexProductionUrl;
    }

    public String getFlexDevelopmentUrl() {
        return flexDevelopmentUrl;
    }
}