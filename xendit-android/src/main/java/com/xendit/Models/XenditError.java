package com.xendit.Models;

/**
 * Created by Sergey on 3/16/17.
 */

public class XenditError {

    private String error;
    private String networkError;
    private Authentication authentication;

    public XenditError(String error) {
        this.error = error;
    }

    public XenditError(String error, Authentication authentication) {
        this(error);
        this.authentication = authentication;
    }

    public XenditError(String error, String networkError) {
        this(error);
        this.networkError = networkError;
    }

    public String getError() {
        return error;
    }

    public String getNetworkError() {
        return networkError;
    }

    public Authentication getAuthentication() {
        return authentication;
    }
}