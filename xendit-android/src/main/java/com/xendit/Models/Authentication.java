package com.xendit.Models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Sergey on 3/22/17.
 */

public class Authentication implements Parcelable {

    @SerializedName("id")
    private String id;

    @SerializedName("status")
    private String status;

    @SerializedName("payer_authentication_url")
    private String payerAuthenticationUrl;

    private Authentication(Parcel in) {
        id = in.readString();
        status = in.readString();
        payerAuthenticationUrl = in.readString();
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

    public String getStatus() {
        return status;
    }

    public String getPayerAuthenticationUrl() {
        return payerAuthenticationUrl;
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
    }

    @Override
    public String toString() {
        return "{"+this.id+","+this.status+"}";
    }
}