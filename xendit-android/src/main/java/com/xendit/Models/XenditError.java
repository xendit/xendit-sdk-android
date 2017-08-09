package com.xendit.Models;

import com.xendit.network.errors.NetworkError;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Sergey on 3/16/17.
 */

public class XenditError {

    private String errorCode;
    private String errorMessage;
    private Authentication authentication;

    public XenditError(String error) {
        this.errorCode = "API_VALIDATION_ERROR";
        this.errorMessage = error;
    }

    public XenditError(String errorCode, String errorMessage) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    public XenditError(NetworkError networkError) {
        try {
            this.errorCode =  networkError.errorResponse.getString("error_code");
            this.errorMessage = networkError.errorResponse.getString("message");
        } catch (Exception e) {
            try {
                JSONObject flexResponseStatus = networkError.errorResponse.getJSONObject("responseStatus");
                this.errorCode = flexResponseStatus.getString("reason");
                this.errorMessage = flexResponseStatus.getString("message");
            } catch (JSONException ex) {
                this.errorCode = "SERVER_ERROR";
                this.errorMessage = "Something unexpected happened, we are investigating this issue right now";
            }
        }

        this.authentication = null;
    }

    public XenditError(String failureReason, Authentication authentication) {
        this.errorMessage = failureReason;
        this.errorCode = failureReason;
        this.authentication = authentication;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public Authentication getAuthentication() {
        return authentication;
    }
}