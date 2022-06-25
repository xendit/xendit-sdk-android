package com.xendit;

import android.content.Context;

import androidx.test.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;

import com.xendit.Models.Authentication;
import com.xendit.Models.XenditError;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
@SmallTest
public class AuthTest {

    private final static String PUBLISHABLE_KEY = "xnd_public_development_O4uGfOR3gbOunJU4frcaHmLCYNLy8oQuknDm+R1r9G3S/b2lBQR+gQ==";
    private Context appContext = InstrumentationRegistry.getTargetContext();
    private final Xendit xendit = new Xendit(appContext, PUBLISHABLE_KEY);

    @Test
    public void test_createAuth() {
        AuthenticationCallback callback = new AuthenticationCallback() {
            @Override
            public void onSuccess(Authentication authentication) {
            }

            @Override
            public void onError(XenditError xenditError) {
            }
        };
        xendit.createAuthentication("9823104219412", 200, callback);
    }


    @Test
    public void test_createAuth_deprecated() {
        AuthenticationCallback callback = new AuthenticationCallback() {
            @Override
            public void onSuccess(Authentication authentication) {

            }

            @Override
            public void onError(XenditError xenditError) {
            }
        };


        xendit.createAuthentication("9823104219412", 123, "250", callback);
    }
}
