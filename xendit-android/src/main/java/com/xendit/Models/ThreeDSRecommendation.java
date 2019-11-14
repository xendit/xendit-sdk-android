package com.xendit.Models;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Sergey on 3/22/17.
 */

public class ThreeDSRec implements Parcelable {

    @SerializedName("status")
    private String should3ds;

    @SerializedName("authentication_id")
    private String token_id;

    private ThreeDSRec(Parcel in) {
        should3ds = in.readString();
        token_id = in.readString();
    }

    public static final Creator<ThreeDSRec> CREATOR = new Creator<ThreeDSRec>() {
        @Override
        public ThreeDSRec createFromParcel(Parcel in) {
            return new ThreeDSRec(in);
        }

        @Override
        public ThreeDSRec[] newArray(int size) {
            return new ThreeDSRec[size];
        }
    };

    public String getTokenId() {
        return token_id;
    }

    public String get3DSRec() {
        return should3ds;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(should3ds);
        parcel.writeString(token_id);
    }

    @NonNull
    @Override
    public String toString() {
        return "{"+this.token_id+","+this.should3ds+"}";
    }
}