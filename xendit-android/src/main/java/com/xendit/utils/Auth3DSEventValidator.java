package com.xendit.utils;

import android.content.Context;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.xendit.R;
import java.util.HashMap;
import java.util.Map;

public class Auth3DSEventValidator {
  private static final String ID_FIELD = "id";
  private static final String STATUS_FIELD = "status";

  private Auth3DSEventValidator() {
    // Private constructor to prevent instantiation
  }

  public static boolean is3DSResultEventFromXendit(String message, Context context) {
    if (message.isEmpty()) return false;

    return isValidJsonMessage(message) || isKnownErrorMessage(message, context);
  }

  private static boolean isValidJsonMessage(String message) {
    try {
      Map<String, Object> messageInJson = new Gson().fromJson(
          message,
          new TypeToken<HashMap<String, Object>>() {}.getType()
      );

      // A valid 3ds callback payload from Xendit, should contain required fields: id and status.
      return messageInJson.get(ID_FIELD) != null && messageInJson.get(STATUS_FIELD) != null;
    } catch (Exception e) {
      return false;
    }
  }

  private static boolean isKnownErrorMessage(String message, Context context) {
    return message.equals(context.getString(R.string.create_token_error_validation)) ||
        message.equals(context.getString(R.string.tokenization_error));
  }

}
