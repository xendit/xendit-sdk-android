package com.xendit;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.xendit.Models.Authentication;
import com.xendit.Models.Token;
import com.xendit.Models.XenditError;

import org.json.JSONException;
import org.json.JSONObject;


/**
 * Created by Javier on 26/10/20.
 *
 * This maps our unbundled authentication response into a bundled authentication response.
 * Required for backward compatibility after migration to 3DS 2.0 in which only
 * unbundled flow is supported.
 */

public class AuthenticatedTokenBroadcastReceiver extends BroadcastReceiver {

    private final String TAG = "AuthenticatedTokenBroadcastReceiver";
    private TokenCallback tokenCallback;

    public AuthenticatedTokenBroadcastReceiver(TokenCallback tokenCallback) {
        this.tokenCallback = tokenCallback;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            String message = intent.getExtras().getString(XenditActivity.MESSAGE_KEY);
            if (!message.isEmpty() && message.equals(context.getString(R.string.create_token_error_validation))) {
                tokenCallback.onError(new XenditError(context.getString(R.string.create_token_error_validation)));
            } else if (message.equals(context.getString(R.string.tokenization_error))) {
                tokenCallback.onError(new XenditError("TOKENIZATION_ERROR", context.getString(R.string.tokenization_error)));
            } else {
                Gson gson = new Gson();
                Authentication authentication = gson.fromJson(message, Authentication.class);
                if (authentication.getStatus().equals("VERIFIED")) {
                    tokenCallback.onSuccess(new Token(authentication));
                } else {
                    try {
                        JSONObject errorJson = new JSONObject(message);
                        String errorMessage = errorJson.getString("failure_reason");
                        tokenCallback.onError(new XenditError(errorMessage));
                    } catch (JSONException e) {
                        e.printStackTrace();
                        tokenCallback.onError(new XenditError("SERVER_ERROR", context.getString(R.string.tokenization_error)));
                    }

                }
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
            tokenCallback.onError(new XenditError("SERVER_ERROR", e.getMessage()));
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
            tokenCallback.onError(new XenditError("SERVER_ERROR", "Error parsing response from 3DS. Please try again."));
        }

        context.unregisterReceiver(this);
    }
}