package com.xendit.utils;

import android.content.Context;
import android.content.pm.PackageManager;

public class PermissionUtils {
    public static boolean hasPermission(Context context, String permission) {
        int result = context.checkCallingOrSelfPermission(permission);
        return result == PackageManager.PERMISSION_GRANTED;
    }
}
