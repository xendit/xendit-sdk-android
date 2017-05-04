package com.xendit.network.interfaces;

import com.xendit.network.errors.NetworkError;

public interface ResultListener<T> {

    void onSuccess(T responseObject);

    void onFailure(NetworkError error);
}