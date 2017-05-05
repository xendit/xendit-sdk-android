package com.xendit.network.interfaces;

import com.xendit.network.errors.NetworkError;

public interface AuthorisationErrorListener {
    void onAuthorisationError(NetworkError error);
}