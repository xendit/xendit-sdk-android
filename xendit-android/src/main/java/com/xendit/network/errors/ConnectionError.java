package com.xendit.network.errors;

import com.android.volley.VolleyError;

public class ConnectionError extends NetworkError {

    private static final String NO_INTERNET_CONNECTION_ERROR = "No internet connection";

    public ConnectionError() {
        super(NO_INTERNET_CONNECTION_ERROR);
    }

    public ConnectionError(VolleyError error) {
        super(error);
    }
}