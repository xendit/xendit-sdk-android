package com.xendit.Models;

import android.os.Parcel;
import android.support.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Sergey on 3/22/17.
 * Response object for tokenization and bundled authentication flows
 *
 */

public class AuthenticatedToken implements HasAuthenticationUrl {

    /**
     * Credit Card Token ID
     */
    @SerializedName("id")
    private String id;

    @SerializedName("status")
    private String status;

    @SerializedName("authentication_id")
    private String authentication_id;

    @SerializedName("payer_authentication_url")
    private String payerAuthenticationUrl;

    @SerializedName("masked_card_number")
    private String maskedCardNumber;

    @SerializedName("metadata")
    private CardMetadata metadata;

    @SerializedName("jwt")
    private String jwt;

    @SerializedName("threeds_version")
    private String threedsVersion;

    @SerializedName("environment")
    private String environment;

    private AuthenticatedToken(Parcel in) {
        id = in.readString();
        status = in.readString();
        payerAuthenticationUrl = in.readString();
        maskedCardNumber = in.readString();
    }

    public static final Creator<AuthenticatedToken> CREATOR = new Creator<AuthenticatedToken>() {
        @Override
        public AuthenticatedToken createFromParcel(Parcel in) {
            return new AuthenticatedToken(in);
        }

        @Override
        public AuthenticatedToken[] newArray(int size) {
            return new AuthenticatedToken[size];
        }
    };

    public String getId() {
        return id;
    }

    public String getStatus() {
        return status;
    }

    public String getPayerAuthenticationUrl() {
        return payerAuthenticationUrl;
    }

    public String getAuthenticationId() { return authentication_id; }

    public String getMaskedCardNumber() { return maskedCardNumber; }

    public CardMetadata getMetadata() { return metadata; }

    public String getJwt() {
        return jwt;
    }

    public String getThreedsVersion() {
        return threedsVersion;
    }

    public String getEnvironment() {
        return environment;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(id);
        parcel.writeString(status);
        parcel.writeString(payerAuthenticationUrl);
        parcel.writeString(maskedCardNumber);
    }

    @NonNull
    @Override
    public String toString() {
        return "{"+this.id+","+this.status+"}";
    }
}