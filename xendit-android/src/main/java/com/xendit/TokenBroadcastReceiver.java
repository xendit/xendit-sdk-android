package com.xendit;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.google.gson.Gson;
import com.xendit.Models.Authentication;
import com.xendit.Models.Token;
import com.xendit.Models.XenditError;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Sergey on 3/30/17.
 */

public class TokenBroadcastReceiver extends BroadcastReceiver {

    private TokenCallback tokenCallback;

    public TokenBroadcastReceiver(TokenCallback tokenCallback) {
        this.tokenCallback = tokenCallback;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String message = intent.getExtras().getString(XenditActivity.MESSAGE_KEY);
        if (message != null && message.equals(context.getString(R.string.create_token_error_validation))) {
            tokenCallback.onError(new XenditError(context.getString(R.string.create_token_error_validation)));
        } else {
            Gson gson = new Gson();
            Authentication authentication = gson.fromJson(message, Authentication.class);
            if (authentication.getStatus().equals("VERIFIED")) {
                tokenCallback.onSuccess(new Token(authentication));
            } else {
                try {
                    JSONObject errorJson = new JSONObject(message);
                    String errorMessage = errorJson.getString("failure_reason");
                    tokenCallback.onError(new XenditError(errorMessage, authentication));
                } catch (JSONException e) {
                    e.printStackTrace();
                    tokenCallback.onError(new XenditError(context.getString(R.string.tokenization_error)));
                }

            }
        }

        context.unregisterReceiver(this);
    }
}