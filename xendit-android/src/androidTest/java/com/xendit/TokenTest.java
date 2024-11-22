package com.xendit;


import android.content.Context;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;

import com.android.volley.VolleyError;
import com.xendit.Models.AuthenticatedToken;
import com.xendit.Models.Card;
import com.xendit.Models.CardHolderData;
import com.xendit.Models.Token;
import com.xendit.Models.XenditError;

import com.xendit.interceptor.Interceptor;
import com.xendit.interceptor.InterceptorImpl;
import com.xendit.network.BaseRequest;
import java.util.concurrent.TimeUnit;
import org.awaitility.Awaitility;
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
        String PUBLISHABLE_KEY = "xnd_public_development_D8wJuWpOY15JvjJyUNfCdDUTRYKGp8CSM3W0ST4d0N4CsugKyoGEIx6b84j1D7Pg";
        Context appContext = InstrumentationRegistry.getInstrumentation().getContext();
        xendit = Xendit.create(appContext, PUBLISHABLE_KEY);
    }

    @Test
    public void test_createSingleUseTokenAuthFalse() {
        final boolean[] done = { false };
        Card card = new Card("4000000000001091",
                "12",
                "2030",
                "123");
        TokenCallback callback = new TokenCallback() {
            @Override
            public void onSuccess(Token token) {
                System.out.println("Success " + token.getId());
                done[0] = true;
                assertThat(token.getId(), isA(String.class));
                assertThat(token.getAuthenticationId(), isA(String.class));
            }

            @Override
            public void onError(XenditError xenditError) {
                System.out.println("Error " + xenditError.getErrorMessage());
                done[0] = true;
                fail();
            }
        };

        String PUBLISHABLE_KEY = "xnd_public_development_D8wJuWpOY15JvjJyUNfCdDUTRYKGp8CSM3W0ST4d0N4CsugKyoGEIx6b84j1D7Pg";
        Context appContext = InstrumentationRegistry.getInstrumentation().getContext();
        Xendit xendit = new XenditImpl(appContext, PUBLISHABLE_KEY,
            new Interceptor<BaseRequest<?>>() {
                @Override public void intercept(BaseRequest<?> interceptedMessage) {
                    System.out.println("Intercepted " + interceptedMessage.getUrl());
                }

                @Override public void handleError(VolleyError error) {
                    System.out.println("Error " + error.getMessage());
                }
            }, new Interceptor<Object>() {
                @Override public void intercept(Object interceptedMessage) {
                    System.out.println("Intercepted " + interceptedMessage.getClass().getName());
                    AuthenticatedToken authenticatedToken = (AuthenticatedToken) interceptedMessage;
                    System.out.println("AuthenticatedToken " + authenticatedToken.getId());
                    done[0] = true;
                }

                @Override public void handleError(VolleyError error) {
                    System.out.println("Error " + error.getMessage());
                }
        });
        xendit.createSingleUseToken(card, 10000, true, onBehalfOf, callback);

        Awaitility.await().atMost(10, TimeUnit.SECONDS).until(() -> done[0]);
    }

    @Test
    public void test_createSingleUseTokenAuthTrue() {
        Card card = new Card("4000000000000002",
                "12",
                "2050",
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
                "2050",
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
    public void test_createSingleUseTokenAuthFalseWithCardHolderData() {
        CardHolderData cardHolderData = new CardHolderData("John", "Doe", "johndoe@example.com", "+628212223242526");
        Card card = new Card("4000000000000002",
                "12",
                "2050",
                "123",
                cardHolderData);
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
    public void test_createSingleUseTokenAuthTrueWithCardHolderData() {
        CardHolderData cardHolderData = new CardHolderData("John", "Doe", "johndoe@example.com", "+628212223242526");
        Card card = new Card("4000000000000002",
                "12",
                "2050",
                "123",
                cardHolderData);
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
    public void test_createMultipleUseTokenWithCardHolderData() {
        CardHolderData cardHolderData = new CardHolderData("John", "Doe", "johndoe@example.com", "+628212223242526");
        Card card = new Card("4000000000000002",
                "12",
                "2050",
                "123",
                cardHolderData);
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
                "2050",
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
                "2050",
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
                "2050",
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
                "2050",
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
                "2050",
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
                "2050",
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