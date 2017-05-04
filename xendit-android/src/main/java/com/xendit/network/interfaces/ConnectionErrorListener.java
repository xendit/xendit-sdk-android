package com.xendit.network.interfaces;

import com.xendit.network.errors.NetworkError;

public interface ConnectionErrorListener {
    void onConnectionError(NetworkError error);
}