package com.xendit;

import android.content.Context;
import android.os.Debug;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;

import com.xendit.Models.AuthenticatedToken;
import com.xendit.Models.Authentication;
import com.xendit.Models.CardHolderData;
import com.xendit.Models.Token;
import com.xendit.Models.XenditError;

import java.util.concurrent.TimeUnit;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.awaitility.Awaitility;

@RunWith(AndroidJUnit4.class)
@SmallTest
public class AuthTest {

    private final static String PUBLISHABLE_KEY = "xnd_public_development_D8wJuWpOY15JvjJyUNfCdDUTRYKGp8CSM3W0ST4d0N4CsugKyoGEIx6b84j1D7Pg";
    private Context appContext = InstrumentationRegistry.getInstrumentation().getContext();
    private final Xendit xendit = Xendit.create(appContext, PUBLISHABLE_KEY);

    @Test
    public void test_createAuth() {
        final boolean[] done = { false };
        AuthenticationCallback callback = new AuthenticationCallback() {
            @Override public void onSuccess(Authentication authentication) {
                System.out.println("Success");
                done[0] = true;
            }

            @Override
            public void onError(XenditError xenditError) {
                System.out.println("Error " + xenditError.getErrorMessage());
                done[0] = true;
            }
        };
        xendit.createAuthentication("9823104219412", 200, callback);

        Awaitility.await().atMost(10, TimeUnit.SECONDS).until(() -> done[0]);
    }

    @Test
    public void test_createAuthWithCardHolderData() {
        AuthenticationCallback callback = new AuthenticationCallback() {
            @Override public void onSuccess(Authentication authentication) {

            }

            @Override
            public void onError(XenditError xenditError) {
            }
        };
        CardHolderData cardHolderData = new CardHolderData("John", "Doe", "johndoe@example.com", "+628212223242526");
        xendit.createAuthentication("9823104219412", 200, cardHolderData, callback);
    }

}
