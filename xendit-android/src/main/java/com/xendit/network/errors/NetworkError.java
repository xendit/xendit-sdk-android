package com.xendit.network.errors;

import com.android.volley.NetworkResponse;
import com.android.volley.VolleyError;

public class NetworkError extends Exception {

    public int responseCode = -1;
    private String errorResponse;

    public NetworkError(VolleyError error) {
        super(error.getMessage(), error.getCause());
        NetworkResponse networkResponse = error.networkResponse;
        if (networkResponse != null) {
            responseCode = networkResponse.statusCode;
            if (networkResponse.data != null) {
                errorResponse = new String(networkResponse.data).trim();
            }
        }
    }

    NetworkError(String detailMessage) {
        super(detailMessage);
    }
}