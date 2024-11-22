package com.xendit.network;

import androidx.annotation.Nullable;
import com.android.volley.AuthFailureError;
import com.android.volley.NoConnectionError;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.xendit.interceptor.Interceptor;
import com.xendit.network.errors.AuthorisationError;
import com.xendit.network.errors.ConnectionError;
import com.xendit.network.errors.NetworkError;

public class DefaultResponseHandler<T> implements Response.Listener<T>, Response.ErrorListener {

    private final NetworkHandler<T> handler;;
    private final Interceptor<T> interceptor;

    public DefaultResponseHandler(NetworkHandler<T> handler, @Nullable Interceptor<T> interceptor) {
        this.interceptor = interceptor;
        this.handler = handler;
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        if (interceptor != null) {
            interceptor.handleError(error);
        }
        if (handler != null) {
            NetworkError netError;
            if (error instanceof TimeoutError || error instanceof NoConnectionError) {
                netError = new ConnectionError(error);
            } else if (error instanceof AuthFailureError) {
                netError = new AuthorisationError(error);
            } else {
                netError = new NetworkError(error);
            }
            handler.handleError(netError);
        }
    }

    @Override
    public void onResponse(T response) {
        if (interceptor != null) {
            interceptor.intercept(response);
        }
        if (handler != null) {
            handler.handleSuccess(response);
        }
    }
}