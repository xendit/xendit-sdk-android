package com.xendit.Models;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Sergey on 3/22/17.
 */

public class TokenCredentials {

    @SerializedName("tokenization_auth_key_id")
    private String tokenizationAuthKeyId;

    @SerializedName("flex_api_key")
    private String flexApiKey;

    public String getTokenizationAuthKeyId() {
        return tokenizationAuthKeyId;
    }

    public String getFlexApiKey() {
        return flexApiKey;
    }
}