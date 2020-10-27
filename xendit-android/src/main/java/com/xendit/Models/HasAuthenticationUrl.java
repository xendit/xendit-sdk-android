package com.xendit.Models;

import android.os.Parcelable;

public interface HasAuthenticationUrl extends Parcelable {
    String getPayerAuthenticationUrl();
}
