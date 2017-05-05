package com.xendit.network.errors;

import com.android.volley.VolleyError;

public class AuthorisationError extends NetworkError {

    public AuthorisationError(VolleyError error) {
        super(error);
    }
}