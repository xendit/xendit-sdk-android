package com.xendit;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.xendit.Models.Authentication;
import com.xendit.Models.XenditError;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


/**
 * Created by gonzalez on 7/26/17.
 */

public class AuthenticationBroadcastReceiver extends BroadcastReceiver {

    private final String TAG = "AuthenticationBroadcastReceiver";
    private AuthenticationCallback authenticationCallback;

    public AuthenticationBroadcastReceiver(AuthenticationCallback authenticationCallback) {
        this.authenticationCallback = authenticationCallback;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            String message = intent.getExtras().getString(XenditActivity.MESSAGE_KEY);
            Log.d("TAG", "AuthenticationBroadcastReceiver onReceive: " + message);
            if (!message.isEmpty() && message.equals(context.getString(R.string.create_token_error_validation))) {
                authenticationCallback.onError(new XenditError(context.getString(R.string.create_token_error_validation)));
                context.unregisterReceiver(this);

            } else if (message.equals(context.getString(R.string.tokenization_error))) {
                authenticationCallback.onError(new XenditError("AUTHENTICATION_ERROR", context.getString(R.string.tokenization_error)));
                context.unregisterReceiver(this);

            } else {
                if (is3DSResultEventFromXendit(message)) {
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
                    context.unregisterReceiver(this);

                } else {
                // NOTE: To handle case where event is triggered from external parties other than Xendit.
                // Event payload here is not nullish but it is unknown format.
                // Do not unregister broadcast receiver here.
                }
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
            authenticationCallback.onError(new XenditError("SERVER_ERROR", e.getMessage()));
            context.unregisterReceiver(this);

        } catch (JsonSyntaxException e) {
            e.printStackTrace();
            authenticationCallback.onError(new XenditError("SERVER_ERROR", "Error parsing response from 3DS. Please try again."));
            context.unregisterReceiver(this);

        }
    }

    private boolean is3DSResultEventFromXendit(String messageInString){
        Map<String, Object> messageInJson = new Gson().fromJson(
                messageInString, new TypeToken<HashMap<String, Object>>() {}.getType()
        );

        // A valid 3ds callback payload from Xendit, should contain required fields: id and status.
        return messageInJson.get("id") != null && messageInJson.get("status") != null;
    }
}
