package com.xendit.network;

import com.xendit.network.errors.AuthorisationError;
import com.xendit.network.errors.ConnectionError;
import com.xendit.network.errors.NetworkError;
import com.xendit.network.interfaces.AuthorisationErrorListener;
import com.xendit.network.interfaces.ConnectionErrorListener;
import com.xendit.network.interfaces.ResultListener;

public class NetworkHandler<T> {

    private ResultListener<T> resultListener;
    private ConnectionErrorListener connectionListener;
    private AuthorisationErrorListener authorisationListener;

    final void handleSuccess(final T response) {
        if (resultListener != null) {
            resultListener.onSuccess(response);
        }
    }

    public final void handleError(final NetworkError error) {
        if (error instanceof ConnectionError && connectionListener != null) {
            connectionListener.onConnectionError(error);
        } else if (error instanceof AuthorisationError && authorisationListener != null) {
            authorisationListener.onAuthorisationError(error);
        } else {
            deliverError(error);
        }
    }

    private void deliverError(NetworkError error) {
        if (resultListener != null) {
            resultListener.onFailure(error);
        }
    }

    public NetworkHandler<T> setResultListener(ResultListener<T> resultListener) {
        this.resultListener = resultListener;
        return this;
    }

    public NetworkHandler<T> setConnectionErrorListener(ConnectionErrorListener connectionErrorListener) {
        this.connectionListener = connectionErrorListener;
        return this;
    }

    public NetworkHandler<T> setAuthorisationErrorListener(AuthorisationErrorListener authorisationListener) {
        this.authorisationListener = authorisationListener;
        return this;
    }
}