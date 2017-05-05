package com.xendit;

import com.xendit.Models.Token;
import com.xendit.Models.XenditError;

/**
 * Created by Sergey on 3/16/17.
 */

public abstract class TokenCallback {

    public abstract void onSuccess(Token token);

    public abstract void onError(XenditError error);
}