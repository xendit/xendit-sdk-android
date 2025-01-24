package com.xendit;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
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
import com.xendit.Models.CardHolderData;
import com.xendit.Models.Customer;
import com.xendit.Models.ThreeDSRecommendation;
import com.xendit.Models.Token;
import com.xendit.Models.XenditError;
import com.xendit.interceptor.Interceptor;
import com.xendit.network.BaseRequest;
import com.xendit.network.DefaultResponseHandler;
import com.xendit.network.NetworkHandler;
import com.xendit.network.TLSSocketFactory;
import com.xendit.network.errors.ConnectionError;
import com.xendit.network.errors.NetworkError;
import com.xendit.network.interfaces.ResultListener;
import com.xendit.utils.CardValidator;
import com.xendit.utils.PermissionUtils;
import com.xendit.utils.StoreCVNCallback;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import io.sentry.Hint;
import io.sentry.Sentry;
import io.sentry.SentryEvent;
import io.sentry.SentryOptions;
import io.sentry.android.core.SentryAndroid;
import io.sentry.android.core.SentryAndroidOptions;
import io.sentry.protocol.SentryException;
import io.sentry.protocol.SentryStackFrame;
import io.sentry.protocol.SentryStackTrace;
import javax.annotation.Nullable;

import static com.xendit.Tracker.SnowplowTrackerBuilder.getTracker;

/**
 * Created by Dimon_GDA on 3/7/17.
 */

public class XenditImpl implements Xendit {

    private static final String TAG = "Xendit";
    private static final String PRODUCTION_XENDIT_BASE_URL = "https://api.xendit.co";
    private static final String CREATE_CREDIT_CARD_URL = PRODUCTION_XENDIT_BASE_URL + "/credit_card_tokens";
    private static final String CREATE_CREDIT_CARD_TOKEN_URL = PRODUCTION_XENDIT_BASE_URL + "/v2/credit_card_tokens";
    private static final String GET_3DS_URL = PRODUCTION_XENDIT_BASE_URL + "/3ds_bin_recommendation";
    private static final String DSN_SERVER = "https://7190a1331444434eb6aed7b5a8d776f0@o30316.ingest.sentry.io/6314580";
    private static final String CLIENT_IDENTIFIER = "Xendit Android SDK";
    private static final String CLIENT_TYPE = "SDK";

    private Context context;
    private String publishableKey;
    private RequestQueue requestQueue;
    private ConnectivityManager connectivityManager;
    private Activity activity;
    private Gson gsonMapper;

    private AuthenticationBroadcastReceiver authenticationBroadcastReceiver;
    private TokenBroadcastReceiver tokenBroadcastReceiver;
    private AuthenticatedTokenBroadcastReceiver authenticatedTokenBroadcastReceiver;

    private Interceptor<BaseRequest<?>> requestInterceptor;
    private Interceptor<?> responseInterceptor;

    protected XenditImpl(final Context context, String publishableKey, @Nullable Interceptor<BaseRequest<?>> request, @Nullable Interceptor<?> response) {
        this(context, publishableKey);
        this.requestInterceptor = request;
        this.responseInterceptor = response;
    }

    protected XenditImpl(final Context context, String publishableKey, Activity activity) {
        this(context, publishableKey);
        this.activity = activity;
    }

