package com.xendit;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.BaseHttpStack;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.Volley;
import com.cardinalcommerce.cardinalmobilesdk.Cardinal;
import com.cardinalcommerce.cardinalmobilesdk.enums.CardinalEnvironment;
import com.cardinalcommerce.cardinalmobilesdk.enums.CardinalRenderType;
import com.cardinalcommerce.cardinalmobilesdk.enums.CardinalUiType;
import com.cardinalcommerce.cardinalmobilesdk.models.CardinalConfigurationParameters;
import com.cardinalcommerce.cardinalmobilesdk.models.ValidateResponse;
import com.cardinalcommerce.cardinalmobilesdk.services.CardinalInitService;
import com.cardinalcommerce.cardinalmobilesdk.services.CardinalValidateReceiver;
import com.cardinalcommerce.shared.userinterfaces.UiCustomization;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.snowplowanalytics.snowplow.controller.TrackerController;
import com.snowplowanalytics.snowplow.event.Structured;
import com.xendit.DeviceInfo.AdInfo;
import com.xendit.DeviceInfo.DeviceInfo;
import com.xendit.Models.AuthenticatedToken;
import com.xendit.Models.Authentication;
import com.xendit.Models.BillingDetails;
import com.xendit.Models.Card;
import com.xendit.Models.Customer;
import com.xendit.Models.Jwt;
import com.xendit.Models.ThreeDSRecommendation;
import com.xendit.Models.Token;
import com.xendit.Models.XenditError;
import com.xendit.network.BaseRequest;
import com.xendit.network.DefaultResponseHandler;
import com.xendit.network.NetworkHandler;
import com.xendit.network.TLSSocketFactory;
import com.xendit.network.errors.ConnectionError;
import com.xendit.network.errors.NetworkError;
import com.xendit.network.interfaces.ResultListener;
import com.xendit.utils.CardValidator;
import com.xendit.utils.PermissionUtils;

import org.json.JSONArray;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

import io.sentry.Sentry;
import io.sentry.SentryEvent;
import io.sentry.SentryOptions;
import io.sentry.android.core.SentryAndroid;
import io.sentry.android.core.SentryAndroidOptions;
import io.sentry.protocol.SentryException;
import io.sentry.protocol.SentryStackFrame;
import io.sentry.protocol.SentryStackTrace;

import static com.xendit.Tracker.SnowplowTrackerBuilder.getTracker;

/**
 * Created by Dimon_GDA on 3/7/17.
 */

public class Xendit {

    private static final String TAG = "Xendit";
    private static final String PRODUCTION_XENDIT_BASE_URL = "https://api.xendit.co";
    private static final String CREATE_CREDIT_CARD_URL = PRODUCTION_XENDIT_BASE_URL + "/credit_card_tokens";
    private static final String CREATE_CREDIT_CARD_TOKEN_URL = PRODUCTION_XENDIT_BASE_URL + "/v2/credit_card_tokens";
    private static final String GET_3DS_URL = PRODUCTION_XENDIT_BASE_URL + "/3ds_bin_recommendation";
    private static final String CREATE_JWT_URL = PRODUCTION_XENDIT_BASE_URL + "/credit_card_tokens/:token_id/jwt";
    private static final String DNS_SERVER = "https://182c197ad5c04f878fef7eab1e0cbcd6@sentry.io/262922";
    private static final String CLIENT_IDENTIFIER = "Xendit Android SDK";
    private static final String CLIENT_API_VERSION = "2.0.0";
    private static final String CLIENT_TYPE = "SDK";
    static final String ACTION_KEY = "ACTION_KEY";

    private Context context;
    private String publishableKey;
    private RequestQueue requestQueue;
    private ConnectivityManager connectivityManager;
    private Cardinal cardinal;
    private Activity activity;
    private Gson gsonMapper;

    public Xendit(final Context context, String publishableKey, Activity activity) {
        this(context, publishableKey);
        this.activity = activity;
    }

