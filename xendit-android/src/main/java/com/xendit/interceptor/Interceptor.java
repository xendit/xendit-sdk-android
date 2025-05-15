package com.xendit.interceptor;

import com.android.volley.VolleyError;
import com.xendit.Models.XenditError;


public interface Interceptor<T> {
    void intercept(T interceptedMessage);
    void handleError(VolleyError error);

    interface Callback<T> {
        void onSuccess(T request);
        void onError(XenditError error);
        void onError(VolleyError error);
    }
}
