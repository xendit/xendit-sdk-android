package com.xendit;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.google.gson.Gson;
import com.xendit.Models.Authentication;
import com.xendit.Models.XenditError;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by gonzalez on 7/26/17.
 */

public class AuthenticationBroadcastReceiver extends BroadcastReceiver {
    private AuthenticationCallback authenticationCallback;

    public AuthenticationBroadcastReceiver(AuthenticationCallback authenticationCallback) {
        this.authenticationCallback = authenticationCallback;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String message = intent.getExtras().getString(XenditActivity.MESSAGE_KEY);

        if (message != null && message.equals(context.getString(R.string.create_token_error_validation))) {
            authenticationCallback.onError(new XenditError(context.getString(R.string.create_token_error_validation)));
        } else {
            Gson gson = new Gson();
            Authentication authentication = gson.fromJson(message, Authentication.class);

            if (authentication.getStatus().equals("VERIFIED")) {
                authenticationCallback.onSuccess(authentication);
            } else {
                try {
                    JSONObject errorJson = new JSONObject(message);
                    String errorMessage = errorJson.getString("failure_reason");
                    authenticationCallback.onError(new XenditError(errorMessage, authentication));
                } catch (JSONException e) {
                    e.printStackTrace();
                    authenticationCallback.onError(new XenditError("SERVER_ERROR", context.getString(R.string.authentication_error)));
                }

            }
        }

        context.unregisterReceiver(this);
    }
}
