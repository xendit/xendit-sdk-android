package com.xendit;

import android.content.Context;
import android.os.Debug;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;

import com.android.volley.AuthFailureError;
import com.android.volley.NoConnectionError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.xendit.Models.AuthenticatedToken;
import com.xendit.Models.Authentication;
import com.xendit.Models.BillingDetails;
import com.xendit.Models.Card;
import com.xendit.Models.CardHolderData;
import com.xendit.Models.Customer;
import com.xendit.Models.Token;
import com.xendit.Models.XenditError;

import com.xendit.interceptor.Interceptor;
import com.xendit.network.BaseRequest;
import com.xendit.network.errors.AuthorisationError;
import com.xendit.network.errors.ConnectionError;
import com.xendit.network.errors.NetworkError;
import com.xendit.utils.StoreCVNCallback;
import java.io.UnsupportedEncodingException;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.awaitility.Awaitility;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(AndroidJUnit4.class)
@SmallTest
public class AuthTest {

    private final String onBehalfOf = "";
    private static final Gson gson = new GsonBuilder().create();
    private static final String PROTOCOL_CHARSET = "utf-8";
    static final String PUBLISHABLE_KEY = "xnd_public_development_O4uGfOR3gbOunJU4frcaHmLCYNLy8oQuknDm+R1r9G3S/b2lBQR+gQ==";


