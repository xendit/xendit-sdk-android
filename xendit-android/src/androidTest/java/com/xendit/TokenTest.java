package com.xendit;


import android.content.Context;

import androidx.test.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;

import com.xendit.Models.Card;
import com.xendit.Models.Token;
import com.xendit.Models.XenditError;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.isA;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

@RunWith(AndroidJUnit4.class)
@SmallTest
public class TokenTest {

    private Xendit xendit;
    private String onBehalfOf = "";
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
                "2030",
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

        xendit.createSingleUseToken(card, 400, false, onBehalfOf, callback);
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
        xendit.createSingleUseToken(card, 400, true, onBehalfOf, callback);
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
        xendit.createMultipleUseToken(card, onBehalfOf, callback);
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
        xendit.createCreditCardToken(card, "123456789", false, onBehalfOf, false, callback);
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
        xendit.createCreditCardToken(card, "123456789", false, onBehalfOf, true, callback);
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
        xendit.createSingleUseToken(card, 450, true, onBehalfOf, callback);
    }

    @Test
    public void test_createSingleUseTokenInvalidCard() {
        Card card = new Card("4000000000000001",
                "12",
                "2020",
                "123");
        TokenCallback callback = new TokenCallback() {
            @Override
            public void onSuccess(Token token) {
                fail();
            }

            @Override
            public void onError(XenditError xenditError) {
                assertThat(xenditError.getErrorCode(), isA(String.class));
                assertEquals(xenditError.getErrorCode(), "API_VALIDATION_ERROR");
            }
        };

        xendit.createSingleUseToken(card, 400, false, onBehalfOf, callback);
    }

    @Test
    public void test_createSingleUseTokenInvalidExpiryMonth() {
        Card card = new Card("4000000000000002",
                "120",
                "2020",
                "123");
        TokenCallback callback = new TokenCallback() {
            @Override
            public void onSuccess(Token token) {
                fail();
            }

            @Override
            public void onError(XenditError xenditError) {
                assertThat(xenditError.getErrorCode(), isA(String.class));
                assertEquals(xenditError.getErrorCode(), "API_VALIDATION_ERROR");
            }
        };

        xendit.createSingleUseToken(card, 400, false, onBehalfOf, callback);
    }

    @Test
    public void test_createSingleUseTokenInvalidExpiryYear() {
        Card card = new Card("4000000000000002",
                "12",
                "2016",
                "123");
        TokenCallback callback = new TokenCallback() {
            @Override
            public void onSuccess(Token token) {
                fail();
            }

            @Override
            public void onError(XenditError xenditError) {
                assertThat(xenditError.getErrorCode(), isA(String.class));
                assertEquals(xenditError.getErrorCode(), "API_VALIDATION_ERROR");
            }
        };

        xendit.createSingleUseToken(card, 400, false, onBehalfOf, callback);
    }

    @Test
    public void test_createSingleUseTokenInvalidCvn() {
        Card card = new Card("4000000000000002",
                "12",
                "2020",
                "12");
        TokenCallback callback = new TokenCallback() {
            @Override
            public void onSuccess(Token token) {
                fail();
            }

            @Override
            public void onError(XenditError xenditError) {
                assertThat(xenditError.getErrorCode(), isA(String.class));
                assertEquals(xenditError.getErrorCode(), "API_VALIDATION_ERROR");
            }
        };

        xendit.createSingleUseToken(card, 400, false, onBehalfOf, callback);
    }
}