package com.xendit.Models;

import android.os.Parcel;

import com.google.gson.annotations.SerializedName;

/**
 * Response object for unbundled authentication flows
 */
public class Authentication implements HasAuthenticationUrl {

    /**
     * Authentication ID
     */
    @SerializedName("id")
    private String id;

    @SerializedName("credit_card_token_id")
    private String creditCardTokenId;

    @SerializedName("payer_authentication_url")
    private String payerAuthenticationUrl;

    @SerializedName("status")
    private String status;

    @SerializedName("masked_card_number")
    private String maskedCardNumber;

    @SerializedName("card_info")
    private CardInfo card_info;

    @SerializedName("pa_req")
    private String requestPayload;

    @SerializedName("authentication_transaction_id")
    private String authenticationTransactionId;

    @SerializedName("threeds_version")
    private String threedsVersion;

    @SerializedName("failure_reason")
    private String failureReason;

    protected Authentication(Parcel in) {
        id = in.readString();
        creditCardTokenId = in.readString();
        payerAuthenticationUrl = in.readString();
        status = in.readString();
        maskedCardNumber = in.readString();
        requestPayload = in.readString();
        authenticationTransactionId = in.readString();
        failureReason = in.readString();
    }

    public Authentication(Token token) {
        id = token.getAuthenticationId();
        creditCardTokenId = token.getId();
        status = token.getStatus();
        maskedCardNumber = token.getMaskedCardNumber();
        card_info = token.getCardInfo();
        failureReason = token.getFailureReason();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(creditCardTokenId);
        dest.writeString(payerAuthenticationUrl);
        dest.writeString(status);
        dest.writeString(maskedCardNumber);
        dest.writeString(requestPayload);
        dest.writeString(authenticationTransactionId);
        dest.writeString(failureReason);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Authentication> CREATOR = new Creator<Authentication>() {
        @Override
        public Authentication createFromParcel(Parcel in) {
            return new Authentication(in);
        }

        @Override
        public Authentication[] newArray(int size) {
            return new Authentication[size];
        }
    };

    public String getId() {
        return id;
    }

    public String getCreditCardTokenId() {
        return creditCardTokenId;
    }

    public String getStatus() {
        return status;
    }

    public String getMaskedCardNumber() {
        return maskedCardNumber;
    }

    public CardInfo getCardInfo() {
        return card_info;
    }

    public String getRequestPayload() {
        return requestPayload;
    }

    public String getAuthenticationTransactionId() {
        return authenticationTransactionId;
    }

    public String getThreedsVersion() {
        return threedsVersion;
    }

    public String getFailureReason() {
        return failureReason;
    }

    @Override
    public String getPayerAuthenticationUrl() {
        return payerAuthenticationUrl;
    }
}
