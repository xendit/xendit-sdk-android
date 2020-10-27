package com.xendit;

import com.xendit.Models.Authentication;
import com.xendit.Models.XenditError;

/**
 * Created by gonzalez on 7/26/17.
 */

public abstract class AuthenticationCallback {
    public abstract void onSuccess(Authentication authentication);

    public abstract void onError(XenditError error);
}
