package com.xendit.utils;

import com.xendit.Models.Token;
import com.xendit.Models.XenditError;

public abstract class StoreCVNCallback {
    public abstract void onSuccess(Token token);

    public abstract void onError(XenditError error);
}
