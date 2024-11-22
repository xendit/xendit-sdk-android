package com.xendit.interceptor;

import com.android.volley.VolleyError;
import com.xendit.Models.XenditError;

public class InterceptorImpl<T> implements Interceptor<T> {
    private final Interceptor.Callback<T> callback;

    InterceptorImpl(Interceptor.Callback<T> callback) {
        this.callback = callback;
    }

    @Override public void intercept(T interceptedMessage) {
        callback.onSuccess(interceptedMessage);
    }

    @Override public void handleError(VolleyError error) {
        callback.onError(error);
    }
}
