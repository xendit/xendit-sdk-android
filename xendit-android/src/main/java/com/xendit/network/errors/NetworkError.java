package com.xendit.network.errors;

import com.android.volley.NetworkResponse;
import com.android.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;

public class NetworkError extends Exception {

    public int responseCode = -1;
    public JSONObject errorResponse;

    public NetworkError(VolleyError error) {
        super(error.getMessage(), error.getCause());
        NetworkResponse networkResponse = error.networkResponse;
        if (networkResponse != null) {
            responseCode = networkResponse.statusCode;
            if (networkResponse.data != null) {
                String errorString = new String(networkResponse.data);
                try {
                    JSONObject errorJson = new JSONObject(errorString);
                    errorResponse = errorJson;
                } catch (JSONException exception) {
                    exception.printStackTrace();
                    errorResponse = null;
                }
            }
        }
    }

    NetworkError(String detailMessage) {
        super(detailMessage);
    }
}