    public Xendit(final Context context, String publishableKey) {
        this.context = context;
        this.publishableKey = publishableKey;
        this.gsonMapper = new Gson();

        // init sentry
        // Use the Sentry DSN (client key) from the Project Settings page on Sentry
        SentryAndroid.init(context, new Sentry.OptionsConfiguration<SentryAndroidOptions>() {
            @Override
            public void configure(SentryAndroidOptions sentryAndroidOptions) {
                sentryAndroidOptions.setBeforeSend(new SentryOptions.BeforeSendCallback() {
                    @Override
                    public SentryEvent execute(SentryEvent event, Object o) {
                        // decide whether to send the event
                        for (SentryException sentryException : event.getExceptions()) {
                            SentryStackTrace stackTrace = sentryException.getStacktrace();
                            for (SentryStackFrame frame : stackTrace.getFrames()) {
                                if (frame.getModule().contains("com.xendit")) {
                                    return event;
                                }
                            }
                        }
                        return null;
                    }
                });
                sentryAndroidOptions.setDsn(DNS_SERVER);
            }
        });

        //get device info
        new Thread(new Runnable() {
            public void run() {
                try {
                    AdInfo adInfo = DeviceInfo.getAdvertisingIdInfo(context);
                    String advertisingId = adInfo.getId();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN
                && Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP_MR1) {

            BaseHttpStack stack;
            try {
                stack = new HurlStack(null, new TLSSocketFactory());
            } catch (KeyManagementException e) {
                e.printStackTrace();
                stack = new HurlStack();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
                stack = new HurlStack();
            }
            requestQueue = Volley.newRequestQueue(context, stack);
        } else {
            requestQueue = Volley.newRequestQueue(context);
        }

        connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        cardinal = Cardinal.getInstance();
    }

    private void configureCardinal(CardinalEnvironment environment) {
        CardinalConfigurationParameters cardinalConfigurationParameters = new CardinalConfigurationParameters();
        cardinalConfigurationParameters.setEnvironment(environment);
        cardinalConfigurationParameters.setRequestTimeout(8000);
        cardinalConfigurationParameters.setChallengeTimeout(5);

        JSONArray rType = new JSONArray();
        rType.put(CardinalRenderType.OTP);
        rType.put(CardinalRenderType.SINGLE_SELECT);
        rType.put(CardinalRenderType.MULTI_SELECT);
        rType.put(CardinalRenderType.OOB);
        rType.put(CardinalRenderType.HTML);
        cardinalConfigurationParameters.setRenderType(rType);

        cardinalConfigurationParameters.setUiType(CardinalUiType.BOTH);

        UiCustomization yourUICustomizationObject = new UiCustomization();
        cardinalConfigurationParameters.setUICustomization(yourUICustomizationObject);
        cardinal.configure(context, cardinalConfigurationParameters);
    }

    /**
     * Determines whether the credit card number provided is valid
     *
     * @param  creditCardNumber A credit card number
     * @return true if the credit card number is valid, false otherwise
     * @deprecated Use CardValidator.isCardNumberValid
     */
    @Deprecated
    public static boolean isCardNumberValid(String creditCardNumber) {
        return CardValidator.isCardNumberValid(creditCardNumber);
    }

    /**
     * Determines whether the card expiration month and year are valid
     *
     * @param  cardExpirationMonth The month a card expired represented by digits (e.g. 12)
     * @param  cardExpirationYear The year a card expires represented by digits (e.g. 2026)
     * @return true if both the expiration month and year are valid
     * @deprecated Use CardValidator.isExpiryValid
     */
    @Deprecated
    public static boolean isExpiryValid(String cardExpirationMonth, String cardExpirationYear) {
        return CardValidator.isExpiryValid(cardExpirationMonth, cardExpirationYear);
    }

    /**
     * Determines whether the card CVN is valid
     *
     * @param  creditCardCVN The credit card CVN
     * @return true if the cvn is valid, false otherwise
     * @deprecated Use CardValidator.isCvnValid
     */
    @Deprecated
    public static boolean isCvnValid(String creditCardCVN) {
        return CardValidator.isCvnValid(creditCardCVN);
    }

    /**
     * Creates a single-use token. 3DS authentication will be bundled into this method unless you
     * set shouldAuthenticate as false.
     *
     * @param  card A credit card
     * @param  amount The amount you will eventually charge. This value is used to display to the
     *                user in the 3DS authentication view.
     * @param tokenCallback The callback that will be called when the token creation completes or
     *                      fails
     */
    public void createSingleUseToken(final Card card, final int amount, final TokenCallback tokenCallback) {
        String amountStr = Integer.toString(amount);

        createSingleOrMultipleUseToken(card, amountStr, true, "", false, null, null, null, tokenCallback);
    }

    /**
     * Creates a single-use token. 3DS authentication will be bundled into this method unless you
     * set shouldAuthenticate as false.
     *
     * @param  card A credit card
     * @param  amount The amount you will eventually charge. This value is used to display to the
     *                user in the 3DS authentication view.
     * @param shouldAuthenticate A flag indicating if 3DS authentication are required for this token
     * @param tokenCallback The callback that will be called when the token creation completes or
     *                      fails
     */
    public void createSingleUseToken(final Card card, final int amount, final boolean shouldAuthenticate, final TokenCallback tokenCallback) {
        String amountStr = Integer.toString(amount);

        createSingleOrMultipleUseToken(card, amountStr, shouldAuthenticate, "", false, null, null, null, tokenCallback);
    }

    /**
     * Creates a single-use token. 3DS authentication will be bundled into this method unless you
     * set shouldAuthenticate as false.
     *
     * @param  card A credit card
     * @param  amount The amount you will eventually charge. This value is used to display to the
     *                user in the 3DS authentication view.
     * @param shouldAuthenticate A flag indicating if 3DS authentication are required for this token
     * @param onBehalfOf The onBehalfOf is sub account business id
     * @param tokenCallback The callback that will be called when the token creation completes or
     *                      fails
     */
    public void createSingleUseToken(final Card card, final int amount, final boolean shouldAuthenticate, final String onBehalfOf, final TokenCallback tokenCallback) {
        String amountStr = Integer.toString(amount);

        createSingleOrMultipleUseToken(card, amountStr, shouldAuthenticate, onBehalfOf, false, null, null, null, tokenCallback);
    }

    /**
     * Creates a single-use token. 3DS authentication will be bundled into this method unless you
     * set shouldAuthenticate as false.
     *
     * @param  card A credit card
     * @param  amount The amount you will eventually charge. This value is used to display to the
     *                user in the 3DS authentication view.
     * @param shouldAuthenticate A flag indicating if 3DS authentication are required for this token
     * @param onBehalfOf The onBehalfOf is sub account business id
     * @param billingDetails Billing details of the card
     * @param customer Customer object making the transaction
     * @param tokenCallback The callback that will be called when the token creation completes or
     *                      fails
     */
    public void createSingleUseToken(
            final Card card,
            final int amount,
            final boolean shouldAuthenticate,
            final String onBehalfOf,
            final BillingDetails billingDetails,
            final Customer customer,
            TokenCallback tokenCallback) {
        String amountStr = Integer.toString(amount);

        createSingleOrMultipleUseToken(card, amountStr, shouldAuthenticate, onBehalfOf, false, billingDetails, customer, null, tokenCallback);
    }

    /**
     * Creates a single-use token. 3DS authentication will be bundled into this method unless you
     * set shouldAuthenticate as false.
     *
     * @param  card A credit card
     * @param  amount The amount you will eventually charge. This value is used to display to the
     *                user in the 3DS authentication view.
     * @param shouldAuthenticate A flag indicating if 3DS authentication are required for this token
     * @param onBehalfOf The onBehalfOf is sub account business id
     * @param billingDetails Billing details of the card
     * @param customer Customer object making the transaction
     * @param currency Currency when requesting for 3DS authentication
     * @param tokenCallback The callback that will be called when the token creation completes or
     *                      fails
     */
    public void createSingleUseToken(
            final Card card,
            final int amount,
            final boolean shouldAuthenticate,
            final String onBehalfOf,
            final BillingDetails billingDetails,
            final Customer customer,
            final String currency,
            TokenCallback tokenCallback) {
        String amountStr = Integer.toString(amount);

        createSingleOrMultipleUseToken(card, amountStr, shouldAuthenticate, onBehalfOf, false, billingDetails, customer, currency, tokenCallback);
    }

    /**
     * Creates a single-use token. 3DS authentication will be bundled into this method unless you
     * set shouldAuthenticate as false.
     *
     * @param  card A credit card
     * @param  amount The amount in string you will eventually charge. This value is used to display to the
     *                user in the 3DS authentication view.
     * @param shouldAuthenticate A flag indicating if 3DS authentication are required for this token
     * @param onBehalfOf The onBehalfOf is sub account business id
     * @param billingDetails Billing details of the card
     * @param customer Customer object making the transaction
     * @param currency Currency when requesting for 3DS authentication
     * @param tokenCallback The callback that will be called when the token creation completes or
     *                      fails
     */
    public void createSingleUseToken(
            final Card card,
            final String amount,
            final boolean shouldAuthenticate,
            final String onBehalfOf,
            final BillingDetails billingDetails,
            final Customer customer,
            final String currency,
            TokenCallback tokenCallback) {
        createSingleOrMultipleUseToken(card, amount, shouldAuthenticate, onBehalfOf, false, billingDetails, customer, currency, tokenCallback);
    }

    /**
     * Creates a single-use token. 3DS authentication will be bundled into this method unless you
     * set shouldAuthenticate as false.
     *
     * @param  card A credit card
     * @param  amount The amount in string you will eventually charge. This value is used to display to the
     *                user in the 3DS authentication view.
     * @param shouldAuthenticate A flag indicating if 3DS authentication are required for this token
     * @param currency Currency when requesting for 3DS authentication
     * @param tokenCallback The callback that will be called when the token creation completes or
     *                      fails
     */
    public void createSingleUseToken(
            final Card card,
            final String amount,
            final boolean shouldAuthenticate,
            final String currency,
            TokenCallback tokenCallback) {
        createSingleOrMultipleUseToken(card, amount, shouldAuthenticate, null, false, null, null, currency, tokenCallback);
    }

    /**
     * Creates a multiple-use token. Authentication must be created separately if shouldAuthenticate
     * is true.
     *
     * @param  card A credit card
     * @param tokenCallback The callback that will be called when the token creation completes or
     *                      fails
     */
    public void createMultipleUseToken(final Card card, final TokenCallback tokenCallback) {
        createSingleOrMultipleUseToken(card, "0", false, "", true, null, null, null, tokenCallback);
    }

    /**
     * Creates a multiple-use token. Authentication must be created separately if shouldAuthenticate
     * is true.
     *
     * @param  card A credit card
     * @param onBehalfOf The onBehalfOf is sub account business id
     * @param tokenCallback The callback that will be called when the token creation completes or
     *                      fails
     */
    public void createMultipleUseToken(final Card card, final String onBehalfOf, final TokenCallback tokenCallback) {
        createSingleOrMultipleUseToken(card, "0", false, onBehalfOf, true, null, null, null, tokenCallback);
    }

    /**
     * Creates a multiple-use token. Authentication must be created separately if shouldAuthenticate
     * is true.
     *
     * @param  card A credit card
     * @param onBehalfOf The onBehalfOf is sub account business id
     * @param billingDetails Billing details of the card
     * @param tokenCallback The callback that will be called when the token creation completes or
     *                      fails
     */
    public void createMultipleUseToken(final Card card, final String onBehalfOf, BillingDetails billingDetails, final TokenCallback tokenCallback) {
        createSingleOrMultipleUseToken(card, "0", false, onBehalfOf, true, billingDetails, null, null, tokenCallback);
    }

    /**
     * Creates a multiple-use token. Authentication must be created separately if shouldAuthenticate
     * is true.
     *
     * @param  card A credit card
     * @param onBehalfOf The onBehalfOf is sub account business id
     * @param billingDetails Billing details of the card
     * @param customer Customer linked to the payment method
     * @param tokenCallback The callback that will be called when the token creation completes or
     *                      fails
     */
    public void createMultipleUseToken(final Card card, final String onBehalfOf, BillingDetails billingDetails, Customer customer, final TokenCallback tokenCallback) {
        createSingleOrMultipleUseToken(card, "0", false, onBehalfOf, true, billingDetails, customer, null, tokenCallback);
    }

    private void createSingleOrMultipleUseToken(
            final Card card,
            final String amount,
            final boolean shouldAuthenticate,
            final String onBehalfOf,
            final boolean isMultipleUse,
            final BillingDetails billingDetails,
            final Customer customer,
            final String currency,
            final TokenCallback tokenCallback
    ) {
        if (card != null && tokenCallback != null) {
            if (!CardValidator.isCardNumberValid(card.getCreditCardNumber())) {
                tokenCallback.onError(new XenditError(context.getString(R.string.create_token_error_card_number)));
                return;
            }

            if (!CardValidator.isExpiryValid(card.getCardExpirationMonth(), card.getCardExpirationYear())) {
                tokenCallback.onError(new XenditError(context.getString(R.string.create_token_error_card_expiration)));
                return;
            }

            if (card.getCreditCardCVN() != null && !CardValidator.isCvnValid(card.getCreditCardCVN())) {
                tokenCallback.onError(new XenditError(context.getString(R.string.create_token_error_card_cvn)));
                return;
            }

            if (card.getCreditCardCVN() != null && !CardValidator.isCvnValidForCardType(card.getCreditCardCVN(), card.getCreditCardNumber())) {
                tokenCallback.onError(new XenditError(context.getString(R.string.error_card_cvn_invalid_for_type)));
                return;
            }

            createCreditCardToken(card, amount, shouldAuthenticate, onBehalfOf, isMultipleUse, billingDetails, customer, currency, tokenCallback);
        }
    }

    /**
     * Creates a 3DS authentication for a multiple-use token
     *
     * @param tokenId A multi-use token id
     * @param amount Amount of money to be authenticated
     * @param currency Currency of the amount
     * @param authenticationCallback The callback that will be called when the authentication completes or
     *                      fails
     */
    public void createAuthentication(final String tokenId, final int amount, final String currency, final AuthenticationCallback authenticationCallback) {
        final String amountStr = Integer.toString(amount);
        createAuthenticationInternal(tokenId, amountStr, currency, null, null, authenticationCallback);
    }

    /**
     * Creates a 3DS authentication for a multiple-use token
     *
     * @param  tokenId The id of a multiple-use token
     * @param  amount The amount that will eventually be charged. This number is displayed to the
     *                user in the 3DS authentication view
     * @param authenticationCallback The callback that will be called when the authentication
     *                               creation completes or fails
     */
    public void createAuthentication(final String tokenId, final int amount, final AuthenticationCallback authenticationCallback) {
        final String amountStr = Integer.toString(amount);
        createAuthenticationInternal(tokenId, amountStr, null, null, null, authenticationCallback);
    }

    /**
     * Creates a 3DS authentication for a multiple-use token
     *
     * @param tokenId A multi-use token id
     * @param amount Amount of money to be authenticated
     * @param currency Currency of the amount
     * @param cardCvn CVV/CVN collected from the card holder
     * @param authenticationCallback The callback that will be called when the authentication completes or
     *                      fails
     */
    public void createAuthentication(final String tokenId, final String amount, final String currency, final String cardCvn, final AuthenticationCallback authenticationCallback) {
        createAuthenticationInternal(tokenId, amount, currency, cardCvn, null, authenticationCallback);
    }

    /**
     * Creates a 3DS authentication for a multiple-use token
     *
     * @param tokenId A multi-use token id
     * @param amount Amount of money to be authenticated
     * @param currency Currency of the amount
     * @param authenticationCallback The callback that will be called when the authentication completes or
     *                      fails
     */
    public void createAuthentication(final String tokenId, final String amount, final String currency, final AuthenticationCallback authenticationCallback) {
        createAuthenticationInternal(tokenId, amount, currency, null, null, authenticationCallback);
    }

    /**
     * Creates a 3DS authentication for a multiple-use token
     *
     * @param tokenId A multi-use token id
     * @param amount Amount of money to be authenticated
     * @param currency Currency of the amount
     * @param cardCvn CVV/CVN collected from the card holder
     * @param onBehalfOf Business Id to call the API on behalf of (Applicable to Platform merchants)
     * @param authenticationCallback The callback that will be called when the authentication completes or
     *                      fails
     */
    public void createAuthentication(final String tokenId, final String amount, final String currency, final String cardCvn, final String onBehalfOf, final AuthenticationCallback authenticationCallback) {
        createAuthenticationInternal(tokenId, amount, currency, cardCvn, onBehalfOf, authenticationCallback);
    }

    private void createAuthenticationInternal(final String tokenId, final String amount, final String currency, final String cardCvn, final String onBehalfOf, final AuthenticationCallback authenticationCallback) {
        if (tokenId == null || tokenId.equals("")) {
            authenticationCallback.onError(new XenditError(context.getString(R.string.create_token_error_validation)));
            return;
        }

        if (Double.parseDouble(amount) < 0) {
            authenticationCallback.onError(new XenditError(context.getString(R.string.create_token_error_validation)));
            return;
        }
        _createJWT(tokenId, amount, "IDR", null, onBehalfOf, new NetworkHandler<Jwt>().setResultListener(new ResultListener<Jwt>() {
            @Override
            public void onSuccess(Jwt jwt) {
                create3ds2Authentication(tokenId, jwt.getEnvironment(), jwt.getJwt(), amount, currency, cardCvn, onBehalfOf, new TokenCallback() {
                    @Override
                    public void onSuccess(Token token) {
                        TrackerController tracker = getTracker(context);
                        tracker.track(Structured.builder()
                                .category("api-request")
                                .action("create-authentication-emv-3ds")
                                .label("Create EMV 3DS")
                                .build());
                        authenticationCallback.onSuccess(new Authentication(token));
                    }

                    @Override
                    public void onError(XenditError error) {
                        authenticationCallback.onError(error);
                    }
                });
            }

            @Override
            public void onFailure(NetworkError error) {
                _createAuthentication(tokenId, amount, currency, cardCvn, onBehalfOf, new NetworkHandler<Authentication>().setResultListener(new ResultListener<Authentication>() {
                    @Override
                    public void onSuccess(Authentication authentication) {
                        TrackerController tracker = getTracker(context);
                        tracker.track(Structured.builder()
                                .category("api-request")
                                .action("create-authentication")
                                .label("Create Authentication")
                                .build());

                        if (!authentication.getStatus().equalsIgnoreCase("VERIFIED")) {
                            registerBroadcastReceiver(authenticationCallback);
                            context.startActivity(XenditActivity.getLaunchIntent(context, authentication));
                        } else {
                            authenticationCallback.onSuccess(authentication);
                        }
                    }

                    @Override
                    public void onFailure(NetworkError error) {
                        authenticationCallback.onError(new XenditError(error));
                    }
                }));
            }
        }));
    }

    private void get3DSRecommendation(String tokenId, final AuthenticatedToken authentication, final TokenCallback callback){
        _get3DSRecommendation(tokenId, new NetworkHandler<ThreeDSRecommendation>().setResultListener(new ResultListener<ThreeDSRecommendation>(){
            @Override
            public void onSuccess (ThreeDSRecommendation rec) {
                TrackerController tracker = getTracker(context);
                tracker.track(Structured.builder()
                        .category("api-request")
                        .action("get-3ds-recommendation")
                        .label("Get 3DS Recommendation")
                        .build());

                callback.onSuccess(new Token(authentication, rec));
            }

            @Override
            public void onFailure (NetworkError error) {
                callback.onSuccess(new Token(authentication));
            }
        }));
    }

    public void createCreditCardToken(Card card, String amount, boolean shouldAuthenticate, boolean isMultipleUse, final TokenCallback tokenCallback) {
        _createToken(card, amount, shouldAuthenticate, "", isMultipleUse, null, null, null, new NetworkHandler<AuthenticatedToken>().setResultListener(new ResultListener<AuthenticatedToken>() {
            @Override
            public void onSuccess(AuthenticatedToken authentication) {
                    TrackerController tracker = getTracker(context);
                    tracker.track(Structured.builder()
                        .category("api-request")
                        .action("create-token")
                        .label("Create Token")
                        .build());

                if (!authentication.getStatus().equalsIgnoreCase("VERIFIED")) { //single token = automatically attempt 3DS
                    registerBroadcastReceiver(tokenCallback);
                    context.startActivity(XenditActivity.getLaunchIntent(context, authentication));
                }
                else { //for multi token
                    String tokenId = authentication.getId();
                    get3DSRecommendation(tokenId, authentication, tokenCallback);
                }
            }

            @Override
            public void onFailure(NetworkError error) {
                tokenCallback.onError(new XenditError(error));
            }
        }));
    }

    public void createCreditCardToken(final Card card, final String amount, boolean shouldAuthenticate, final String onBehalfOf, boolean isMultipleUse, final TokenCallback tokenCallback) {
        _createToken(card, amount, shouldAuthenticate, onBehalfOf, isMultipleUse, null, null, null, new NetworkHandler<AuthenticatedToken>().setResultListener(new ResultListener<AuthenticatedToken>() {
            @Override
            public void onSuccess(final AuthenticatedToken authentication) {
                TrackerController tracker = getTracker(context);
                tracker.track(Structured.builder()
                        .category("api-request")
                        .action("create-token")
                        .label("Create Token")
                        .build());
                if (is3ds2Version(authentication.getThreedsVersion())) {
                    create3ds2Authentication(authentication.getId(), authentication.getEnvironment(), authentication.getJwt(), amount, null, card.getCreditCardCVN(), onBehalfOf, tokenCallback);
                } else {
                    handle3ds1Tokenization(authentication, tokenCallback);
                }
            }

            @Override
            public void onFailure(NetworkError error) {
                tokenCallback.onError(new XenditError(error));
            }
        }));
    }

    public void createCreditCardToken(
            final Card card,
            final String amount,
            boolean shouldAuthenticate,
            final String onBehalfOf,
            boolean isMultipleUse,
            BillingDetails billingDetails,
            Customer customer,
            final String currency,
            final TokenCallback tokenCallback) {
        _createToken(card, amount, shouldAuthenticate, onBehalfOf, isMultipleUse, billingDetails, customer, currency, new NetworkHandler<AuthenticatedToken>().setResultListener(new ResultListener<AuthenticatedToken>() {
            @Override
            public void onSuccess(final AuthenticatedToken authentication) {
                TrackerController tracker = getTracker(context);
                tracker.track(Structured.builder()
                        .category("api-request")
                        .action("create-token")
                        .label("Create Token")
                        .build());
                if (is3ds2Version(authentication.getThreedsVersion())) {
                    create3ds2Authentication(authentication.getId(), authentication.getEnvironment(), authentication.getJwt(), amount, currency, card.getCreditCardCVN(), onBehalfOf, tokenCallback);
                } else {
                    handle3ds1Tokenization(authentication, tokenCallback);
                }
            }

            @Override
            public void onFailure(NetworkError error) {
                tokenCallback.onError(new XenditError(error));
            }
        }));
    }

    private void registerBroadcastReceiver(final AuthenticationCallback authenticationCallback) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                context.registerReceiver(new AuthenticationBroadcastReceiver(authenticationCallback), new IntentFilter(ACTION_KEY));
            }
        });
    }

    private void registerBroadcastReceiver(final TokenCallback tokenCallback) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                context.registerReceiver(new TokenBroadcastReceiver(tokenCallback), new IntentFilter(ACTION_KEY));
            }
        });
    }

    private void registerBroadcastReceiverAuthenticatedToken(final TokenCallback tokenCallback) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                context.registerReceiver(new AuthenticatedTokenBroadcastReceiver(tokenCallback), new IntentFilter(ACTION_KEY));
            }
        });
    }

    private void _createToken(Card card, String amount, boolean shouldAuthenticate, String onBehalfOf, boolean isMultipleUse, BillingDetails billingDetails, Customer customer, String currency, NetworkHandler<AuthenticatedToken> handler) {
        JsonObject cardData = new JsonObject();
        cardData.addProperty("account_number", card.getCreditCardNumber());
        cardData.addProperty("exp_year", card.getCardExpirationYear());
        cardData.addProperty("exp_month", card.getCardExpirationMonth());
        cardData.addProperty("cvn", card.getCreditCardCVN());

        BaseRequest<AuthenticatedToken> request = buildBaseRequest(Request.Method.POST, CREATE_CREDIT_CARD_TOKEN_URL, onBehalfOf, AuthenticatedToken.class, new DefaultResponseHandler<>(handler));
        request.addParam("is_single_use", String.valueOf(!isMultipleUse));
        request.addParam("should_authenticate", String.valueOf(shouldAuthenticate));
        request.addJsonParam("card_data", cardData);

        if (customer != null) {
            request.addJsonParam("customer", gsonMapper.toJsonTree(customer));
        }

        if (billingDetails != null) {
            request.addJsonParam("billing_details", gsonMapper.toJsonTree(billingDetails));
        }

        if (!isMultipleUse) {
            request.addParam("amount", amount);
        }

        if (currency != null) {
            request.addParam("currency", currency);
        }

        sendRequest(request, handler);
    }

    private void _get3DSRecommendation(String tokenId, NetworkHandler<ThreeDSRecommendation> handler) {
        String url = GET_3DS_URL + "?token_id=" + tokenId;

        BaseRequest<ThreeDSRecommendation> request = buildBaseRequest(Request.Method.GET, url, null, ThreeDSRecommendation.class, new DefaultResponseHandler<>(handler));
        sendRequest(request, handler);
    }

    private void _createAuthentication(String tokenId, String amount, String currency, String cardCvn, String onBehalfOf, NetworkHandler<Authentication> handler) {
        String requestUrl = CREATE_CREDIT_CARD_URL + "/" + tokenId + "/authentications";

        BaseRequest<Authentication> request = buildBaseRequest(Request.Method.POST, requestUrl, onBehalfOf, Authentication.class, new DefaultResponseHandler<>(handler));
        request.addParam("amount", amount);
        if (currency != null && currency != "") {
            request.addParam("currency", currency);
        }
        if (cardCvn != null && currency != "") {
            request.addParam("card_cvn", cardCvn);
        }
        sendRequest(request, handler);
    }

    private void _createAuthenticationToken(final String tokenId, String amount, String currency, String cardCvn, String onBehalfOf, final TokenCallback tokenCallback) {
        _createAuthentication(tokenId, amount, currency, cardCvn, onBehalfOf, new NetworkHandler<Authentication>().setResultListener(new ResultListener<Authentication>() {
            @Override
            public void onSuccess(Authentication responseObject) {
                handleAuthenticatedToken(tokenId, responseObject, tokenCallback);
            }

            @Override
            public void onFailure(NetworkError error) {
                tokenCallback.onError(new XenditError(error));
            }
        }));
    }

    private void _createAuthenticationWithSessionId(final String tokenId, final String amount, final String sessionId, final String currency, final String cardCvn, final String onBehalfOf, final TokenCallback tokenCallback) {
        String requestUrl = CREATE_CREDIT_CARD_URL + "/" + tokenId + "/authentications";
        NetworkHandler handler = new NetworkHandler<Authentication>().setResultListener(new ResultListener<Authentication>() {
            @Override
            public void onSuccess(Authentication responseObject) {
                if (responseObject.getStatus().equalsIgnoreCase("VERIFIED") || !is3ds2Version(responseObject.getThreedsVersion())) {
                    // Authentication completed or 3DS 1.0 transaction
                    handleAuthenticatedToken(tokenId, responseObject, tokenCallback);
                }
                else if (
                        responseObject.getStatus().equalsIgnoreCase("FAILED") ||
                        responseObject.getAuthenticationTransactionId() == null ||
                        responseObject.getRequestPayload() == null) {
                    // Fallback to 3DS1 flow
                    _createAuthenticationToken(tokenId, amount, currency, cardCvn, onBehalfOf,tokenCallback);
                } else {
                    final String transactionId = responseObject.getAuthenticationTransactionId();
                    final String reqPayload = responseObject.getRequestPayload();
                    final String authenticationId = responseObject.getId();
                    cardinal.cca_continue(transactionId, reqPayload, activity, new CardinalValidateReceiver() {
                        @Override
                        public void onValidated(Context context, ValidateResponse validateResponse, String serverJwt) {
                            _verifyAuthentication(authenticationId, transactionId, onBehalfOf, new NetworkHandler<Authentication>().setResultListener(new ResultListener<Authentication>() {
                                @Override
                                public void onSuccess(Authentication responseObject) {
                                    Token token = new Token(responseObject);
                                    tokenCallback.onSuccess(token);
                                }

                                @Override
                                public void onFailure(NetworkError error) {
                                    tokenCallback.onError(new XenditError(error));
                                }
                            }));
                        }
                    });
                }
            }

            @Override
            public void onFailure(NetworkError error) {
                // Fallback to 3DS1 flow
                _createAuthenticationToken(tokenId, amount, currency, cardCvn, onBehalfOf, tokenCallback);
            }
        });

        BaseRequest<Authentication> request = buildBaseRequest(Request.Method.POST, requestUrl, onBehalfOf, Authentication.class, new DefaultResponseHandler<>(handler));
        request.addParam("amount", amount);
        request.addParam("session_id", sessionId);
        if (currency != null && currency != "") {
            request.addParam("currency", currency);
        }
        if (cardCvn != null && cardCvn != "") {
            request.addParam("card_cvn", cardCvn);
        }
        sendRequest(request, handler);
    }

    private void _verifyAuthentication(String authenticationId, String authenticationTransactionId, String onBehalfOf, NetworkHandler<Authentication> handler) {
        String requestUrl = PRODUCTION_XENDIT_BASE_URL + "/credit_card_authentications/" + authenticationId + "/verification";

        BaseRequest<AuthenticatedToken> request = buildBaseRequest(Request.Method.POST, requestUrl, onBehalfOf, Authentication.class, new DefaultResponseHandler<>(handler));
        request.addParam("authentication_transaction_id", authenticationTransactionId);
        sendRequest(request, handler);
    }

    private void handle3ds1Tokenization(AuthenticatedToken authentication, TokenCallback tokenCallback) {
        if (authentication.getStatus().equalsIgnoreCase("FAILED")) {
            tokenCallback.onSuccess(new Token(authentication));
        } else if (!authentication.getStatus().equalsIgnoreCase("VERIFIED")) {
            registerBroadcastReceiver(tokenCallback);
            context.startActivity(XenditActivity.getLaunchIntent(context, authentication));
        } else { //for multi token
            String tokenId = authentication.getId();
            get3DSRecommendation(tokenId, authentication, tokenCallback);
        }
    }

    private void handleAuthenticatedToken(String tokenId, Authentication authenticatedToken, TokenCallback tokenCallback) {
        if (authenticatedToken.getStatus().equalsIgnoreCase("VERIFIED") || authenticatedToken.getStatus().equalsIgnoreCase("FAILED")) {
            Token token = new Token(authenticatedToken, tokenId);
            tokenCallback.onSuccess(token);
        } else {
            registerBroadcastReceiverAuthenticatedToken(tokenCallback);
            context.startActivity(XenditActivity.getLaunchIntent(context, authenticatedToken));
        }
    }

    private boolean is3ds2Version(String version) {
        if (version != null) {
            int currentMajorVersion = Integer.parseInt(version.substring(0, 1));
            return currentMajorVersion >= 2;
        }
        return false;
    }

    private void create3ds2Authentication(final String tokenId, final String environment, final String jwt, final String amount, final String currency, final String cardCvn, final String onBehalfOf, final TokenCallback tokenCallback) {
        if ("DEVELOPMENT".equals(environment)) {
            configureCardinal(CardinalEnvironment.STAGING);
        } else {
            configureCardinal(CardinalEnvironment.PRODUCTION);
        }
        cardinal.init(jwt, new CardinalInitService() {
            @Override
            public void onSetupCompleted(String consumerSessionId) {
                _createAuthenticationWithSessionId(tokenId, amount, consumerSessionId, currency, cardCvn, onBehalfOf, tokenCallback);
            }

            /**
             * If there was an error with set up, Cardinal will call this function with
             * validate response and empty serverJWT. We will fallback to 3DS 1.0 flow.
             *
             * @param validateResponse
             * @param serverJwt        will be an empty
             */
            @Override
            public void onValidated(ValidateResponse validateResponse, String serverJwt) {
                _createAuthenticationToken(tokenId, amount, currency, cardCvn, onBehalfOf, tokenCallback);
            }
        });
    }

    private String encodeBase64(String key) {
        try {
            byte[] keyData = key.getBytes("UTF-8");
            return Base64.encodeToString(keyData, Base64.DEFAULT);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void sendRequest(BaseRequest request, NetworkHandler<?> handler) {
        if (isConnectionAvailable()) {
            requestQueue.add(request);
        } else if (handler != null) {
            handler.handleError(new ConnectionError());
        }
    }

    private boolean isConnectionAvailable() {
        if (PermissionUtils.hasPermission(context, Manifest.permission.ACCESS_NETWORK_STATE)) {
            @SuppressLint("MissingPermission") NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
            return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        } else {
            return false;
        }

    }

    private boolean getEnvironment() {
        String publishKey = publishableKey.toUpperCase();
        return publishKey.contains("PRODUCTION");
    }

    private BaseRequest buildBaseRequest(int method, String url, String onBehalfOf, Type type, DefaultResponseHandler handler) {
        String encodedKey = encodeBase64(publishableKey + ":");
        String basicAuthCredentials = "Basic " + encodedKey;
        BaseRequest request = new BaseRequest<>(method, url, type, handler);
        if (onBehalfOf != null && onBehalfOf != "") {
            request.addHeader("for-user-id", onBehalfOf);
        }
        request.addHeader("Authorization", basicAuthCredentials.replace("\n", ""));
        request.addHeader("x-client-identifier", CLIENT_IDENTIFIER);
        request.addHeader("client-version", CLIENT_API_VERSION);
        request.addHeader("client-type", CLIENT_TYPE);
        return request;
    }

    private void _createJWT(String tokenId, String amount, String currency, Customer customer, String onBehalfOf, NetworkHandler<Jwt> handler) {
        String url = CREATE_JWT_URL.replace(":token_id", tokenId);
        BaseRequest request = this.buildBaseRequest(Request.Method.POST, url, onBehalfOf, Jwt.class, new DefaultResponseHandler(handler));
        request.addParam("amount", amount);
        request.addParam("currency", currency);
        if (customer != null) {
            request.addParam("customer", gsonMapper.toJson(customer));
        }

        sendRequest(request, handler);
    }
}