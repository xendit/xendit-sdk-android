package com.xendit;

import com.google.gson.JsonObject;

public interface InterceptorTestWithURLCallback {
    void interceptRequest(JsonObject jsonObj, String url);
    void interceptRequestFailed(String error);

    void interceptResponse(Object interceptedMessage);
    void interceptResponseFailed(String error);
}
