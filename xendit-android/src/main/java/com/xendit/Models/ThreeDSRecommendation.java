package com.xendit.Models;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Sergey on 3/22/17.
 */

public class ThreeDSRecommendation implements Parcelable {

    @SerializedName("should_3ds")
    private Boolean should_3ds;

    @SerializedName("token_id")
    private String token_id;

    private ThreeDSRecommendation(Parcel in) {
        should_3ds = in.readInt() == 1;;
        token_id = in.readString();
    }

    public static final Creator<ThreeDSRecommendation> CREATOR = new Creator<ThreeDSRecommendation>() {
        @Override
        public ThreeDSRecommendation createFromParcel(Parcel in) {
            return new ThreeDSRecommendation(in);
        }

        @Override
        public ThreeDSRecommendation[] newArray(int size) {
            return new ThreeDSRecommendation[size];
        }
    };

    public String getTokenId() {
        return token_id;
    }

    public Boolean getShould_3DS() {
        return should_3ds;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(should_3ds ? 1 : 0);
        parcel.writeString(token_id);
    }

    @NonNull
    @Override
    public String toString() {
        return "{"+this.token_id+","+this.should_3ds+"}";
    }
}