    protected XenditImpl(final Context context, String publishableKey) {
        this.context = context;
        this.publishableKey = publishableKey;
        this.gsonMapper = new Gson();

        // init sentry
        // Use the Sentry DSN (client key) from the Project Settings page on Sentry
        SentryAndroid.init(context, new Sentry.OptionsConfiguration<SentryAndroidOptions>() {
            @Override
            public void configure(SentryAndroidOptions sentryAndroidOptions) {
                sentryAndroidOptions.setDsn(DSN_SERVER);

                try {
                    PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
                    String versionName = pInfo.versionName;
                    String applicationName =
                        context.getApplicationInfo().loadLabel(context.getPackageManager()).toString();

                    sentryAndroidOptions.setTag("applicationName", applicationName);
                    sentryAndroidOptions.setTag("applicationVersionName", versionName);
                    sentryAndroidOptions.setTag("sdkVersionName", BuildConfig.VERSION_NAME);
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }

                sentryAndroidOptions.setBeforeSend(new SentryOptions.BeforeSendCallback() {
                    @Override
                    public SentryEvent execute(SentryEvent event, Hint hint) {
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
    }

    /**
     * Creates a single-use token. 3DS authentication will be bundled into this method unless you
     * set shouldAuthenticate as false.
     *
     * @param card A credit card
     * @param amount The amount you will eventually charge. This value is used to display to the
     * user in the 3DS authentication view.
     * @param tokenCallback The callback that will be called when the token creation completes or
     * fails
     */
    @Override
    public void createSingleUseToken(final Card card, final int amount, final TokenCallback tokenCallback) {
        String amountStr = Integer.toString(amount);

        createSingleOrMultipleUseToken(
            card, amountStr, true, "",
            false, null, null,
            null, null, null, null,
            tokenCallback);
    }

    /**
     * Creates a single-use token. 3DS authentication will be bundled into this method unless you
     * set shouldAuthenticate as false.
     *
     * @param card A credit card
     * @param amount The amount you will eventually charge. This value is used to display to the
     * user in the 3DS authentication view.
     * @param shouldAuthenticate A flag indicating if 3DS authentication are required for this token
     * @param tokenCallback The callback that will be called when the token creation completes or
     * fails
     */
    @Override
    public void createSingleUseToken(final Card card, final int amount, final boolean shouldAuthenticate,
        final TokenCallback tokenCallback) {
        String amountStr = Integer.toString(amount);

        createSingleOrMultipleUseToken(
            card, amountStr, shouldAuthenticate, "", false,
            null, null, null,
            null, null, null,
            tokenCallback);
    }

    /**
     * Creates a single-use token. 3DS authentication will be bundled into this method unless you
     * set shouldAuthenticate as false.
     *
     * @param card A credit card
     * @param amount The amount you will eventually charge. This value is used to display to the
     * user in the 3DS authentication view.
     * @param shouldAuthenticate A flag indicating if 3DS authentication are required for this token
     * @param onBehalfOf The onBehalfOf is sub account business id
     * @param tokenCallback The callback that will be called when the token creation completes or
     * fails
     */
    @Override
    public void createSingleUseToken(final Card card, final int amount, final boolean shouldAuthenticate,
        final String onBehalfOf, final TokenCallback tokenCallback) {
        String amountStr = Integer.toString(amount);

        createSingleOrMultipleUseToken(
            card, amountStr, shouldAuthenticate, onBehalfOf,
            false, null, null, null,
            null, null, null,
            tokenCallback);
    }

    /**
     * Creates a single-use token. 3DS authentication will be bundled into this method unless you
     * set shouldAuthenticate as false.
     *
     * @param card A credit card
     * @param amount The amount you will eventually charge. This value is used to display to the
     * user in the 3DS authentication view.
     * @param shouldAuthenticate A flag indicating if 3DS authentication are required for this token
     * @param onBehalfOf The onBehalfOf is sub account business id
     * @param billingDetails Billing details of the card
     * @param customer Customer object making the transaction
     * @param tokenCallback The callback that will be called when the token creation completes or
     * fails
     */
    @Override
    public void createSingleUseToken(
        final Card card,
        final int amount,
        final boolean shouldAuthenticate,
        final String onBehalfOf,
        final BillingDetails billingDetails,
        final Customer customer,
        TokenCallback tokenCallback) {
        String amountStr = Integer.toString(amount);

        createSingleOrMultipleUseToken(card, amountStr, shouldAuthenticate, onBehalfOf, false, billingDetails, customer,
            null, null, null, null, tokenCallback);
    }

    /**
     * Creates a single-use token. 3DS authentication will be bundled into this method unless you
     * set shouldAuthenticate as false.
     *
     * @param card A credit card
     * @param amount The amount you will eventually charge. This value is used to display to the
     * user in the 3DS authentication view.
     * @param shouldAuthenticate A flag indicating if 3DS authentication are required for this token
     * @param onBehalfOf The onBehalfOf is sub account business id
     * @param billingDetails Billing details of the card
     * @param customer Customer object making the transaction
     * @param currency Currency when requesting for 3DS authentication
     * @param tokenCallback The callback that will be called when the token creation completes or
     * fails
     */
    @Override
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

        createSingleOrMultipleUseToken(card, amountStr, shouldAuthenticate, onBehalfOf, false,
            billingDetails, customer,
            currency, null, null, null, tokenCallback);
    }

    /**
     * Creates a single-use token. 3DS authentication will be bundled into this method unless you
     * set shouldAuthenticate as false.
     *
     * @param card A credit card
     * @param amount The amount in string you will eventually charge. This value is used to display to the
     * user in the 3DS authentication view.
     * @param shouldAuthenticate A flag indicating if 3DS authentication are required for this token
     * @param onBehalfOf The onBehalfOf is sub account business id
     * @param billingDetails Billing details of the card
     * @param customer Customer object making the transaction
     * @param currency Currency when requesting for 3DS authentication
     * @param tokenCallback The callback that will be called when the token creation completes or
     * fails
     */
    @Override
    public void createSingleUseToken(
        final Card card,
        final String amount,
        final boolean shouldAuthenticate,
        final String onBehalfOf,
        final BillingDetails billingDetails,
        final Customer customer,
        final String currency,
        TokenCallback tokenCallback) {
        createSingleOrMultipleUseToken(card, amount, shouldAuthenticate, onBehalfOf, false,
            billingDetails, customer,
            currency, null, null, null, tokenCallback);
    }

    /**
     * Creates a single-use token. 3DS authentication will be bundled into this
     * method unless you
     * set shouldAuthenticate as false.
     *
     * @param card A credit card
     * @param amount The amount in string you will eventually charge.
     * This value is used to display to the
     * user in the 3DS authentication view.
     * @param shouldAuthenticate A flag indicating if 3DS authentication are
     * required for this token
     * @param currency Currency when requesting for 3DS authentication
     * @param tokenCallback The callback that will be called when the token
     * creation completes or
     * fails
     */
    @Override
    public void createSingleUseToken(
        final Card card,
        final String amount,
        final boolean shouldAuthenticate,
        final String currency,
        TokenCallback tokenCallback) {

        createSingleOrMultipleUseToken(
            card,
            amount,
            shouldAuthenticate,
            null, false, null, null,
            currency, null, null, null,
            tokenCallback);
    }

    /**
     * Creates a single-use token. 3DS authentication will be bundled into this method unless you
     * set shouldAuthenticate as false.
     *
     * @param tokenId ID of the token as the identifier
     * @param cardCvn CVV number of the card
     * @param tokenCallback The callback that will be called when the token creation completes or
     * fails
     */
    @Override
    public void createSingleUseToken(
        final BillingDetails billingDetails,
        final Customer customer,
        final String tokenId,
        final String cardCvn,
        TokenCallback tokenCallback) {
        createSingleOrMultipleUseToken(
            null, null, false, null,
            false, billingDetails, customer, null,
            tokenId, cardCvn, null,
            tokenCallback);
    }

    /**
     * Creates a single-use token. 3DS authentication will be bundled into this
     * method unless you
     * set shouldAuthenticate as false.
     *
     * @param card A credit card
     * @param amount The amount in string you will eventually charge.
     * This value is used to display to the
     * user in the 3DS authentication view.
     * @param shouldAuthenticate A flag indicating if 3DS authentication are
     * required for this token
     * @param onBehalfOf The onBehalfOf is sub account business id
     * @param billingDetails Billing details of the card
     * @param customer Customer object making the transaction
     * @param currency Currency when requesting for 3DS authentication
     * @param midLabel Mid label to perform authentication if tokenization
     * is bundled with authenticaiton. This is only
     * applicable for switcher mid.
     * @param tokenCallback The callback that will be called when the token
     * creation completes or
     * fails
     */
    @Override
    public void createSingleUseToken(
        final Card card,
        final String amount,
        final boolean shouldAuthenticate,
        final String onBehalfOf,
        final BillingDetails billingDetails,
        final Customer customer,
        final String currency,
        final String midLabel,
        TokenCallback tokenCallback) {
        createSingleOrMultipleUseToken(card, amount, shouldAuthenticate, onBehalfOf, false,
            billingDetails, customer,
            currency, null, null, midLabel, tokenCallback);
    }

    /**
     * Creates a multiple-use token. Authentication must be created separately if shouldAuthenticate
     * is true.
     *
     * @param card A credit card
     * @param tokenCallback The callback that will be called when the token creation completes or
     * fails
     */
    @Override
    public void createMultipleUseToken(final Card card, final TokenCallback tokenCallback) {
        createSingleOrMultipleUseToken(
            card, "0", false,
            "", true, null,
            null, null, null, null, null,
            tokenCallback);
    }

    /**
     * Creates a multiple-use token. Authentication must be created separately if shouldAuthenticate
     * is true.
     *
     * @param card A credit card
     * @param onBehalfOf The onBehalfOf is sub account business id
     * @param tokenCallback The callback that will be called when the token creation completes or
     * fails
     */
    @Override
    public void createMultipleUseToken(final Card card, final String onBehalfOf,
        final TokenCallback tokenCallback) {
        createSingleOrMultipleUseToken(
            card, "0", false, onBehalfOf,
            true, null, null, null,
            null, null, null,
            tokenCallback);
    }

    /**
     * Creates a multiple-use token. Authentication must be created separately if shouldAuthenticate
     * is true.
     *
     * @param card A credit card
     * @param onBehalfOf The onBehalfOf is sub account business id
     * @param billingDetails Billing details of the card
     * @param tokenCallback The callback that will be called when the token creation completes or
     * fails
     */
    @Override
    public void createMultipleUseToken(final Card card, final String onBehalfOf,
        BillingDetails billingDetails,
        final TokenCallback tokenCallback) {
        createSingleOrMultipleUseToken(
            card, "0", false, onBehalfOf, true,
            billingDetails, null,
            null, null, null, null,
            tokenCallback);
    }

    /**
     * Creates a multiple-use token. Authentication must be created separately if shouldAuthenticate
     * is true.
     *
     * @param card A credit card
     * @param onBehalfOf The onBehalfOf is sub account business id
     * @param billingDetails Billing details of the card
     * @param customer Customer linked to the payment method
     * @param tokenCallback The callback that will be called when the token creation completes or
     * fails
     */
    @Override
    public void createMultipleUseToken(final Card card, final String onBehalfOf,
        BillingDetails billingDetails, Customer customer, final TokenCallback tokenCallback) {
        createSingleOrMultipleUseToken(
            card, "0", false, onBehalfOf, true,
            billingDetails, customer, null,
            null, null, null,
            tokenCallback);
    }

    /**
     * Creates a multiple-use token. Authentication must be created separately if shouldAuthenticate
     * is true.
     *
     * @param card A credit card
     * @param onBehalfOf The onBehalfOf is sub account business id
     * @param billingDetails Billing details of the card
     * @param customer Customer linked to the payment method
     * @param midLabel Mid label to perform authentication if tokenization is
     * bundled with tokenization.
     * This argument is only applicable for switcher merchant.
     * @param tokenCallback The callback that will be called when the token creation completes or
     * fails
     */
    @Override
    public void createMultipleUseToken(final Card card, final String onBehalfOf,
        BillingDetails billingDetails, Customer customer, final String midLabel,
        final TokenCallback tokenCallback) {
        createSingleOrMultipleUseToken(
            card, "0", false, onBehalfOf, true,
            billingDetails, customer, null,
            null, null, midLabel,
            tokenCallback);
    }

    /**
     * Creates a multiple-use token. Authentication must be created separately if
     * shouldAuthenticate
     * is true.
     *
     * @param tokenId ID of the token as the identifier
     * @param cardCvn CVV number of the card
     * @param billingDetails Billing details of the card
     * @param customer Customer linked to the payment method
     * @param tokenCallback The callback that will be called when the token
     * creation completes or
     * fails
     */
    @Override
    public void createMultipleUseToken(
        final BillingDetails billingDetails,
        final Customer customer,
        final String tokenId,
        final String cardCvn,
        final TokenCallback tokenCallback) {
        createSingleOrMultipleUseToken(
            null, null, false, null,
            true, billingDetails, customer,
            null, tokenId, cardCvn, null,
            tokenCallback);
    }

    /**
     * Creates a multiple-use token. Authentication must be created separately if
     * shouldAuthenticate
     * is true.
     *
     * @param tokenId ID of the token as the identifier
     * @param cardCvn CVV number of the card
     * @param billingDetails Billing details of the card
     * @param customer Customer linked to the payment method
     * @param midLabel Mid label to perform authentication if tokenization is
     * bundled with tokenization.
     * This argument is only applicable for switcher merchant.
     * @param tokenCallback The callback that will be called when the token
     * creation completes or
     * fails
     */
    @Override
    public void createMultipleUseToken(
        final BillingDetails billingDetails,
        final Customer customer,
        final String tokenId,
        final String cardCvn,
        final String midLabel,
        final TokenCallback tokenCallback) {
        createSingleOrMultipleUseToken(
            null, null, false, null,
            true, billingDetails, customer,
            null, tokenId, cardCvn, midLabel,
            tokenCallback);
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
        final String tokenId,
        final String cardCvn,
        final String midLabel,
        final TokenCallback tokenCallback
    ) {
        /**
         * card must exist when doing normal tokenization
         * tokenId must exist when doing re-tokenization
         */
        if ((card != null || tokenId != null) && tokenCallback != null) {
            if (card != null && !CardValidator.isCardNumberValid(card.getCreditCardNumber())) {
                tokenCallback.onError(
                    new XenditError(context.getString(R.string.create_token_error_card_number)));
                return;
            }

            if (card != null && !CardValidator.isExpiryValid(card.getCardExpirationMonth(),
                card.getCardExpirationYear())) {
                tokenCallback.onError(
                    new XenditError(
                        context.getString(R.string.create_token_error_card_expiration)));
                return;
            }

            if (card != null && card.getCreditCardCVN() != null && !CardValidator.isCvnValid(
                card.getCreditCardCVN())) {
                tokenCallback.onError(
                    new XenditError(context.getString(R.string.create_token_error_card_cvn)));
                return;
            }

            if (card != null
                && card.getCreditCardCVN() != null
                && !CardValidator.isCvnValidForCardType(
                card.getCreditCardCVN(), card.getCreditCardNumber())) {
                tokenCallback.onError(
                    new XenditError(context.getString(R.string.error_card_cvn_invalid_for_type)));
                return;
            }

            if (cardCvn != null && !CardValidator.isCvnValid(cardCvn)) {
                tokenCallback.onError(
                    new XenditError(context.getString(R.string.create_token_error_card_cvn)));
                return;
            }

            createCreditCardToken(card, amount, shouldAuthenticate, onBehalfOf, isMultipleUse,
                billingDetails, customer,
                currency, tokenId, cardCvn, midLabel, tokenCallback);
        }
    }

    /**
     * Store CVN method will perform store cvn using an existing tokenId (retokenization).
     * This method is commonly used for performing re-tokenization on subsequent usage of a multi-use token in the purpose of re-caching cardCVN.
     *
     * @param tokenId is a previously created Xendit multiple-use tokenId. Required field.
     * @param cardCvn is card cvn code linked to the tokenId created. Required field.
     * @param billingDetails Billing details of the card
     * @param customer Customer linked to the payment method
     * @param onBehalfOf The onBehalfOf is sub account business id. This field is used for merchants utilizing xenPlatform feature.
     * @param storeCVNCallback The callback that will be called when the token re-creation completes or
     * fails
     */
    @Override
    public void storeCVN(
        final String tokenId,
        final String cardCvn,
        final BillingDetails billingDetails,
        final Customer customer,
        final String onBehalfOf,
        final StoreCVNCallback storeCVNCallback
    ) {
        NetworkHandler<Token> handler = new NetworkHandler<Token>().setResultListener(
            new ResultListener<Token>() {
                @Override
                public void onSuccess(Token token) {
                    TrackerController tracker = getTracker(context);
                    tracker.track(Structured.builder()
                        .category("api-request")
                        .action("store-cvn")
                        .label("Store CVN")
                        .build());

                    storeCVNCallback.onSuccess(token);
                }

                @Override
                public void onFailure(NetworkError error) {
                    storeCVNCallback.onError(new XenditError(error));
                }
            });

        BaseRequest<Token> request = buildBaseRequest(
            Request.Method.POST,
            CREATE_CREDIT_CARD_TOKEN_URL,
            onBehalfOf == null || onBehalfOf.equals("") ? "" : onBehalfOf,
            Token.class,
            new DefaultResponseHandler<>(handler, (Interceptor<Token>)responseInterceptor)
        );

        if (tokenId == null || tokenId.equals("")) {
            storeCVNCallback.onError(new XenditError("TokenId is required"));
            return;
        }
        request.addParam("token_id", tokenId);

        if (cardCvn == null || cardCvn.equals("")) {
            storeCVNCallback.onError(new XenditError("CVN is required"));
            return;
        } else {
            if (!CardValidator.isCvnValid(cardCvn)) {
                storeCVNCallback.onError(new XenditError("CVN in invalid"));
                return;
            }
            request.addParam("card_cvn", cardCvn);
        }

        if (customer != null) {
            request.addJsonParam("customer", gsonMapper.toJsonTree(customer));
        }

        if (billingDetails != null) {
            request.addJsonParam("billing_details", gsonMapper.toJsonTree(billingDetails));
        }

        sendRequest(request, handler);
    }

    /**
     * Creates a 3DS authentication for a multiple-use token
     *
     * @param tokenId A multi-use token id
     * @param amount Amount of money to be authenticated
     * @param currency Currency of the amount
     * @param authenticationCallback The callback that will be called when the
     * authentication completes or fails
     */
    @Override
    public void createAuthentication(final String tokenId, final int amount, final String currency,
        final AuthenticationCallback authenticationCallback) {
        final String amountStr = Integer.toString(amount);
        createAuthenticationInternal(tokenId, amountStr, currency, null, null, null, null,
            authenticationCallback);
    }

    /**
     * Creates a 3DS authentication for a multiple-use token
     *
     * @param tokenId A multi-use token id
     * @param amount Amount of money to be authenticated
     * @param currency Currency of the amount
     * @param cardHolderData Additional information of the card holder data
     * @param authenticationCallback The callback that will be called when the
     * authentication completes or fails
     */
    @Override
    public void createAuthentication(final String tokenId, final int amount, final String currency,
        final CardHolderData cardHolderData, final AuthenticationCallback authenticationCallback) {
        final String amountStr = Integer.toString(amount);
        createAuthenticationInternal(tokenId, amountStr, currency, null, null, null, cardHolderData,
            authenticationCallback);
    }

    /**
     * Creates a 3DS authentication for a multiple-use token
     *
     * @param tokenId The id of a multiple-use token
     * @param amount The amount that will eventually be charged. This number is displayed to the
     * user in the 3DS authentication view
     * @param authenticationCallback The callback that will be called when the authentication
     * creation completes or fails
     */
    @Override
    public void createAuthentication(final String tokenId, final int amount,
        final AuthenticationCallback authenticationCallback) {
        final String amountStr = Integer.toString(amount);
        createAuthenticationInternal(tokenId, amountStr, null, null, null, null, null,
            authenticationCallback);
    }

    /**
     * Creates a 3DS authentication for a multiple-use token
     *
     * @param tokenId The id of a multiple-use token
     * @param amount The amount that will eventually be charged. This number is displayed to the
     * user in the 3DS authentication view
     * @param cardHolderData Additional information of the card holder
     * @param authenticationCallback The callback that will be called when the
     * authentication creation completes or fails
     */
    @Override
    public void createAuthentication(final String tokenId, final int amount,
        CardHolderData cardHolderData, final AuthenticationCallback authenticationCallback) {
        final String amountStr = Integer.toString(amount);
        createAuthenticationInternal(tokenId, amountStr, null, null, null, null, cardHolderData,
            authenticationCallback);
    }

    /**
     * Creates a 3DS authentication for a multiple-use token
     *
     * @param tokenId A multi-use token id
     * @param amount Amount of money to be authenticated
     * @param currency Currency of the amount
     * @param cardCvn CVV/CVN collected from the card holder
     * @param authenticationCallback The callback that will be called when the
     * authentication completes or fails
     */
    @Override
    public void createAuthentication(final String tokenId, final String amount,
        final String currency,
        final String cardCvn, final AuthenticationCallback authenticationCallback) {
        createAuthenticationInternal(tokenId, amount, currency, cardCvn, null, null, null,
            authenticationCallback);
    }

    /**
     * Creates a 3DS authentication for a multiple-use token
     *
     * @param tokenId A multi-use token id
     * @param amount Amount of money to be authenticated
     * @param currency Currency of the amount
     * @param cardCvn CVV/CVN collected from the card holder
     * @param cardHolderData Additional information of the card holder data
     * @param authenticationCallback The callback that will be called when the authentication completes or
     * fails
     */
    @Override
    public void createAuthentication(final String tokenId, final String amount,
        final String currency,
        final String cardCvn, final CardHolderData cardHolderData,
        final AuthenticationCallback authenticationCallback) {
        createAuthenticationInternal(tokenId, amount, currency, cardCvn, null, null, cardHolderData,
            authenticationCallback);
    }

    /**
     * Creates a 3DS authentication for a multiple-use token
     *
     * @param tokenId A multi-use token id
     * @param amount Amount of money to be authenticated
     * @param currency Currency of the amount
     * @param authenticationCallback The callback that will be called when the
     * authentication completes or fails
     */
    @Override
    public void createAuthentication(final String tokenId, final String amount,
        final String currency,
        final AuthenticationCallback authenticationCallback) {
        createAuthenticationInternal(tokenId, amount, currency, null, null, null, null,
            authenticationCallback);
    }

    /**
     * Creates a 3DS authentication for a multiple-use token
     *
     * @param tokenId A multi-use token id
     * @param amount Amount of money to be authenticated
     * @param currency Currency of the amount
     * @param cardHolderData Additional information of the card holder data
     * @param authenticationCallback The callback that will be called when the
     * authentication completes or fails
     */
    @Override
    public void createAuthentication(final String tokenId, final String amount,
        final String currency,
        final CardHolderData cardHolderData, final AuthenticationCallback authenticationCallback) {
        createAuthenticationInternal(tokenId, amount, currency, null, null, null, cardHolderData,
            authenticationCallback);
    }

    /**
     * Creates a 3DS authentication for a multiple-use token
     *
     * @param tokenId A multi-use token id
     * @param amount Amount of money to be authenticated
     * @param currency Currency of the amount
     * @param cardCvn CVV/CVN collected from the card holder
     * @param onBehalfOf Business Id to call the API on behalf of
     * (Applicable to Platform merchants)
     * @param authenticationCallback The callback that will be called when the
     * authentication completes or fails
     */
    @Override
    public void createAuthentication(final String tokenId, final String amount,
        final String currency,
        final String cardCvn, final String onBehalfOf,
        final AuthenticationCallback authenticationCallback) {
        createAuthenticationInternal(tokenId, amount, currency, cardCvn, onBehalfOf, null, null,
            authenticationCallback);
    }

    /**
     * Creates a 3DS authentication for a multiple-use token
     *
     * @param tokenId A multi-use token id
     * @param amount Amount of money to be authenticated
     * @param currency Currency of the amount
     * @param cardCvn CVV/CVN collected from the card holder
     * @param cardHolderData Additional information of the card holder data
     * @param onBehalfOf Business Id to call the API on behalf of
     * (Applicable to Platform merchants)
     * @param authenticationCallback The callback that will be called when the
     * authentication completes or fails
     */
    @Override
    public void createAuthentication(final String tokenId, final String amount,
        final String currency,
        final String cardCvn, final CardHolderData cardHolderData, final String onBehalfOf,
        final AuthenticationCallback authenticationCallback) {
        createAuthenticationInternal(tokenId, amount, currency, cardCvn, onBehalfOf, null,
            cardHolderData, authenticationCallback);
    }

    /**
     * Creates a 3DS authentication for a multiple-use token
     *
     * @param tokenId A multi-use token id
     * @param amount Amount of money to be authenticated
     * @param currency Currency of the amount
     * @param cardCvn CVV/CVN collected from the card holder
     * @param onBehalfOf Business Id to call the API on behalf of
     * (Applicable to Platform merchants)
     * @param midLabel Mid label to perform authentication if
     * tokenization is bundled with authenticaiton.
     * This is only applicable for switcher mid.
     * @param authenticationCallback The callback that will be called when the
     * authentication completes or
     * fails
     */
    @Override
    public void createAuthentication(final String tokenId, final String amount,
        final String currency,
        final String cardCvn, final String onBehalfOf, final String midLabel,
        final AuthenticationCallback authenticationCallback) {
        createAuthenticationInternal(tokenId, amount, currency, cardCvn, onBehalfOf, midLabel, null,
            authenticationCallback);
    }

    /**
     * Creates a 3DS authentication for a multiple-use token
     *
     * @param tokenId A multi-use token id
     * @param amount Amount of money to be authenticated
     * @param currency Currency of the amount
     * @param cardCvn CVV/CVN collected from the card holder
     * @param cardHolderData Additional information of the card holder data
     * @param onBehalfOf Business Id to call the API on behalf of
     * (Applicable to Platform merchants)
     * @param midLabel Mid label to perform authentication if
     * tokenization is bundled with authenticaiton.
     * This is only applicable for switcher mid.
     * @param authenticationCallback The callback that will be called when the
     * authentication completes or
     * fails
     */
    @Override
    public void createAuthentication(final String tokenId, final String amount,
        final String currency,
        final String cardCvn, final CardHolderData cardHolderData, final String onBehalfOf,
        final String midLabel,
        final AuthenticationCallback authenticationCallback) {
        createAuthenticationInternal(tokenId, amount, currency, cardCvn, onBehalfOf, midLabel,
            cardHolderData, authenticationCallback);
    }

    private void createAuthenticationInternal(final String tokenId, final String amount,
        final String currency,
        final String cardCvn, final String onBehalfOf, final String midLabel,
        final CardHolderData cardHolderData,
        final AuthenticationCallback authenticationCallback) {
        if (tokenId == null || tokenId.equals("")) {
            authenticationCallback.onError(
                new XenditError(context.getString(R.string.create_token_error_validation)));
            return;
        }

        if (Double.parseDouble(amount) < 0) {
            authenticationCallback.onError(
                new XenditError(context.getString(R.string.create_token_error_validation)));
            return;
        }

        _createAuthentication(tokenId, amount, currency, cardCvn, onBehalfOf, midLabel,
            cardHolderData,
            new NetworkHandler<Authentication>().setResultListener(
                new ResultListener<Authentication>() {
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
                            context.startActivity(
                                XenditActivity.getLaunchIntent(context, authentication));
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

    private void get3DSRecommendation(String tokenId, final AuthenticatedToken authentication,
        final TokenCallback callback) {
        _get3DSRecommendation(tokenId,
            new NetworkHandler<ThreeDSRecommendation>().setResultListener(
                new ResultListener<ThreeDSRecommendation>() {
                    @Override
                    public void onSuccess(ThreeDSRecommendation rec) {
                        TrackerController tracker = getTracker(context);
                        tracker.track(Structured.builder()
                            .category("api-request")
                            .action("get-3ds-recommendation")
                            .label("Get 3DS Recommendation")
                            .build());

                        callback.onSuccess(new Token(authentication, rec));
                    }

                    @Override
                    public void onFailure(NetworkError error) {
                        callback.onSuccess(new Token(authentication));
                    }
                }));
    }

    // createCreditCardToken with 5 arguments
    @Override
    public void createCreditCardToken(
        Card card,
        String amount,
        boolean shouldAuthenticate,
        boolean isMultipleUse,
        final TokenCallback tokenCallback) {

        _createToken(
            card,
            amount,
            shouldAuthenticate,
            "",
            isMultipleUse,
            null,
            null,
            null,
            null,
            null,
            null,
            new NetworkHandler<AuthenticatedToken>().setResultListener(
                new ResultListener<AuthenticatedToken>() {
                    @Override
                    public void onSuccess(AuthenticatedToken authentication) {
                        TrackerController tracker = getTracker(context);
                        tracker.track(Structured.builder()
                            .category("api-request")
                            .action("create-token")
                            .label("Create Token")
                            .build());

                        handle3ds1Tokenization(authentication, tokenCallback);
                    }

                    @Override
                    public void onFailure(NetworkError error) {
                        tokenCallback.onError(new XenditError(error));
                    }
                }));
    }

    // createCreditCardToken with 6 arguments
    @Override
    public void createCreditCardToken(
        final Card card,
        final String amount,
        boolean shouldAuthenticate,
        final String onBehalfOf,
        boolean isMultipleUse,
        final TokenCallback tokenCallback) {

        _createToken(
            card,
            amount,
            shouldAuthenticate,
            onBehalfOf,
            isMultipleUse,
            null,
            null,
            null,
            null,
            null,
            null,
            new NetworkHandler<AuthenticatedToken>().setResultListener(
                new ResultListener<AuthenticatedToken>() {
                    @Override
                    public void onSuccess(final AuthenticatedToken authentication) {
                        TrackerController tracker = getTracker(context);
                        tracker.track(Structured.builder()
                            .category("api-request")
                            .action("create-token")
                            .label("Create Token")
                            .build());

                        handle3ds1Tokenization(authentication, tokenCallback);
                    }

                    @Override
                    public void onFailure(NetworkError error) {
                        tokenCallback.onError(new XenditError(error));
                    }
                }));
    }

    // createCreditCardToken with 11 arguments
    @Override
    public void createCreditCardToken(
        final Card card,
        final String amount,
        boolean shouldAuthenticate,
        final String onBehalfOf,
        boolean isMultipleUse,
        BillingDetails billingDetails,
        Customer customer,
        final String currency,
        final String tokenId,
        final String cardCvn,
        final TokenCallback tokenCallback) {

        _createToken(
            card,
            amount,
            shouldAuthenticate,
            onBehalfOf,
            isMultipleUse,
            billingDetails,
            customer,
            currency,
            tokenId,
            cardCvn,
            null,
            new NetworkHandler<AuthenticatedToken>().setResultListener(
                new ResultListener<AuthenticatedToken>() {
                    @Override
                    public void onSuccess(final AuthenticatedToken authentication) {
                        TrackerController tracker = getTracker(context);
                        tracker.track(Structured.builder()
                            .category("api-request")
                            .action("create-token")
                            .label("Create Token")
                            .build());

                        handle3ds1Tokenization(authentication, tokenCallback);
                    }

                    @Override
                    public void onFailure(NetworkError error) {
                        tokenCallback.onError(new XenditError(error));
                    }
                }));
    }

    // createCreditCardToken with 12 arguments
    @Override
    public void createCreditCardToken(
        final Card card,
        final String amount,
        boolean shouldAuthenticate,
        final String onBehalfOf,
        boolean isMultipleUse,
        BillingDetails billingDetails,
        Customer customer,
        final String currency,
        final String tokenId,
        final String cardCvn,
        final String midLabel,
        final TokenCallback tokenCallback) {

        _createToken(
            card,
            amount,
            shouldAuthenticate,
            onBehalfOf,
            isMultipleUse,
            billingDetails,
            customer,
            currency,
            tokenId,
            cardCvn,
            midLabel,
            new NetworkHandler<AuthenticatedToken>().setResultListener(
                new ResultListener<AuthenticatedToken>() {
                    @Override
                    public void onSuccess(final AuthenticatedToken authentication) {
                        TrackerController tracker = getTracker(context);
                        tracker.track(Structured.builder()
                            .category("api-request")
                            .action("create-token")
                            .label("Create Token")
                            .build());

                        handle3ds1Tokenization(authentication, tokenCallback);
                    }

                    @Override
                    public void onFailure(NetworkError error) {
                        tokenCallback.onError(new XenditError(error));
                    }
                }));
    }

    @Override
    public void unregisterXenBroadcastReceiver(BroadcastReceiver receiver) {
        try {
            context.unregisterReceiver(receiver);
        } catch (Exception ignored) {
        }
    }

    @Override
    public void unregisterAllBroadcastReceiver() {
        if (authenticationBroadcastReceiver != null) {
            unregisterXenBroadcastReceiver(authenticationBroadcastReceiver);
            authenticationBroadcastReceiver = null;
        }

        if (tokenBroadcastReceiver != null) {
            unregisterXenBroadcastReceiver(tokenBroadcastReceiver);
            tokenBroadcastReceiver = null;
        }

        if (authenticatedTokenBroadcastReceiver != null) {
            unregisterXenBroadcastReceiver(authenticatedTokenBroadcastReceiver);
            authenticatedTokenBroadcastReceiver = null;
        }
    }

    private void registerBroadcastReceiver(final AuthenticationCallback authenticationCallback) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            @SuppressLint("UnspecifiedRegisterReceiverFlag")
            public void run() {
                if (authenticationBroadcastReceiver != null) {
                    unregisterXenBroadcastReceiver(authenticationBroadcastReceiver);
                    authenticationBroadcastReceiver = null;
                }

                authenticationBroadcastReceiver =
                    new AuthenticationBroadcastReceiver(authenticationCallback);
                // if version is over 33
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                    context.registerReceiver(authenticationBroadcastReceiver,
                        new IntentFilter(ACTION_KEY),
                        Context.RECEIVER_EXPORTED);
                } else {
                    context.registerReceiver(authenticationBroadcastReceiver,
                        new IntentFilter(ACTION_KEY));
                }
            }
        });
    }

    private void registerBroadcastReceiver(final TokenCallback tokenCallback) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {

            @SuppressLint("UnspecifiedRegisterReceiverFlag")
            @Override
            public void run() {
                if (tokenBroadcastReceiver != null) {
                    unregisterXenBroadcastReceiver(tokenBroadcastReceiver);
                    tokenBroadcastReceiver = null;
                }

                tokenBroadcastReceiver = new TokenBroadcastReceiver(tokenCallback);
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                    context.registerReceiver(tokenBroadcastReceiver, new IntentFilter(ACTION_KEY),
                        Context.RECEIVER_EXPORTED);
                } else {
                    context.registerReceiver(tokenBroadcastReceiver, new IntentFilter(ACTION_KEY));
                }
            }
        });
    }

    private void registerBroadcastReceiverAuthenticatedToken(final TokenCallback tokenCallback) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @SuppressLint("UnspecifiedRegisterReceiverFlag")
            @Override
            public void run() {
                if (authenticatedTokenBroadcastReceiver != null) {
                    unregisterXenBroadcastReceiver(authenticatedTokenBroadcastReceiver);
                    authenticatedTokenBroadcastReceiver = null;
                }

                authenticatedTokenBroadcastReceiver =
                    new AuthenticatedTokenBroadcastReceiver(tokenCallback);
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                    context.registerReceiver(authenticatedTokenBroadcastReceiver,
                        new IntentFilter(ACTION_KEY), Context.RECEIVER_EXPORTED);
                } else {
                    context.registerReceiver(authenticatedTokenBroadcastReceiver,
                        new IntentFilter(ACTION_KEY));
                }
            }
        });
    }

    private void _createToken(
        Card card,
        String amount,
        boolean shouldAuthenticate,
        String onBehalfOf,
        boolean isMultipleUse,
        BillingDetails billingDetails,
        Customer customer,
        String currency,
        String tokenId,
        String cardCvn,
        String midLabel,
        NetworkHandler<AuthenticatedToken> handler) {
        BaseRequest<AuthenticatedToken> request =
            buildBaseRequest(Request.Method.POST, CREATE_CREDIT_CARD_TOKEN_URL, onBehalfOf,
                AuthenticatedToken.class, new DefaultResponseHandler<>(handler, (Interceptor<AuthenticatedToken>)responseInterceptor));

        JsonObject cardData = new JsonObject();
        if (card != null) {
            cardData.addProperty("account_number", card.getCreditCardNumber());
            cardData.addProperty("exp_year", card.getCardExpirationYear());
            cardData.addProperty("exp_month", card.getCardExpirationMonth());
            cardData.addProperty("cvn", card.getCreditCardCVN());
            CardHolderData cardHolderData = card.getCardHolder();
            if (cardHolderData != null) {
                cardData.addProperty("card_holder_first_name", cardHolderData.getFirstName());
                cardData.addProperty("card_holder_last_name", cardHolderData.getLastName());
                cardData.addProperty("card_holder_email", cardHolderData.getEmail());
                cardData.addProperty("card_holder_phone_number", cardHolderData.getPhoneNumber());
            }
            request.addJsonParam("card_data", cardData);
        }

        // If tokenId doesn't exist add is_single_use and should_authenticate
        if (tokenId == null) {
            request.addParam("is_single_use", String.valueOf(!isMultipleUse));
            request.addParam("should_authenticate", String.valueOf(shouldAuthenticate));
        }

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

        if (tokenId != null) {
            request.addParam("token_id", tokenId);
        }

        if (cardCvn != null) {
            request.addParam("card_cvn", cardCvn);
        }

        if (midLabel != null) {
            request.addParam("mid_label", midLabel);
        }

        sendRequest(request, handler);
    }

    private void _get3DSRecommendation(String tokenId,
        NetworkHandler<ThreeDSRecommendation> handler) {
        String url = GET_3DS_URL + "?token_id=" + tokenId;

        BaseRequest<ThreeDSRecommendation> request =
            buildBaseRequest(Request.Method.GET, url, null, ThreeDSRecommendation.class,
                new DefaultResponseHandler<>(handler, (Interceptor<ThreeDSRecommendation>)responseInterceptor));
        sendRequest(request, handler);
    }

    private void _createAuthentication(String tokenId, String amount, String currency,
        String cardCvn,
        String onBehalfOf, String midLabel, CardHolderData cardHolder,
        NetworkHandler<Authentication> handler) {
        String requestUrl = CREATE_CREDIT_CARD_URL + "/" + tokenId + "/authentications";

        BaseRequest<Authentication> request =
            buildBaseRequest(Request.Method.POST, requestUrl, onBehalfOf, Authentication.class,
                new DefaultResponseHandler<>(handler, (Interceptor<Authentication>)responseInterceptor));
        request.addParam("amount", amount);
        if (currency != null && !currency.isEmpty()) {
            request.addParam("currency", currency);
        }
        if (cardCvn != null && !cardCvn.isEmpty()) {
            request.addParam("card_cvn", cardCvn);
        }
        if (cardHolder != null) {
            JsonObject cardHolderData = new JsonObject();
            cardHolderData.addProperty("card_holder_first_name", cardHolder.getFirstName());
            cardHolderData.addProperty("card_holder_last_name", cardHolder.getLastName());
            cardHolderData.addProperty("card_holder_email", cardHolder.getEmail());
            cardHolderData.addProperty("card_holder_phone_number", cardHolder.getPhoneNumber());
            request.addJsonParam("card_data", cardHolderData);
        }

        if (midLabel != null) {
            request.addParam("mid_label", midLabel);
        }

        sendRequest(request, handler);
    }

    private void handle3ds1Tokenization(AuthenticatedToken authentication,
        TokenCallback tokenCallback) {
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

    private void handleAuthenticatedToken(String tokenId, Authentication authenticatedToken,
        TokenCallback tokenCallback) {
        if (authenticatedToken.getStatus().equalsIgnoreCase("VERIFIED")
            || authenticatedToken.getStatus().equalsIgnoreCase("FAILED")) {
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
            if (requestInterceptor != null) {
                requestInterceptor.intercept(request);
            }
            requestQueue.add(request);
        } else if (handler != null) {
            handler.handleError(new ConnectionError());
        }
    }

    private boolean isConnectionAvailable() {
        if (PermissionUtils.hasPermission(context, Manifest.permission.ACCESS_NETWORK_STATE)) {
            @SuppressLint("MissingPermission") NetworkInfo activeNetwork =
                connectivityManager.getActiveNetworkInfo();
            return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        } else {
            return false;
        }
    }

    private boolean getEnvironment() {
        String publishKey = publishableKey.toUpperCase();
        return publishKey.contains("PRODUCTION");
    }

    private BaseRequest buildBaseRequest(int method, String url, String onBehalfOf, Type type,
        DefaultResponseHandler handler) {
        String encodedKey = encodeBase64(publishableKey + ":");
        String basicAuthCredentials = "Basic " + encodedKey;
        BaseRequest request = new BaseRequest<>(method, url, type, handler);
        if (onBehalfOf != null && !onBehalfOf.isEmpty()) {
            request.addHeader("for-user-id", onBehalfOf);
        }
        request.addHeader("Authorization", basicAuthCredentials.replace("\n", ""));
        request.addHeader("x-client-identifier", CLIENT_IDENTIFIER);
        request.addHeader("client-version", BuildConfig.VERSION_NAME);
        request.addHeader("client-type", CLIENT_TYPE);
        return request;
    }
}
