package com.xendit;


import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.SmallTest;
import android.support.test.runner.AndroidJUnit4;

import com.xendit.Models.Card;
import com.xendit.Models.Token;
import com.xendit.Models.XenditError;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
@SmallTest
public class TokenTest {

    private Xendit xendit;
    @Before
    public void setup() {
        String PUBLISHABLE_KEY = "xnd_public_development_O4uGfOR3gbOunJU4frcaHmLCYNLy8oQuknDm+R1r9G3S/b2lBQR+gQ==";
        Context appContext = InstrumentationRegistry.getTargetContext();
        xendit = new Xendit(appContext, PUBLISHABLE_KEY);
    }

    @Test
    public void test_createSingleUseTokenAuthFalse() {

        Card card = new Card("4000000000000002",
                "12",
                "2020",
                "123");
        TokenCallback callback = new TokenCallback() {
            @Override
            public void onSuccess(Token token) {
                assertThat(token.getId(), isA(String.class));
                assertThat(token.getAuthenticationId(), isA(String.class));
            }

            @Override
            public void onError(XenditError xenditError) {
                fail();
            }
        };
        xendit.createSingleUseToken(card, 400, false, callback);
    }

    @Test
    public void test_createSingleUseTokenAuthTrue() {

        Card card = new Card("4000000000000002",
                "12",
                "2020",
                "123");
        TokenCallback callback = new TokenCallback() {
            @Override
            public void onSuccess(Token token) {
            }

            @Override
            public void onError(XenditError xenditError) {
            }
        };
        xendit.createSingleUseToken(card, 400, true, callback);
    }


    @Test
    public void test_createMultipleUseToken() {

        Card card = new Card("4000000000000002",
                "12",
                "2020",
                "123");
        TokenCallback callback = new TokenCallback() {
            @Override
            public void onSuccess(Token token) {
            }

            @Override
            public void onError(XenditError xenditError) {
            }
        };
        xendit.createMultipleUseToken(card, callback);
    }

    @Test
    public void test_createCardTokenNotMultipleUse_deprecated() {
        Card card = new Card("4000000000000002",
                "12",
                "2020",
                "123");
        TokenCallback callback = new TokenCallback() {
            @Override
            public void onSuccess(Token token) {
            }

            @Override
            public void onError(XenditError xenditError) {
            }
        };
        xendit.createCreditCardToken(card, "123456789", false, false, callback);
    }

    @Test
    public void test_createCardTokenMultipleUse_deprecated() {
        Card card = new Card("4000000000000002",
                "12",
                "2020",
                "123");
        TokenCallback callback = new TokenCallback() {
            @Override
            public void onSuccess(Token token) {
            }

            @Override
            public void onError(XenditError xenditError) {
            }
        };
        xendit.createCreditCardToken(card, "123456789", false, true, callback);
    }


    @Test
    public void test_createSingleUseToken() {

        Card card = new Card("4000000000000002",
                "12",
                "2020",
                "123");
        TokenCallback callback = new TokenCallback() {
            @Override
            public void onSuccess(Token token) {
            }

            @Override
            public void onError(XenditError xenditError) {
            }
        };
        xendit.createSingleUseToken(card, 450, callback);
    }
}