package com.xendit;


import android.content.Context;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;

import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.xendit.Models.Address;
import com.xendit.Models.AuthenticatedToken;
import com.xendit.Models.BillingDetails;
import com.xendit.Models.Card;
import com.xendit.Models.CardHolderData;
import com.xendit.Models.Token;
import com.xendit.Models.XenditError;

import com.xendit.interceptor.Interceptor;
import com.xendit.network.BaseRequest;
import java.io.UnsupportedEncodingException;
import java.util.concurrent.TimeUnit;
import org.awaitility.Awaitility;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

@RunWith(AndroidJUnit4.class)
@SmallTest
public class TokenTest {
    private final String onBehalfOf = "";
    private static final Gson gson = new GsonBuilder().create();
    private static final String PROTOCOL_CHARSET = "utf-8";
    static final String PUBLISHABLE_KEY = "xnd_public_development_D8wJuWpOY15JvjJyUNfCdDUTRYKGp8CSM3W0ST4d0N4CsugKyoGEIx6b84j1D7Pg";
    @Before
    public void setup() {
    }

    public static Xendit createXendit(
        InterceptorTestCallback intercept
    ) {
        Context appContext = InstrumentationRegistry.getInstrumentation().getContext();
        return new XenditImpl(appContext, PUBLISHABLE_KEY,
            new Interceptor<BaseRequest<?>>() {
                @Override public void intercept(BaseRequest<?> interceptedMessage) {
                    // convert utf-8 bytes to string
                    String jsonBody = "";
                    try {
                        jsonBody= new String(interceptedMessage.getBody(), PROTOCOL_CHARSET);
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    JsonObject jsonObj = gson.fromJson(jsonBody, JsonObject.class);
                    System.out.println("Intercepted " + jsonObj.toString());

                    intercept.interceptRequest(jsonObj);
                }

                @Override public void handleError(VolleyError error) {
                    System.out.println("Error " + error.getMessage() + " " + error.getCause());
                    intercept.interceptRequestFailed(error.getMessage());
                }
            }, new Interceptor<Object>() {
            @Override public void intercept(Object interceptedMessage) {
                intercept.interceptResponse(interceptedMessage);
            }

            @Override public void handleError(VolleyError error) {
                System.out.println("Error " + error.getMessage() + " " + error.getCause());
                intercept.interceptResponseFailed(error.getMessage());

            }
        });
    }

    // Create token with invalid card expiry month
    @Test
    public void test_createSingleUseTokenAuth_invalid_card_expiry_month() {
        final boolean[] done = { false };
        Card card = new Card("4000000000001091",
                "13",
                "2030",
                "123");
        TokenCallback callback = new TokenCallback() {
            @Override
            public void onSuccess(Token token) {
                fail();
            }

            @Override
            public void onError(XenditError xenditError) {
                System.out.println("Error " + xenditError.getErrorMessage());
                assertEquals(xenditError.getErrorMessage(), "Card expiration date is invalid");
                done[0] = true;
            }
        };

        Xendit x = createXendit(
            new InterceptorTestCallback() {
                @Override
                public void interceptRequest(JsonObject jsonObj) {
                    // verify if the json object contains card_data
                    assertNotNull(jsonObj.get("card_data").getAsJsonObject().get("account_number"));
                    assertNotNull(jsonObj.get("card_data").getAsJsonObject().get("exp_year"));
                    assertNotNull(jsonObj.get("card_data").getAsJsonObject().get("exp_month"));
                    assertNotNull(jsonObj.get("card_data").getAsJsonObject().get("cvn"));
                }
                @Override
                public void interceptRequestFailed(String error) {
                    // should not be called
                    fail();
                }
                @Override
                public void interceptResponse(Object interceptedMessage) {
                    // should not be called
                    fail();
                }
                @Override
                public void interceptResponseFailed(String error) {
                    // should not be called
                    fail();
                }
            });
        x.createSingleUseToken(card, 10000, true, onBehalfOf, callback);

        Awaitility.await().atMost(10, TimeUnit.SECONDS).until(() -> done[0]);
    }

    // Create token with invalid card expiry year
    @Test
    public void test_createSingleUseToken_auth_invalid_card_expiry_year() {
        final boolean[] done = { false };
        Card card = new Card("4000000000001091",
            "12",
            "20301",
            "123");
        TokenCallback callback = new TokenCallback() {
            @Override
            public void onSuccess(Token token) {
                fail();
            }

            @Override
            public void onError(XenditError xenditError) {
                System.out.println("Error " + xenditError.getErrorMessage());
                assertEquals(xenditError.getErrorMessage(), "Card expiration date is invalid");
                done[0] = true;
            }
        };

        Xendit x = createXendit(
            new InterceptorTestCallback() {
                @Override
                public void interceptRequest(JsonObject jsonObj) {
                    // verify if the json object contains card_data
                    assertNotNull(jsonObj.get("card_data").getAsJsonObject().get("account_number"));
                    assertNotNull(jsonObj.get("card_data").getAsJsonObject().get("exp_year"));
                    assertNotNull(jsonObj.get("card_data").getAsJsonObject().get("exp_month"));
                    assertNotNull(jsonObj.get("card_data").getAsJsonObject().get("cvn"));
                }
                @Override
                public void interceptRequestFailed(String error) {
                    // should not be called
                    fail();
                }
                @Override
                public void interceptResponse(Object interceptedMessage) {
                    // should not be called
                    fail();
                }
                @Override
                public void interceptResponseFailed(String error) {
                    // should not be called
                    fail();
                }
            });
        x.createSingleUseToken(card, 10000, true, onBehalfOf, callback);

        Awaitility.await().atMost(10, TimeUnit.SECONDS).until(() -> done[0]);
    }

    // Create token with invalid card number
    @Test
    public void test_createSingleUseToken_auth_invalid_card_number() {
        final boolean[] done = { false };
        Card card = new Card("4000000000001099",
            "12",
            "2030",
            "123");
        TokenCallback callback = new TokenCallback() {
            @Override
            public void onSuccess(Token token) {
                fail();
            }

            @Override
            public void onError(XenditError xenditError) {
                System.out.println("Error " + xenditError.getErrorMessage());
                assertEquals(xenditError.getErrorMessage(), "Card number is invalid");
                done[0] = true;
            }
        };

        Xendit x = createXendit(
            new InterceptorTestCallback() {
                @Override
                public void interceptRequest(JsonObject jsonObj) {
                    // verify if the json object contains card_data
                    assertNotNull(jsonObj.get("card_data").getAsJsonObject().get("account_number"));
                    assertNotNull(jsonObj.get("card_data").getAsJsonObject().get("exp_year"));
                    assertNotNull(jsonObj.get("card_data").getAsJsonObject().get("exp_month"));
                    assertNotNull(jsonObj.get("card_data").getAsJsonObject().get("cvn"));
                }
                @Override
                public void interceptRequestFailed(String error) {
                    // should not be called
                    fail();
                }
                @Override
                public void interceptResponse(Object interceptedMessage) {
                    // should not be called
                    fail();
                }
                @Override
                public void interceptResponseFailed(String error) {
                    // should not be called
                    fail();
                }
            });
        x.createSingleUseToken(card, 10000, true, onBehalfOf, callback);

        Awaitility.await().atMost(10, TimeUnit.SECONDS).until(() -> done[0]);
    }

    // Create token with invalid card CVN
    @Test
    public void test_createSingleUseToken_auth_invalid_card_cvn() {
        final boolean[] done = { false };
        Card card = new Card("4000000000001091",
            "12",
            "2030",
            "1234");
        TokenCallback callback = new TokenCallback() {
            @Override
            public void onSuccess(Token token) {
                fail();
            }

            @Override
            public void onError(XenditError xenditError) {
                System.out.println("Error " + xenditError.getErrorMessage());
                assertEquals(xenditError.getErrorMessage(), "Card cvn is invalid for this card type");
                done[0] = true;
            }
        };

        Xendit x = createXendit(
            new InterceptorTestCallback() {
                @Override
                public void interceptRequest(JsonObject jsonObj) {
                    // verify if the json object contains card_data
                    assertNotNull(jsonObj.get("card_data").getAsJsonObject().get("account_number"));
                    assertNotNull(jsonObj.get("card_data").getAsJsonObject().get("exp_year"));
                    assertNotNull(jsonObj.get("card_data").getAsJsonObject().get("exp_month"));
                    assertNotNull(jsonObj.get("card_data").getAsJsonObject().get("cvn"));
                }
                @Override
                public void interceptRequestFailed(String error) {
                    // should not be called
                    fail();
                }
                @Override
                public void interceptResponse(Object interceptedMessage) {
                    // should not be called
                    fail();
                }
                @Override
                public void interceptResponseFailed(String error) {
                    // should not be called
                    fail();
                }
            });
        x.createSingleUseToken(card, 10000, true, onBehalfOf, callback);

        Awaitility.await().atMost(10, TimeUnit.SECONDS).until(() -> done[0]);
    }

    // Create single use token without 3DS
    @Test
    public void test_createSingleUseToken_auth_without_3ds() {
        final boolean[] done = { false };
        Card card = new Card("4000000000001091",
            "12",
            "2030",
            "123");
        TokenCallback callback = new TokenCallback() {
            @Override
            public void onSuccess(Token token) {
                fail();
            }

            @Override
            public void onError(XenditError xenditError) {
                fail();
            }
        };

        Xendit x = createXendit(
            new InterceptorTestCallback() {
                @Override
                public void interceptRequest(JsonObject jsonObj) {
                    // verify if the json object contains card_data
                    assertEquals(jsonObj.get("should_authenticate").getAsString(), "true");
                    assertNotNull(jsonObj.get("card_data").getAsJsonObject().get("account_number"));
                    assertNotNull(jsonObj.get("card_data").getAsJsonObject().get("exp_year"));
                    assertNotNull(jsonObj.get("card_data").getAsJsonObject().get("exp_month"));
                    assertNotNull(jsonObj.get("card_data").getAsJsonObject().get("cvn"));
                }
                @Override
                public void interceptRequestFailed(String error) {
                    // should not be called
                    fail();
                }
                @Override
                public void interceptResponse(Object interceptedMessage) {
                    System.out.println("Intercepted " + interceptedMessage.getClass().getName());
                    AuthenticatedToken authenticatedToken = (AuthenticatedToken) interceptedMessage;
                    System.out.println("AuthenticatedToken " + authenticatedToken);
                    assertNotNull(authenticatedToken.getId());
                    assertEquals(authenticatedToken.getStatus(), "IN_REVIEW");
                    done[0] = true;
                }
                @Override
                public void interceptResponseFailed(String error) {
                    // should not be called
                    fail();
                }
            });
        x.createSingleUseToken(card, 10000, true, onBehalfOf, callback);

        Awaitility.await().atMost(10, TimeUnit.SECONDS).until(() -> done[0]);
    }

    // Create single use token with 3DS
    @Test
    public void test_createSingleUseToken_auth_with_3ds() {
        final boolean[] done = { false };
        Card card = new Card("4000000000001091",
            "12",
            "2030",
            "123");
        TokenCallback callback = new TokenCallback() {
            @Override
            public void onSuccess(Token token) {
                fail();
            }

            @Override
            public void onError(XenditError xenditError) {
                fail();
            }
        };

        Xendit x = createXendit(
            new InterceptorTestCallback() {
                @Override
                public void interceptRequest(JsonObject jsonObj) {
                    // verify if the json object contains card_data
                    assertEquals(jsonObj.get("should_authenticate").getAsString(), "false");
                    assertNotNull(jsonObj.get("card_data").getAsJsonObject().get("account_number"));
                    assertNotNull(jsonObj.get("card_data").getAsJsonObject().get("exp_year"));
                    assertNotNull(jsonObj.get("card_data").getAsJsonObject().get("exp_month"));
                    assertNotNull(jsonObj.get("card_data").getAsJsonObject().get("cvn"));
                }
                @Override
                public void interceptRequestFailed(String error) {
                    // should not be called
                    fail();
                }
                @Override
                public void interceptResponse(Object interceptedMessage) {
                    System.out.println("Intercepted " + interceptedMessage.getClass().getName());
                    AuthenticatedToken authenticatedToken = (AuthenticatedToken) interceptedMessage;
                    System.out.println("AuthenticatedToken " + authenticatedToken);
                    assertNotNull(authenticatedToken.getId());
                    assertEquals(authenticatedToken.getStatus(), "VERIFIED");
                    done[0] = true;
                }
                @Override
                public void interceptResponseFailed(String error) {
                    // should not be called
                    fail();
                }
            });
        x.createSingleUseToken(card, 10000, false, onBehalfOf, callback);

        Awaitility.await().atMost(10, TimeUnit.SECONDS).until(() -> done[0]);
    }

    // Create single use token with currency
    @Test
    public void test_createSingleUseToken_auth_with_currency() {
        final boolean[] done = { false };
        Card card = new Card("4000000000001091",
            "12",
            "2030",
            "123");
        TokenCallback callback = new TokenCallback() {
            @Override
            public void onSuccess(Token token) {
                fail();
            }

            @Override
            public void onError(XenditError xenditError) {
                fail();
            }
        };

        Xendit x = createXendit(
            new InterceptorTestCallback() {
                @Override
                public void interceptRequest(JsonObject jsonObj) {
                    // verify if the json object contains card_data
                    assertEquals(jsonObj.get("should_authenticate").getAsString(), "true");
                    assertEquals(jsonObj.get("currency").getAsString(), "IDR");
                    assertNotNull(jsonObj.get("card_data").getAsJsonObject().get("account_number"));
                    assertNotNull(jsonObj.get("card_data").getAsJsonObject().get("exp_year"));
                    assertNotNull(jsonObj.get("card_data").getAsJsonObject().get("exp_month"));
                    assertNotNull(jsonObj.get("card_data").getAsJsonObject().get("cvn"));
                }
                @Override
                public void interceptRequestFailed(String error) {
                    // should not be called
                    fail();
                }
                @Override
                public void interceptResponse(Object interceptedMessage) {
                    System.out.println("Intercepted " + interceptedMessage.getClass().getName());
                    AuthenticatedToken authenticatedToken = (AuthenticatedToken) interceptedMessage;
                    System.out.println("AuthenticatedToken " + authenticatedToken);
                    assertNotNull(authenticatedToken.getId());
                    assertEquals(authenticatedToken.getStatus(), "IN_REVIEW");
                    done[0] = true;
                }
                @Override
                public void interceptResponseFailed(String error) {
                    // should not be called
                    fail();
                }
            });
        x.createSingleUseToken(card, "10000", true, "IDR", callback);

        Awaitility.await().atMost(10, TimeUnit.SECONDS).until(() -> done[0]);
    }

    // Create single use token with unsupported currency
    @Test
    public void test_createSingleUseToken_auth_with_unsupported_currency() {
        final boolean[] done = { false };
        Card card = new Card("4000000000001091",
            "12",
            "2030",
            "123");
        TokenCallback callback = new TokenCallback() {
            @Override
            public void onSuccess(Token token) {
                fail();
            }

            @Override
            public void onError(XenditError xenditError) {
                fail();
            }
        };

        Xendit x = createXendit(
            new InterceptorTestCallback() {
                @Override
                public void interceptRequest(JsonObject jsonObj) {
                    // verify if the json object contains card_data
                    assertEquals(jsonObj.get("should_authenticate").getAsString(), "true");
                    assertEquals(jsonObj.get("currency").getAsString(), "GBP");
                    assertNotNull(jsonObj.get("card_data").getAsJsonObject().get("account_number"));
                    assertNotNull(jsonObj.get("card_data").getAsJsonObject().get("exp_year"));
                    assertNotNull(jsonObj.get("card_data").getAsJsonObject().get("exp_month"));
                    assertNotNull(jsonObj.get("card_data").getAsJsonObject().get("cvn"));
                }
                @Override
                public void interceptRequestFailed(String error) {
                    // should not be called
                    fail();
                }
                @Override
                public void interceptResponse(Object interceptedMessage) {
                    // should not be called
                    fail();
                }
                @Override
                public void interceptResponseFailed(String error) {
                    System.out.println("Error " + error);
                    done[0] = true;
                }
            });
        x.createSingleUseToken(card, "10000", true, "GBP", callback);

        Awaitility.await().atMost(10, TimeUnit.SECONDS).until(() -> done[0]);
    }

    // Create single use token with invalid currency
    @Test
    public void test_createSingleUseToken_auth_with_invalid_currency() {
        final boolean[] done = { false };
        Card card = new Card("4000000000001091",
            "12",
            "2030",
            "123");
        TokenCallback callback = new TokenCallback() {
            @Override
            public void onSuccess(Token token) {
                fail();
            }

            @Override
            public void onError(XenditError xenditError) {
                fail();
            }
        };

        Xendit x = createXendit(
            new InterceptorTestCallback() {
                @Override
                public void interceptRequest(JsonObject jsonObj) {
                    // verify if the json object contains card_data
                    assertEquals(jsonObj.get("should_authenticate").getAsString(), "true");
                    assertEquals(jsonObj.get("currency").getAsString(), "ZZZ");
                    assertNotNull(jsonObj.get("card_data").getAsJsonObject().get("account_number"));
                    assertNotNull(jsonObj.get("card_data").getAsJsonObject().get("exp_year"));
                    assertNotNull(jsonObj.get("card_data").getAsJsonObject().get("exp_month"));
                    assertNotNull(jsonObj.get("card_data").getAsJsonObject().get("cvn"));
                }
                @Override
                public void interceptRequestFailed(String error) {
                    // should not be called
                    fail();
                }
                @Override
                public void interceptResponse(Object interceptedMessage) {
                    // should not be called
                    fail();
                }
                @Override
                public void interceptResponseFailed(String error) {
                    System.out.println("Error " + error);
                    done[0] = true;
                }
            });
        x.createSingleUseToken(card, "10000", true, "ZZZ", callback);

        Awaitility.await().atMost(10, TimeUnit.SECONDS).until(() -> done[0]);
    }

    // Create single use token without cardData.CVN
    @Test
    public void test_createSingleUseToken_auth_without_cardData_cvn() {
        final boolean[] done = { false };
        Card card = new Card("4000000000001091",
            "12",
            "2030",
            "");
        TokenCallback callback = new TokenCallback() {
            @Override
            public void onSuccess(Token token) {
                fail();
            }

            @Override
            public void onError(XenditError xenditError) {
                System.out.println("Error " + xenditError.getErrorMessage());
                assertEquals(xenditError.getErrorMessage(), "Card CVN is invalid");
                done[0] = true;
            }
        };

        Xendit x = createXendit(
            new InterceptorTestCallback() {
                @Override
                public void interceptRequest(JsonObject jsonObj) {
                    // verify if the json object contains card_data
                    assertEquals(jsonObj.get("should_authenticate").getAsString(), "true");
                    assertNotNull(jsonObj.get("card_data").getAsJsonObject().get("account_number"));
                    assertNotNull(jsonObj.get("card_data").getAsJsonObject().get("exp_year"));
                    assertNotNull(jsonObj.get("card_data").getAsJsonObject().get("exp_month"));
                    assertEquals(jsonObj.get("card_data").getAsJsonObject().get("cvn").toString(), "");
                }
                @Override
                public void interceptRequestFailed(String error) {
                    // should not be called
                    fail();
                }
                @Override
                public void interceptResponse(Object interceptedMessage) {
                    // should not be called
                    fail();
                }
                @Override
                public void interceptResponseFailed(String error) {
                    // should not be called
                    fail();
                }
            });
        x.createSingleUseToken(card, 10000, true, onBehalfOf, callback);

        Awaitility.await().atMost(10, TimeUnit.SECONDS).until(() -> done[0]);
    }

    //Create single use token with complete set of cardData object
    @Test
    public void test_createSingleUseToken_auth_with_cardData() {
        final boolean[] done = { false };
        Card card = new Card("4000000000001091",
            "12",
            "2030",
            "123",
            new CardHolderData("John", "Doe", "johndoe@gmail.com", "+12345678")
            );
        TokenCallback callback = new TokenCallback() {
            @Override
            public void onSuccess(Token token) {
                fail();
            }

            @Override
            public void onError(XenditError xenditError) {
                fail();
            }
        };

        Xendit x = createXendit(
            new InterceptorTestCallback() {
                @Override
                public void interceptRequest(JsonObject jsonObj) {
                    // verify if the json object contains card_data
                    assertEquals(jsonObj.get("should_authenticate").getAsString(), "true");
                    assertEquals(jsonObj.get("currency").getAsString(), "IDR");

                    assertNotNull(jsonObj.get("card_data").getAsJsonObject().get("account_number"));
                    assertNotNull(jsonObj.get("card_data").getAsJsonObject().get("exp_year"));
                    assertNotNull(jsonObj.get("card_data").getAsJsonObject().get("exp_month"));
                    assertNotNull(jsonObj.get("card_data").getAsJsonObject().get("cvn"));
                    assertNotNull(jsonObj.get("card_data").getAsJsonObject().get("card_holder_first_name"));
                    assertNotNull(jsonObj.get("card_data").getAsJsonObject().get("card_holder_last_name"));
                    assertNotNull(jsonObj.get("card_data").getAsJsonObject().get("card_holder_email"));
                    assertNotNull(jsonObj.get("card_data").getAsJsonObject().get("card_holder_phone_number"));
                }
                @Override
                public void interceptRequestFailed(String error) {
                    // should not be called
                    fail();
                }
                @Override
                public void interceptResponse(Object interceptedMessage) {
                    System.out.println("Intercepted " + interceptedMessage.getClass().getName());
                    AuthenticatedToken authenticatedToken = (AuthenticatedToken) interceptedMessage;
                    System.out.println("AuthenticatedToken " + authenticatedToken);
                    assertNotNull(authenticatedToken.getId());
                    assertEquals(authenticatedToken.getStatus(), "IN_REVIEW");
                    done[0] = true;
                }
                @Override
                public void interceptResponseFailed(String error) {
                    // should not be called
                    fail();
                }
            });


        x.createSingleUseToken(card, "10000", true, "IDR", callback);

        Awaitility.await().atMost(10, TimeUnit.SECONDS).until(() -> done[0]);
    }

    // Create single use token with only one set of cardData object
    //- cardData.cardHolderFirstName
    @Test
    public void test_createSingleUseToken_auth_with_cardData_cardHolderFirstName() {
        final boolean[] done = { false };
        Card card = new Card("4000000000001091",
            "12",
            "2030",
            "123",
            new CardHolderData("John", null,null, null)
        );
        TokenCallback callback = new TokenCallback() {
            @Override
            public void onSuccess(Token token) {
                fail();
            }

            @Override
            public void onError(XenditError xenditError) {
                fail();
            }
        };

        Xendit x = createXendit(
            new InterceptorTestCallback() {
                @Override
                public void interceptRequest(JsonObject jsonObj) {
                    // verify if the json object contains card_data
                    assertEquals(jsonObj.get("should_authenticate").getAsString(), "true");
                    assertEquals(jsonObj.get("currency").getAsString(), "IDR");

                    assertNotNull(jsonObj.get("card_data").getAsJsonObject().get("account_number"));
                    assertNotNull(jsonObj.get("card_data").getAsJsonObject().get("exp_year"));
                    assertNotNull(jsonObj.get("card_data").getAsJsonObject().get("exp_month"));
                    assertNotNull(jsonObj.get("card_data").getAsJsonObject().get("cvn"));

                    assertNotNull(jsonObj.get("card_data").getAsJsonObject().get("card_holder_first_name"));
                    assertNull(jsonObj.get("card_data").getAsJsonObject().get("card_holder_last_name"));
                    assertNull(jsonObj.get("card_data").getAsJsonObject().get("card_holder_email"));
                    assertNull(jsonObj.get("card_data").getAsJsonObject().get("card_holder_phone_number"));
                }
                @Override
                public void interceptRequestFailed(String error) {
                    // should not be called
                    fail();
                }
                @Override
                public void interceptResponse(Object interceptedMessage) {
                    fail();
                }
                @Override
                public void interceptResponseFailed(String error) {
                    // should not be called
                    System.out.println("Error " + error);
                    done[0] = true;
                }
            });


        x.createSingleUseToken(card, "10000", true, "IDR", callback);

        Awaitility.await().atMost(10, TimeUnit.SECONDS).until(() -> done[0]);
    }

    // Create single use token with only one set of cardData object
    //- cardData.cardHolderLastName
    @Test
    public void test_createSingleUseToken_auth_with_cardData_cardHolderLastName() {
        final boolean[] done = { false };
        Card card = new Card("4000000000001091",
            "12",
            "2030",
            "123",
            new CardHolderData(null, "Doe",null, null)
        );
        TokenCallback callback = new TokenCallback() {
            @Override
            public void onSuccess(Token token) {
                fail();
            }

            @Override
            public void onError(XenditError xenditError) {
                fail();
            }
        };

        Xendit x = createXendit(
            new InterceptorTestCallback() {
                @Override
                public void interceptRequest(JsonObject jsonObj) {
                    // verify if the json object contains card_data
                    assertEquals(jsonObj.get("should_authenticate").getAsString(), "true");
                    assertEquals(jsonObj.get("currency").getAsString(), "IDR");

                    assertNotNull(jsonObj.get("card_data").getAsJsonObject().get("account_number"));
                    assertNotNull(jsonObj.get("card_data").getAsJsonObject().get("exp_year"));
                    assertNotNull(jsonObj.get("card_data").getAsJsonObject().get("exp_month"));
                    assertNotNull(jsonObj.get("card_data").getAsJsonObject().get("cvn"));

                    assertNull(jsonObj.get("card_data").getAsJsonObject().get("card_holder_first_name"));
                    assertNotNull(jsonObj.get("card_data").getAsJsonObject().get("card_holder_last_name"));
                    assertNull(jsonObj.get("card_data").getAsJsonObject().get("card_holder_email"));
                    assertNull(jsonObj.get("card_data").getAsJsonObject().get("card_holder_phone_number"));
                }
                @Override
                public void interceptRequestFailed(String error) {
                    // should not be called
                    fail();
                }
                @Override
                public void interceptResponse(Object interceptedMessage) {
                    fail();
                }
                @Override
                public void interceptResponseFailed(String error) {
                    // should not be called
                    System.out.println("Error " + error);
                    done[0] = true;
                }
            });


        x.createSingleUseToken(card, "10000", true, "IDR", callback);

        Awaitility.await().atMost(10, TimeUnit.SECONDS).until(() -> done[0]);
    }

    //Create single use token with only one set of cardData object
    //- cardData.cardHolderEmail
    @Test
    public void test_createSingleUseToken_auth_with_cardData_cardHolderEmail() {
        final boolean[] done = { false };
        Card card = new Card("4000000000001091",
            "12",
            "2030",
            "123",
            new CardHolderData(null, null, "johndoe@gmail.com", null)
        );
        TokenCallback callback = new TokenCallback() {
            @Override
            public void onSuccess(Token token) {
                fail();
            }

            @Override
            public void onError(XenditError xenditError) {
                fail();
            }
        };

        Xendit x = createXendit(
            new InterceptorTestCallback() {
                @Override
                public void interceptRequest(JsonObject jsonObj) {
                    // verify if the json object contains card_data
                    assertEquals(jsonObj.get("should_authenticate").getAsString(), "true");
                    assertEquals(jsonObj.get("currency").getAsString(), "IDR");

                    assertNotNull(jsonObj.get("card_data").getAsJsonObject().get("account_number"));
                    assertNotNull(jsonObj.get("card_data").getAsJsonObject().get("exp_year"));
                    assertNotNull(jsonObj.get("card_data").getAsJsonObject().get("exp_month"));
                    assertNotNull(jsonObj.get("card_data").getAsJsonObject().get("cvn"));
                    assertNull(jsonObj.get("card_data").getAsJsonObject().get("card_holder_first_name"));
                    assertNull(jsonObj.get("card_data").getAsJsonObject().get("card_holder_last_name"));
                    assertNotNull(jsonObj.get("card_data").getAsJsonObject().get("card_holder_email"));
                    assertNull(jsonObj.get("card_data").getAsJsonObject().get("card_holder_phone_number"));
                }
                @Override
                public void interceptRequestFailed(String error) {
                    // should not be called
                    fail();
                }
                @Override
                public void interceptResponse(Object interceptedMessage) {
                    System.out.println("Intercepted " + interceptedMessage.getClass().getName());
                    AuthenticatedToken authenticatedToken = (AuthenticatedToken) interceptedMessage;
                    System.out.println("AuthenticatedToken " + authenticatedToken);
                    assertNotNull(authenticatedToken.getId());
                    assertEquals(authenticatedToken.getStatus(), "IN_REVIEW");
                    done[0] = true;
                }
                @Override
                public void interceptResponseFailed(String error) {
                    // should not be called
                    fail();
                }
            });


        x.createSingleUseToken(card, "10000", true, "IDR", callback);

        Awaitility.await().atMost(10, TimeUnit.SECONDS).until(() -> done[0]);
    }

    // Create single use token with complete set of cardData object
    //- cardData.cardHolderPhoneNumber
    @Test
    public void test_createSingleUseToken_auth_with_cardData_cardHolderPhoneNumber() {
        final boolean[] done = { false };
        Card card = new Card("4000000000001091",
            "12",
            "2030",
            "123",
            new CardHolderData(null, null,null, "+12345678")
        );
        TokenCallback callback = new TokenCallback() {
            @Override
            public void onSuccess(Token token) {
                fail();
            }

            @Override
            public void onError(XenditError xenditError) {
                fail();
            }
        };

        Xendit x = createXendit(
            new InterceptorTestCallback() {
                @Override
                public void interceptRequest(JsonObject jsonObj) {
                    // verify if the json object contains card_data
                    assertEquals(jsonObj.get("should_authenticate").getAsString(), "true");
                    assertEquals(jsonObj.get("currency").getAsString(), "IDR");

                    assertNotNull(jsonObj.get("card_data").getAsJsonObject().get("account_number"));
                    assertNotNull(jsonObj.get("card_data").getAsJsonObject().get("exp_year"));
                    assertNotNull(jsonObj.get("card_data").getAsJsonObject().get("exp_month"));
                    assertNotNull(jsonObj.get("card_data").getAsJsonObject().get("cvn"));

                    assertNull(jsonObj.get("card_data").getAsJsonObject().get("card_holder_first_name"));
                    assertNull(jsonObj.get("card_data").getAsJsonObject().get("card_holder_last_name"));
                    assertNull(jsonObj.get("card_data").getAsJsonObject().get("card_holder_email"));
                    assertNotNull(jsonObj.get("card_data").getAsJsonObject().get("card_holder_phone_number"));
                }
                @Override
                public void interceptRequestFailed(String error) {
                    // should not be called
                    fail();
                }
                @Override
                public void interceptResponse(Object interceptedMessage) {
                    fail();
                }
                @Override
                public void interceptResponseFailed(String error) {
                    // should not be called
                    System.out.println("Error " + error);
                    done[0] = true;
                }
            });


        x.createSingleUseToken(card, "10000", true, "IDR", callback);

        Awaitility.await().atMost(10, TimeUnit.SECONDS).until(() -> done[0]);
    }

    // Create single use token without amount
    @Test
    public void test_createSingleUseToken_auth_without_amount() {
        final boolean[] done = { false };
        Card card = new Card("4000000000001091",
            "12",
            "2030",
            "123"
        );
        TokenCallback callback = new TokenCallback() {
            @Override
            public void onSuccess(Token token) {
                fail();
            }

            @Override
            public void onError(XenditError xenditError) {
                fail();
            }
        };

        Xendit x = createXendit(
            new InterceptorTestCallback() {
                @Override
                public void interceptRequest(JsonObject jsonObj) {
                    // verify if the json object contains card_data
                    assertEquals(jsonObj.get("should_authenticate").getAsString(), "true");
                    assertEquals(jsonObj.get("currency").getAsString(), "IDR");

                    assertNull(jsonObj.get("amount"));

                    assertNotNull(jsonObj.get("card_data").getAsJsonObject().get("account_number"));
                    assertNotNull(jsonObj.get("card_data").getAsJsonObject().get("exp_year"));
                    assertNotNull(jsonObj.get("card_data").getAsJsonObject().get("exp_month"));
                    assertNotNull(jsonObj.get("card_data").getAsJsonObject().get("cvn"));
                }
                @Override
                public void interceptRequestFailed(String error) {
                    // should not be called
                    fail();
                }
                @Override
                public void interceptResponse(Object interceptedMessage) {
                    // should not be called
                    fail();

                }
                @Override
                public void interceptResponseFailed(String error) {
                    System.out.println("Error " + error);
                    done[0] = true;
                }
            });


        x.createSingleUseToken(card, null, true, "IDR", callback);

        Awaitility.await().atMost(10, TimeUnit.SECONDS).until(() -> done[0]);
    }

    // Create single use token with mid_label
    @Test
    public void test_createSingleUseToken_auth_with_mid_label() {
        final boolean[] done = { false };
        Card card = new Card("4000000000001091",
            "12",
            "2030",
            "123"
        );
        TokenCallback callback = new TokenCallback() {
            @Override
            public void onSuccess(Token token) {
                fail();
            }

            @Override
            public void onError(XenditError xenditError) {
                fail();
            }
        };

        Xendit x = createXendit(
            new InterceptorTestCallback() {
                @Override
                public void interceptRequest(JsonObject jsonObj) {
                    // verify if the json object contains card_data
                    assertEquals(jsonObj.get("should_authenticate").getAsString(), "true");
                    assertEquals(jsonObj.get("currency").getAsString(), "IDR");

                    assertNotNull(jsonObj.get("mid_label"));

                    assertNotNull(jsonObj.get("card_data").getAsJsonObject().get("account_number"));
                    assertNotNull(jsonObj.get("card_data").getAsJsonObject().get("exp_year"));
                    assertNotNull(jsonObj.get("card_data").getAsJsonObject().get("exp_month"));
                    assertNotNull(jsonObj.get("card_data").getAsJsonObject().get("cvn"));
                }
                @Override
                public void interceptRequestFailed(String error) {
                    // should not be called
                    fail();
                }
                @Override
                public void interceptResponse(Object interceptedMessage) {
                    // should not be called
                    fail();

                }
                @Override
                public void interceptResponseFailed(String error) {
                    System.out.println("Error " + error);
                    done[0] = true;
                }
            });


        x.createSingleUseToken(card, "10000", true, onBehalfOf,  null, null, "IDR", "RANDOM", callback);

        Awaitility.await().atMost(10, TimeUnit.SECONDS).until(() -> done[0]);
    }

    // Create single use token with billingDetails
    @Test
    public void test_createSingleUseToken_auth_with_billingDetails() {
        final boolean[] done = { false };
        Card card = new Card("4000000000001091",
            "12",
            "2030",
            "123"
        );
        TokenCallback callback = new TokenCallback() {
            @Override
            public void onSuccess(Token token) {
                fail();
            }

            @Override
            public void onError(XenditError xenditError) {
                fail();
            }
        };

        Xendit x = createXendit(
            new InterceptorTestCallback() {
                @Override
                public void interceptRequest(JsonObject jsonObj) {
                    // verify if the json object contains card_data
                    assertEquals(jsonObj.get("should_authenticate").getAsString(), "true");
                    assertEquals(jsonObj.get("currency").getAsString(), "IDR");

                    assertNotNull(jsonObj.get("card_data").getAsJsonObject().get("account_number"));
                    assertNotNull(jsonObj.get("card_data").getAsJsonObject().get("exp_year"));
                    assertNotNull(jsonObj.get("card_data").getAsJsonObject().get("exp_month"));
                    assertNotNull(jsonObj.get("card_data").getAsJsonObject().get("cvn"));

                    assertNotNull(jsonObj.get("billing_details").getAsJsonObject().get("given_names"));
                    assertNotNull(jsonObj.get("billing_details").getAsJsonObject().get("surname"));
                    assertNotNull(jsonObj.get("billing_details").getAsJsonObject().get("email"));
                    assertNotNull(jsonObj.get("billing_details").getAsJsonObject().get("mobile_number"));
                    assertNotNull(jsonObj.get("billing_details").getAsJsonObject().get("address").getAsJsonObject().get("country"));
                    assertNotNull(jsonObj.get("billing_details").getAsJsonObject().get("address").getAsJsonObject().get("street_line1"));
                    assertNotNull(jsonObj.get("billing_details").getAsJsonObject().get("address").getAsJsonObject().get("street_line2"));
                    assertNotNull(jsonObj.get("billing_details").getAsJsonObject().get("address").getAsJsonObject().get("city"));
                }
                @Override
                public void interceptRequestFailed(String error) {
                    // should not be called
                    fail();
                }
                @Override
                public void interceptResponse(Object interceptedMessage) {
                    System.out.println("Intercepted " + interceptedMessage.getClass().getName());
                    AuthenticatedToken authenticatedToken = (AuthenticatedToken) interceptedMessage;
                    System.out.println("AuthenticatedToken " + authenticatedToken);
                    assertNotNull(authenticatedToken.getId());
                    assertEquals(authenticatedToken.getStatus(), "IN_REVIEW");
                    done[0] = true;
                }
                @Override
                public void interceptResponseFailed(String error) {
                    // should not be called
                    fail();
                }
            });

        BillingDetails billingDetails = new BillingDetails();
        billingDetails.setGivenNames("John");
        billingDetails.setSurname("Doe");
        billingDetails.setEmail("john.doe@gmail.com");
        billingDetails.setMobileNumber("+12345678");
        Address address = new Address();
        address.setCountry("CA");
        address.setStreetLine1("California");
        address.setStreetLine2("random st");
        address.setCity("14045");
        billingDetails.setAddress(address);

        x.createSingleUseToken(card, "10000", true, onBehalfOf, billingDetails, null, "IDR", callback);

        Awaitility.await().atMost(10, TimeUnit.SECONDS).until(() -> done[0]);
    }

    // Create multipe use token without CVN
    // Remark: Not possible to test this case
    @Test
    public void test_createMultipleUseToken_auth_without_cvn() {
        //final boolean[] done = { false };
        //Card card = new Card("4000000000001091",
        //    "12",
        //    "2030",
        //    "");
        //TokenCallback callback = new TokenCallback() {
        //    @Override
        //    public void onSuccess(Token token) {
        //        fail();
        //    }
        //
        //    @Override
        //    public void onError(XenditError xenditError) {
        //        System.out.println("Error " + xenditError.getErrorMessage());
        //        fail();
        //    }
        //};
        //
        //Xendit x = createXendit(
        //    new InterceptorTestCallback() {
        //        @Override
        //        public void interceptRequest(JsonObject jsonObj) {
        //            // verify if the json object contains card_data
        //            assertEquals(jsonObj.get("should_authenticate").getAsString(), "true");
        //            assertEquals(jsonObj.get("is_single_use").getAsString(), "true");
        //
        //            assertNotNull(jsonObj.get("card_data").getAsJsonObject().get("account_number"));
        //            assertNotNull(jsonObj.get("card_data").getAsJsonObject().get("exp_year"));
        //            assertNotNull(jsonObj.get("card_data").getAsJsonObject().get("exp_month"));
        //            assertEquals(jsonObj.get("card_data").getAsJsonObject().get("cvn").getAsString(), "");
        //
        //
        //        }
        //        @Override
        //        public void interceptRequestFailed(String error) {
        //            // should not be called
        //            fail();
        //        }
        //        @Override
        //        public void interceptResponse(Object interceptedMessage) {
        //            System.out.println("Intercepted " + interceptedMessage.getClass().getName());
        //            AuthenticatedToken authenticatedToken = (AuthenticatedToken) interceptedMessage;
        //            System.out.println("AuthenticatedToken " + authenticatedToken);
        //            assertNotNull(authenticatedToken.getId());
        //            assertEquals(authenticatedToken.getStatus(), "IN_REVIEW");
        //            done[0] = true;
        //        }
        //        @Override
        //        public void interceptResponseFailed(String error) {
        //            // should not be called
        //            fail();
        //        }
        //    });
        //x.createMultipleUseToken(card, onBehalfOf, callback);
        //
        //Awaitility.await().atMost(10, TimeUnit.SECONDS).until(() -> done[0]);
    }

    // Create multiple use token with CVN
    @Test
    public void test_createMultipleUseToken_auth_with_cvn() {
        final boolean[] done = { false };
        Card card = new Card("4000000000001091",
            "12",
            "2030",
            "123");
        TokenCallback callback = new TokenCallback() {
            @Override
            public void onSuccess(Token token) {
                fail();
            }

            @Override
            public void onError(XenditError xenditError) {
                System.out.println("Error " + xenditError.getErrorMessage());
                fail();
            }
        };

        Xendit x = createXendit(
            new InterceptorTestCallback() {
                @Override
                public void interceptRequest(JsonObject jsonObj) {
                    // verify if the json object contains card_data
                    assertEquals(jsonObj.get("should_authenticate").getAsString(), "false");
                    assertEquals(jsonObj.get("is_single_use").getAsString(), "false");

                    assertNotNull(jsonObj.get("card_data").getAsJsonObject().get("account_number"));
                    assertNotNull(jsonObj.get("card_data").getAsJsonObject().get("exp_year"));
                    assertNotNull(jsonObj.get("card_data").getAsJsonObject().get("exp_month"));
                    assertNotNull(jsonObj.get("card_data").getAsJsonObject().get("cvn"));


                }
                @Override
                public void interceptRequestFailed(String error) {
                    // should not be called
                    fail();
                }
                @Override
                public void interceptResponse(Object interceptedMessage) {
                    System.out.println("Intercepted " + interceptedMessage.getClass().getName());
                    AuthenticatedToken authenticatedToken = (AuthenticatedToken) interceptedMessage;
                    System.out.println("AuthenticatedToken " + authenticatedToken);
                    assertNotNull(authenticatedToken.getId());
                    assertEquals(authenticatedToken.getStatus(), "VERIFIED");
                    done[0] = true;
                }
                @Override
                public void interceptResponseFailed(String error) {
                    // should not be called
                    fail();
                }
            });
        x.createMultipleUseToken(card, onBehalfOf, callback);

        Awaitility.await().atMost(10, TimeUnit.SECONDS).until(() -> done[0]);
    }

    // Create single use token with mid_label
    @Test
    public void test_createMultipleUseToken_auth_with_mid_label() {
        final boolean[] done = { false };
        Card card = new Card("4000000000001091",
            "12",
            "2030",
            "123");
        TokenCallback callback = new TokenCallback() {
            @Override
            public void onSuccess(Token token) {
                fail();
            }

            @Override
            public void onError(XenditError xenditError) {
                System.out.println("Error " + xenditError.getErrorMessage());
                fail();
            }
        };

        Xendit x = createXendit(
            new InterceptorTestCallback() {
                @Override
                public void interceptRequest(JsonObject jsonObj) {
                    // verify if the json object contains card_data
                    assertEquals(jsonObj.get("should_authenticate").getAsString(), "false");
                    assertEquals(jsonObj.get("is_single_use").getAsString(), "false");

                    assertNotNull(jsonObj.get("mid_label"));

                    assertNotNull(jsonObj.get("card_data").getAsJsonObject().get("account_number"));
                    assertNotNull(jsonObj.get("card_data").getAsJsonObject().get("exp_year"));
                    assertNotNull(jsonObj.get("card_data").getAsJsonObject().get("exp_month"));
                    assertNotNull(jsonObj.get("card_data").getAsJsonObject().get("cvn"));


                }
                @Override
                public void interceptRequestFailed(String error) {
                    // should not be called
                    fail();
                }
                @Override
                public void interceptResponse(Object interceptedMessage) {
                    // should not be called
                    fail();
                }
                @Override
                public void interceptResponseFailed(String error) {
                    System.out.println("Error " + error);
                    done[0] = true;
                }
            });
        x.createMultipleUseToken(card, onBehalfOf, null, null, "RANDOM", callback);

        Awaitility.await().atMost(10, TimeUnit.SECONDS).until(() -> done[0]);
    }
}