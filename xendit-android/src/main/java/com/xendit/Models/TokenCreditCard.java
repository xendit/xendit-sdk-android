package com.xendit.Models;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Sergey on 3/22/17.
 */

public class TokenCreditCard {

    @SerializedName("keyId")
    private String keyId;

    @SerializedName("token")
    private String token;

    @SerializedName("maskedPan")
    private String maskedPan;

    @SerializedName("cardType")
    private String cardType;

    @SerializedName("timestamp")
    private long timestamp;

    @SerializedName("signedFields")
    private String signedFields;

    @SerializedName("signature")
    private String signature;

    public String getKeyId() {
        return keyId;
    }

    public String getToken() {
        return token;
    }

    public String getMaskedPan() {
        return maskedPan;
    }

    public String getCardType() {
        return cardType;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getSignedFields() {
        return signedFields;
    }

    public String getSignature() {
        return signature;
    }
}