    public static Xendit createXendit(
        InterceptorTestWithURLCallback intercept
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
                    System.out.println("Intercepted " + interceptedMessage.getUrl());

                    intercept.interceptRequest(jsonObj, interceptedMessage.getUrl());
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
                NetworkError netError;
                if (error instanceof TimeoutError || error instanceof NoConnectionError) {
                    netError = new ConnectionError(error);
                } else if (error instanceof AuthFailureError) {
                    netError = new AuthorisationError(error);
                } else {
                    netError = new NetworkError(error);
                }
                XenditError err = new XenditError(netError);
                intercept.interceptResponseFailed(err.getErrorMessage());

            }
        });
    }

    public String createSingleUseToken_auth_with_3ds() {
        final boolean[] done = { false };
        final String[] authToken = { null };
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
            new InterceptorTestWithURLCallback() {
                @Override
                public void interceptRequest(JsonObject jsonObj, String url) {
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
                    authToken[0] = authenticatedToken.getId();
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

        return authToken[0];
    }

    public String createMultipleUseToken_auth_with_cvn() {
        final String[] result = { "" };
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
            new InterceptorTestWithURLCallback() {
                @Override
                public void interceptRequest(JsonObject jsonObj, String url) {
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
                    result[0] = authenticatedToken.getId();
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

        return result[0];
    }

    // Create authentication using a CREATED SINGLE USE token
    @Test
    public void test_createAuth_with_created_single_use_token() {
        String authToken = createSingleUseToken_auth_with_3ds();
        System.out.println("authToken " + authToken);
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

        Xendit x = createXendit(
            new InterceptorTestWithURLCallback() {
                @Override
                public void interceptRequest(JsonObject jsonObj, String url) {
                    // verify if the json object contains card_data
                    assertEquals(jsonObj.get("amount").getAsString(), "10000");
                    assertTrue(url.contains(authToken));
                }
                @Override
                public void interceptRequestFailed(String error) {
                    // should not be called
                    fail();
                }
                @Override
                public void interceptResponse(Object interceptedMessage) {
                    Authentication response = (Authentication) interceptedMessage;
                    assertEquals(response.getStatus(), "IN_REVIEW");
                    done[0] = true;
                }
                @Override
                public void interceptResponseFailed(String error) {
                    System.out.println("Error " + error);
                    // should not be called
                    fail();
                }
            });

        x.createAuthentication(authToken, 10000, callback);

        Awaitility.await().atMost(10, TimeUnit.SECONDS).until(() -> done[0]);
    }

    // Create authentication using a CREATED SINGLE USE token with CVN
    @Test
    public void test_createAuth_with_created_single_use_token_with_cvn() {
        String authToken = createSingleUseToken_auth_with_3ds();
        System.out.println("authToken " + authToken);
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

        Xendit x = createXendit(
            new InterceptorTestWithURLCallback() {
                @Override
                public void interceptRequest(JsonObject jsonObj, String url) {
                    // verify if the json object contains card_data
                    assertEquals(jsonObj.get("amount").getAsString(), "10000");
                    assertEquals(jsonObj.get("card_cvn").getAsString(), "123");
                    assertTrue(url.contains(authToken));
                }
                @Override
                public void interceptRequestFailed(String error) {
                    // should not be called
                    fail();
                }
                @Override
                public void interceptResponse(Object interceptedMessage) {
                    Authentication response = (Authentication) interceptedMessage;
                    assertEquals(response.getStatus(), "IN_REVIEW");
                    done[0] = true;
                }
                @Override
                public void interceptResponseFailed(String error) {
                    System.out.println("Error " + error);
                    fail();
                }
            });

        x.createAuthentication(authToken, "10000", "IDR", "123", callback);

        Awaitility.await().atMost(10, TimeUnit.SECONDS).until(() -> done[0]);
    }

    // Create authentication using a CREATED SINGLE USE token with currency
    @Test
    public void test_createAuth_with_created_single_use_token_with_currency() {
        String authToken = createSingleUseToken_auth_with_3ds();
        System.out.println("authToken " + authToken);
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

        Xendit x = createXendit(
            new InterceptorTestWithURLCallback() {
                @Override
                public void interceptRequest(JsonObject jsonObj, String url) {
                    // verify if the json object contains card_data
                    assertEquals(jsonObj.get("amount").getAsString(), "10000");
                    assertEquals(jsonObj.get("currency").getAsString(), "IDR");
                    assertTrue(url.contains(authToken));
                }
                @Override
                public void interceptRequestFailed(String error) {
                    // should not be called
                    fail();
                }
                @Override
                public void interceptResponse(Object interceptedMessage) {
                    Authentication response = (Authentication) interceptedMessage;
                    assertEquals(response.getStatus(), "IN_REVIEW");
                    done[0] = true;
                }
                @Override
                public void interceptResponseFailed(String error) {
                    System.out.println("Error " + error);
                    // should not be called
                    fail();
                }
            });

        x.createAuthentication(authToken, "10000", "IDR", "123", callback);

        Awaitility.await().atMost(10, TimeUnit.SECONDS).until(() -> done[0]);
    }

    // Create authentication using a CREATED SINGLE USE token with unsupported currency
    @Test
    public void test_createAuth_with_created_single_use_token_with_unsupported_currency() {
        String authToken = createSingleUseToken_auth_with_3ds();
        System.out.println("authToken " + authToken);
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

        Xendit x = createXendit(
            new InterceptorTestWithURLCallback() {
                @Override
                public void interceptRequest(JsonObject jsonObj, String url) {
                    // verify if the json object contains card_data
                    assertEquals(jsonObj.get("amount").getAsString(), "10000");
                    assertEquals(jsonObj.get("currency").getAsString(), "GBP");
                    assertTrue(url.contains(authToken));
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
                    assertEquals(error, "Invalid currency, your business cannot process transaction with this currency");
                }
            });

        x.createAuthentication(authToken, "10000", "GBP", "123", callback);

        Awaitility.await().atMost(10, TimeUnit.SECONDS).until(() -> done[0]);
    }

    //Create authentication using a CREATED SINGLE USE token with invalid currency
    @Test
    public void test_createAuth_with_created_single_use_token_with_invalid_currency() {
        String authToken = createSingleUseToken_auth_with_3ds();
        System.out.println("authToken " + authToken);
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

        Xendit x = createXendit(
            new InterceptorTestWithURLCallback() {
                @Override
                public void interceptRequest(JsonObject jsonObj, String url) {
                    // verify if the json object contains card_data
                    assertEquals(jsonObj.get("amount").getAsString(), "200");
                    assertEquals(jsonObj.get("currency").getAsString(), "ZZZ");
                    assertTrue(url.contains(authToken));
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
                    assertEquals(error, "Currency ZZZ is invalid");
                }
            });

        x.createAuthentication(authToken, "200", "ZZZ", "123", callback);

        Awaitility.await().atMost(10, TimeUnit.SECONDS).until(() -> done[0]);
    }

    // Create authentication using a CREATED MULTIPLE USE token
    @Test
    public void test_createAuth_with_created_multiple_use_token() {
        String authToken = createMultipleUseToken_auth_with_cvn();
        System.out.println("authToken " + authToken);
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

        Xendit x = createXendit(
            new InterceptorTestWithURLCallback() {
                @Override
                public void interceptRequest(JsonObject jsonObj, String url) {
                    // verify if the json object contains card_data
                    assertEquals(jsonObj.get("amount").getAsString(), "10000");
                    assertTrue(url.contains(authToken));
                }
                @Override
                public void interceptRequestFailed(String error) {
                    // should not be called
                    fail();
                }
                @Override
                public void interceptResponse(Object interceptedMessage) {
                    Authentication response = (Authentication) interceptedMessage;
                    System.out.println(response);
                    assertEquals(response.getStatus(), "IN_REVIEW");
                    done[0] = true;
                }
                @Override
                public void interceptResponseFailed(String error) {
                    System.out.println("Error " + error);
                    fail();
                }
            });

        x.createAuthentication(authToken, 10000, callback);

        Awaitility.await().atMost(10, TimeUnit.SECONDS).until(() -> done[0]);
    }

    // Create authentication using a CREATED MULTIPLE USE token with CVN
    @Test
    public void test_createAuth_with_created_multiple_use_token_with_cvn() {
        String authToken = createMultipleUseToken_auth_with_cvn();
        System.out.println("authToken " + authToken);
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

        Xendit x = createXendit(
            new InterceptorTestWithURLCallback() {
                @Override
                public void interceptRequest(JsonObject jsonObj, String url) {
                    // verify if the json object contains card_data
                    assertEquals(jsonObj.get("amount").getAsString(), "10000");
                    assertEquals(jsonObj.get("card_cvn").getAsString(), "123");
                    assertTrue(url.contains(authToken));
                }
                @Override
                public void interceptRequestFailed(String error) {
                    // should not be called
                    fail();
                }
                @Override
                public void interceptResponse(Object interceptedMessage) {
                    Authentication response = (Authentication) interceptedMessage;
                    System.out.println(response);
                    assertEquals(response.getStatus(), "IN_REVIEW");
                    done[0] = true;
                }
                @Override
                public void interceptResponseFailed(String error) {
                    System.out.println("Error " + error);
                    fail();
                }
            });

        x.createAuthentication(authToken, "10000", "IDR", "123", callback);

        Awaitility.await().atMost(10, TimeUnit.SECONDS).until(() -> done[0]);
    }

    // Create authentication using a CREATED MULTIPLE USE token with currency
    @Test
    public void test_createAuth_with_created_multiple_use_token_with_currency() {
        String authToken = createMultipleUseToken_auth_with_cvn();
        System.out.println("authToken " + authToken);
        final boolean[] done = { false };
        AuthenticationCallback callback = new AuthenticationCallback() {
            @Override public void onSuccess(Authentication authentication) {
                System.out.println("Success");
                fail();
            }

            @Override
            public void onError(XenditError xenditError) {
                System.out.println("Error " + xenditError.getErrorMessage());
                fail();
            }
        };

        Xendit x = createXendit(
            new InterceptorTestWithURLCallback() {
                @Override
                public void interceptRequest(JsonObject jsonObj, String url) {
                    // verify if the json object contains card_data
                    assertEquals(jsonObj.get("amount").getAsString(), "10000");
                    assertEquals(jsonObj.get("currency").getAsString(), "IDR");
                    assertEquals(jsonObj.get("card_cvn").getAsString(), "123");
                    assertTrue(url.contains(authToken));
                }
                @Override
                public void interceptRequestFailed(String error) {
                    // should not be called
                    fail();
                }
                @Override
                public void interceptResponse(Object interceptedMessage) {
                    Authentication response = (Authentication) interceptedMessage;
                    System.out.println(response);
                    assertEquals(response.getStatus(), "IN_REVIEW");
                    done[0] = true;
                }
                @Override
                public void interceptResponseFailed(String error) {
                    System.out.println("Error " + error);
                    fail();
                }
            });

        x.createAuthentication(authToken, "10000", "IDR", "123", callback);

        Awaitility.await().atMost(10, TimeUnit.SECONDS).until(() -> done[0]);
    }

    // Create authentication using a CREATED MULTIPLE USE token with unsupported currency
    @Test
    public void test_createAuth_with_created_multiple_use_token_with_unsupported_currency() {
        String authToken = createMultipleUseToken_auth_with_cvn();
        System.out.println("authToken " + authToken);
        final boolean[] done = { false };
        AuthenticationCallback callback = new AuthenticationCallback() {
            @Override public void onSuccess(Authentication authentication) {
                System.out.println("Success");
                fail();
            }

            @Override
            public void onError(XenditError xenditError) {
                System.out.println("Error " + xenditError.getErrorMessage());
                fail();
            }
        };

        Xendit x = createXendit(
            new InterceptorTestWithURLCallback() {
                @Override
                public void interceptRequest(JsonObject jsonObj, String url) {
                    // verify if the json object contains card_data
                    assertEquals(jsonObj.get("amount").getAsString(), "10000");
                    assertEquals(jsonObj.get("currency").getAsString(), "GBP");
                    assertEquals(jsonObj.get("card_cvn").getAsString(), "123");
                    assertTrue(url.contains(authToken));
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
                    assertEquals(error, "Invalid currency, your business cannot process transaction with this currency");
                    done[0] = true;
                }
            });

        x.createAuthentication(authToken, "10000", "GBP", "123", callback);

        Awaitility.await().atMost(10, TimeUnit.SECONDS).until(() -> done[0]);
    }

    // Create authentication using a CREATED MULTIPLE USE token with invalid currency
    @Test
    public void test_createAuth_with_created_multiple_use_token_with_invalid_currency() {
        String authToken = createMultipleUseToken_auth_with_cvn();
        System.out.println("authToken " + authToken);
        final boolean[] done = { false };
        AuthenticationCallback callback = new AuthenticationCallback() {
            @Override public void onSuccess(Authentication authentication) {
                System.out.println("Success");
                fail();
            }

            @Override
            public void onError(XenditError xenditError) {
                System.out.println("Error " + xenditError.getErrorMessage());
                fail();
            }
        };

        Xendit x = createXendit(
            new InterceptorTestWithURLCallback() {
                @Override
                public void interceptRequest(JsonObject jsonObj, String url) {
                    // verify if the json object contains card_data
                    assertEquals(jsonObj.get("amount").getAsString(), "10000");
                    assertEquals(jsonObj.get("currency").getAsString(), "ZZZ");
                    assertEquals(jsonObj.get("card_cvn").getAsString(), "123");
                    assertTrue(url.contains(authToken));
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
                    assertEquals(error, "Currency ZZZ is invalid");
                    done[0] = true;
                }
            });

        x.createAuthentication(authToken, "10000", "ZZZ", "123", callback);

        Awaitility.await().atMost(10, TimeUnit.SECONDS).until(() -> done[0]);
    }

    // Create authentication using a CREATED MULTIPLE USE token with complete set of cardData object
    @Test
    public void test_createAuth_with_created_multiple_use_token_with_complete_cardData() {
        String authToken = createMultipleUseToken_auth_with_cvn();
        System.out.println("authToken " + authToken);
        final boolean[] done = { false };
        AuthenticationCallback callback = new AuthenticationCallback() {
            @Override public void onSuccess(Authentication authentication) {
                System.out.println("Success");
                fail();
            }

            @Override
            public void onError(XenditError xenditError) {
                System.out.println("Error " + xenditError.getErrorMessage());
                fail();
            }
        };

        Xendit x = createXendit(
            new InterceptorTestWithURLCallback() {
                @Override
                public void interceptRequest(JsonObject jsonObj, String url) {
                    // verify if the json object contains card_data
                    assertEquals(jsonObj.get("amount").getAsString(), "10000");
                    assertEquals(jsonObj.get("currency").getAsString(), "IDR");
                    assertEquals(jsonObj.get("card_cvn").getAsString(), "123");

                    assertEquals(jsonObj.get("card_data").getAsJsonObject().get("card_holder_first_name").getAsString(), "John");
                    assertEquals(jsonObj.get("card_data").getAsJsonObject().get("card_holder_last_name").getAsString(), "Doe");
                    assertEquals(jsonObj.get("card_data").getAsJsonObject().get("card_holder_phone_number").getAsString(), "+12345678");
                    assertEquals(jsonObj.get("card_data").getAsJsonObject().get("card_holder_email").getAsString(), "johndoe@example.com");


                    assertTrue(url.contains(authToken));
                }
                @Override
                public void interceptRequestFailed(String error) {
                    // should not be called
                    fail();
                }
                @Override
                public void interceptResponse(Object interceptedMessage) {
                    Authentication response = (Authentication) interceptedMessage;
                    System.out.println(response);
                    assertEquals(response.getStatus(), "IN_REVIEW");
                    done[0] = true;
                }
                @Override
                public void interceptResponseFailed(String error) {
                    System.out.println("Error " + error);
                    fail();
                }
            });

        CardHolderData cardHolderData = new CardHolderData("John", "Doe", "johndoe@example.com", "+12345678");

        x.createAuthentication(authToken, "10000", "IDR", "123", cardHolderData, callback);

        Awaitility.await().atMost(10, TimeUnit.SECONDS).until(() -> done[0]);
    }

    // "Create authentication using a CREATED MULTIPLE USE token with only one set of cardData object
    //- cardData.cardHolderFirstName"
    @Test
    public void test_createAuth_with_created_multiple_use_token_with_only_one_cardData() {
        String authToken = createMultipleUseToken_auth_with_cvn();
        System.out.println("authToken " + authToken);
        final boolean[] done = { false };
        AuthenticationCallback callback = new AuthenticationCallback() {
            @Override public void onSuccess(Authentication authentication) {
                System.out.println("Success");
                fail();
            }

            @Override
            public void onError(XenditError xenditError) {
                System.out.println("Error " + xenditError.getErrorMessage());
                fail();
            }
        };

        Xendit x = createXendit(
            new InterceptorTestWithURLCallback() {
                @Override
                public void interceptRequest(JsonObject jsonObj, String url) {
                    // verify if the json object contains card_data
                    assertEquals(jsonObj.get("amount").getAsString(), "10000");
                    assertEquals(jsonObj.get("currency").getAsString(), "IDR");
                    assertEquals(jsonObj.get("card_cvn").getAsString(), "123");

                    assertEquals(jsonObj.get("card_data").getAsJsonObject().get("card_holder_first_name").getAsString(), "John");
                    assertNull(jsonObj.get("card_data").getAsJsonObject().get("card_holder_last_name"));
                    assertNull(jsonObj.get("card_data").getAsJsonObject().get("card_holder_phone_number"));
                    assertNull(jsonObj.get("card_data").getAsJsonObject().get("card_holder_email"));

                    assertTrue(url.contains(authToken));
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
                    assertEquals(error, "\"card_holder_first_name\" missing required peer \"card_holder_last_name\"");
                    done[0] = true;
                }
            });

        CardHolderData cardHolderData = new CardHolderData("John", null, null, null);

        x.createAuthentication(authToken, "10000", "IDR", "123", cardHolderData, callback);

        Awaitility.await().atMost(10, TimeUnit.SECONDS).until(() -> done[0]);
    }

    // "Create authentication using a CREATED MULTIPLE USE token with only one set of cardData object
    //- cardData.cardHolderLastName"
    @Test
    public void test_createAuth_with_created_multiple_use_token_with_only_one_cardData_lastName() {
        String authToken = createMultipleUseToken_auth_with_cvn();
        System.out.println("authToken " + authToken);
        final boolean[] done = { false };
        AuthenticationCallback callback = new AuthenticationCallback() {
            @Override public void onSuccess(Authentication authentication) {
                System.out.println("Success");
                fail();
            }

            @Override
            public void onError(XenditError xenditError) {
                System.out.println("Error " + xenditError.getErrorMessage());
                fail();
            }
        };

        Xendit x = createXendit(
            new InterceptorTestWithURLCallback() {
                @Override
                public void interceptRequest(JsonObject jsonObj, String url) {
                    // verify if the json object contains card_data
                    assertEquals(jsonObj.get("amount").getAsString(), "10000");
                    assertEquals(jsonObj.get("currency").getAsString(), "IDR");
                    assertEquals(jsonObj.get("card_cvn").getAsString(), "123");

                    assertEquals(jsonObj.get("card_data").getAsJsonObject().get("card_holder_last_name").getAsString(), "Doe");
                    assertNull(jsonObj.get("card_data").getAsJsonObject().get("card_holder_first_name"));
                    assertNull(jsonObj.get("card_data").getAsJsonObject().get("card_holder_phone_number"));
                    assertNull(jsonObj.get("card_data").getAsJsonObject().get("card_holder_email"));

                    assertTrue(url.contains(authToken));
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
                    assertEquals(error, "\"card_holder_last_name\" missing required peer \"card_holder_first_name\"");
                    done[0] = true;
                }
            });

        CardHolderData cardHolderData = new CardHolderData(null, "Doe", null, null);

        x.createAuthentication(authToken, "10000", "IDR", "123", cardHolderData, callback);

        Awaitility.await().atMost(10, TimeUnit.SECONDS).until(() -> done[0]);
    }

    // "Create authentication using a CREATED MULTIPLE USE token with only one set of cardData object
    //- cardData.cardHolderEmail"
    @Test
    public void test_createAuth_with_created_multiple_use_token_with_only_one_cardData_email() {
        String authToken = createMultipleUseToken_auth_with_cvn();
        System.out.println("authToken " + authToken);
        final boolean[] done = { false };
        AuthenticationCallback callback = new AuthenticationCallback() {
            @Override public void onSuccess(Authentication authentication) {
                System.out.println("Success");
                fail();
            }

            @Override
            public void onError(XenditError xenditError) {
                System.out.println("Error " + xenditError.getErrorMessage());
                fail();
            }
        };

        Xendit x = createXendit(
            new InterceptorTestWithURLCallback() {
                @Override
                public void interceptRequest(JsonObject jsonObj, String url) {
                    // verify if the json object contains card_data
                    assertEquals(jsonObj.get("amount").getAsString(), "10000");
                    assertEquals(jsonObj.get("currency").getAsString(), "IDR");
                    assertEquals(jsonObj.get("card_cvn").getAsString(), "123");

                    assertEquals(jsonObj.get("card_data").getAsJsonObject().get("card_holder_email").getAsString(), "johndoe@example.com");

                    assertNull(jsonObj.get("card_data").getAsJsonObject().get("card_holder_first_name"));
                    assertNull(jsonObj.get("card_data").getAsJsonObject().get("card_holder_last_name"));
                    assertNull(jsonObj.get("card_data").getAsJsonObject().get("card_holder_phone_number"));


                    assertTrue(url.contains(authToken));
                }
                @Override
                public void interceptRequestFailed(String error) {
                    // should not be called
                    fail();
                }
                @Override
                public void interceptResponse(Object interceptedMessage) {
                    Authentication response = (Authentication) interceptedMessage;
                    System.out.println(response);
                    assertEquals(response.getStatus(), "IN_REVIEW");
                    done[0] = true;
                }
                @Override
                public void interceptResponseFailed(String error) {
                    System.out.println("Error " + error);
                    fail();
                }
            });

        CardHolderData cardHolderData = new CardHolderData(null, null, "johndoe@example.com", null);

        x.createAuthentication(authToken, "10000", "IDR", "123", cardHolderData, callback);

        Awaitility.await().atMost(10, TimeUnit.SECONDS).until(() -> done[0]);
    }

    //"Create authentication using a CREATED MULTIPLE USE token with complete set of cardData object
    //- cardData.cardHolderPhoneNumber"
    @Test
    public void test_createAuth_with_created_multiple_use_token_with_only_one_cardData_phoneNumber() {
        String authToken = createMultipleUseToken_auth_with_cvn();
        System.out.println("authToken " + authToken);
        final boolean[] done = { false };
        AuthenticationCallback callback = new AuthenticationCallback() {
            @Override public void onSuccess(Authentication authentication) {
                System.out.println("Success");
                fail();
            }

            @Override
            public void onError(XenditError xenditError) {
                System.out.println("Error " + xenditError.getErrorMessage());
                fail();
            }
        };

        Xendit x = createXendit(
            new InterceptorTestWithURLCallback() {
                @Override
                public void interceptRequest(JsonObject jsonObj, String url) {
                    // verify if the json object contains card_data
                    assertEquals(jsonObj.get("amount").getAsString(), "10000");
                    assertEquals(jsonObj.get("currency").getAsString(), "IDR");
                    assertEquals(jsonObj.get("card_cvn").getAsString(), "123");

                    assertNull(jsonObj.get("card_data").getAsJsonObject().get("card_holder_first_name"));
                    assertNull(jsonObj.get("card_data").getAsJsonObject().get("card_holder_last_name"));
                    assertNull(jsonObj.get("card_data").getAsJsonObject().get("card_holder_email"));

                    assertEquals(jsonObj.get("card_data").getAsJsonObject().get("card_holder_phone_number").getAsString(), "+12345678");

                    assertTrue(url.contains(authToken));
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
                    assertEquals(error, "\"card_holder_phone_number\" missing required peer \"card_holder_first_name\"");
                    done[0] = true;
                }
            });

        CardHolderData cardHolderData = new CardHolderData(null, null, null, "+12345678");

        x.createAuthentication(authToken, "10000", "IDR", "123", cardHolderData, callback);

        Awaitility.await().atMost(10, TimeUnit.SECONDS).until(() -> done[0]);
    }

    // Store CVN using a CREATED MULTIPLE USE token with card CVN
    @Test
    public void test_storeCVN_with_created_multiple_use_token_with_card_cvn() {
        String authToken = createMultipleUseToken_auth_with_cvn();
        System.out.println("authToken " + authToken);
        final boolean[] done = { false };
        StoreCVNCallback callback = new StoreCVNCallback() {
            @Override public void onSuccess(Token token) {
                fail();
            }

            @Override
            public void onError(XenditError xenditError) {
                System.out.println("Error " + xenditError.getErrorMessage());
                fail();
            }
        };

        Xendit x = createXendit(
            new InterceptorTestWithURLCallback() {
                @Override
                public void interceptRequest(JsonObject jsonObj, String url) {
                    // verify if the json object contains card_data
                    assertEquals(jsonObj.get("card_cvn").getAsString(), "123");
                    assertEquals(jsonObj.get("token_id").getAsString(), authToken);
                }
                @Override
                public void interceptRequestFailed(String error) {
                    // should not be called
                    fail();
                }
                @Override
                public void interceptResponse(Object interceptedMessage) {
                    Token response = (Token) interceptedMessage;
                    assertEquals(response.getStatus(), "VERIFIED");
                    done[0] = true;
                }
                @Override
                public void interceptResponseFailed(String error) {
                    System.out.println("Error " + error);
                    // should not be called
                    fail();
                }
            });

        x.storeCVN(authToken, "123", new BillingDetails(), new Customer(), "", callback);

        Awaitility.await().atMost(10, TimeUnit.SECONDS).until(() -> done[0]);
    }
}
