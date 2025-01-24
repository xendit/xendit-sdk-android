package com.xendit;

import com.google.gson.JsonObject;

public interface InterceptorTestCallback {
    void interceptRequest(JsonObject jsonObj);
    void interceptRequestFailed(String error);

    void interceptResponse(Object interceptedMessage);
    void interceptResponseFailed(String error);
}

