package com.xendit;

import android.content.Context;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.google.gson.JsonObject;
import com.xendit.Models.Authentication;
import com.xendit.Models.Card;
import com.xendit.Models.Token;
import com.xendit.Models.TokenConfiguration;
import com.xendit.Models.TokenCreditCard;
import com.xendit.Models.XenditError;
import com.xendit.network.BaseRequest;
import com.xendit.network.DefaultResponseHandler;
import com.xendit.network.NetworkHandler;
import com.xendit.network.errors.ConnectionError;
import com.xendit.network.errors.NetworkError;
import com.xendit.network.interfaces.ResultListener;
import com.xendit.utils.CardValidator;

import java.io.UnsupportedEncodingException;

/**
 * Created by Dimon_GDA on 3/7/17.
 */

public class Xendit {

    private static final String PRODUCTION_XENDIT_BASE_URL = "https://api.xendit.co";
    private static final String TOKENIZE_CREDIT_CARD_URL = "/cybersource/flex/v1/tokens?apikey=";
    private static final String CREATE_CREDIT_CARD_URL = PRODUCTION_XENDIT_BASE_URL + "/credit_card_tokens";
    private static final String GET_TOKEN_CONFIGURATION_URL = PRODUCTION_XENDIT_BASE_URL + "/credit_card_tokenization_configuration";

    static final String ACTION_KEY = "ACTION_KEY";

    private Context context;
    private String publishableKey;
    private RequestQueue requestQueue;
    private ConnectivityManager connectivityManager;

    public Xendit(Context context, String publishableKey) {
        this.context = context;
        this.publishableKey = publishableKey;
        requestQueue = Volley.newRequestQueue(context);
        connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
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

        createSingleOrMultipleUseToken(card, amountStr, true, false, tokenCallback);
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

        createSingleOrMultipleUseToken(card, amountStr, shouldAuthenticate, false, tokenCallback);
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
        createSingleOrMultipleUseToken(card, "0", false, true, tokenCallback);
    }

    /**
     * @deprecated As of v.2.0.0.
     * Replaced by {@link #createSingleUseToken(Card, int, boolean, TokenCallback)} for single use token
     * and {@link #createMultipleUseToken(Card, TokenCallback)} for multiple use token
     */
    @Deprecated
    public void createToken(final Card card, final String amount, final boolean isMultipleUse, final TokenCallback tokenCallback) {
        createSingleOrMultipleUseToken(card, amount, true, isMultipleUse, tokenCallback);
    }

    private void createSingleOrMultipleUseToken(final Card card, final String amount, final boolean shouldAuthenticate, final boolean isMultipleUse, final TokenCallback tokenCallback) {
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

            getTokenizationConfiguration(new NetworkHandler<TokenConfiguration>().setResultListener(new ResultListener<TokenConfiguration>() {
                @Override
                public void onSuccess(TokenConfiguration tokenConfiguration) {
                    tokenizeCreditCardRequest(tokenConfiguration, card, amount, shouldAuthenticate, isMultipleUse, tokenCallback);
                }

                @Override
                public void onFailure(NetworkError error) {
                    tokenCallback.onError(new XenditError(error));
                }
            }));
        }
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
        if (tokenId == null || tokenId.equals("")) {
            authenticationCallback.onError(new XenditError(context.getString(R.string.create_token_error_validation)));
            return;
        }

        if (amount <= 0) {
            authenticationCallback.onError(new XenditError(context.getString(R.string.create_token_error_validation)));
            return;
        }

        String amountStr = Integer.toString(amount);

        _createAuthentication(tokenId, amountStr, new NetworkHandler<Authentication>().setResultListener(new ResultListener<Authentication>() {
            @Override
            public void onSuccess(Authentication authentication) {
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

    /**
     * @deprecated As of v.2.0.0, replaced by {@link #createAuthentication(String, int, AuthenticationCallback)}
     * cardCvn can be sent at creating charge
     */
    @Deprecated
    public void createAuthentication(final String tokenId, final String cardCvn, final String amount, final TokenCallback tokenCallback) {
            if (tokenId == null || tokenId == "") {
                tokenCallback.onError(new XenditError(context.getString(R.string.create_token_error_validation)));
                return;
            }

            if (amount == null || Integer.parseInt(amount) <= 0) {
                tokenCallback.onError(new XenditError(context.getString(R.string.create_token_error_validation)));
                return;
            }

            if (!isCvnValid(cardCvn)) {
                tokenCallback.onError(new XenditError(context.getString(R.string.create_token_error_card_cvn)));
                return;
            }

            _createAuthentication(tokenId, amount, new NetworkHandler<Authentication>().setResultListener(new ResultListener<Authentication>() {
                @Override
                public void onSuccess(Authentication authentication) {
                    if (!authentication.getStatus().equalsIgnoreCase("VERIFIED")) {
                        registerBroadcastReceiver(tokenCallback);
                        context.startActivity(XenditActivity.getLaunchIntent(context, authentication));
                    } else {
                        tokenCallback.onSuccess(new Token(authentication));
                    }
                }

                @Override
                public void onFailure(NetworkError error) {
                    tokenCallback.onError(new XenditError(error));
                }
            }));
    }

    private void tokenizeCreditCardRequest(final TokenConfiguration tokenConfiguration, final Card card, final String amount, final boolean shouldAuthenticate, final boolean isMultipleUse, final TokenCallback tokenCallback) {
        tokenizeCreditCard(tokenConfiguration, card, new NetworkHandler<TokenCreditCard>().setResultListener(new ResultListener<TokenCreditCard>() {
            @Override
            public void onSuccess(TokenCreditCard tokenCreditCard) {
                _createCreditCardToken(card, tokenCreditCard.getToken(), amount, shouldAuthenticate, isMultipleUse, tokenCallback);
            }

            @Override
            public void onFailure(NetworkError error) {
                tokenCallback.onError(new XenditError(error));
            }
        }));
    }

    /**
     * @deprecated Not for public use.
     */
    @Deprecated
    public void createCreditCardToken(Card card, final String token, String amount, boolean isMultipleUse, final TokenCallback tokenCallback) {
        if (!isCvnValid(card.getCreditCardCVN())) {
            tokenCallback.onError(new XenditError(context.getString(R.string.create_token_error_card_cvn)));
            return;
        }

        _createToken(card, token, amount, true, isMultipleUse, new NetworkHandler<Authentication>().setResultListener(new ResultListener<Authentication>() {
            @Override
            public void onSuccess(Authentication authentication) {
                if (!authentication.getStatus().equalsIgnoreCase("VERIFIED")) {
                    registerBroadcastReceiver(tokenCallback);
                    context.startActivity(XenditActivity.getLaunchIntent(context, authentication));
                } else {
                    tokenCallback.onSuccess(new Token(authentication));
                }
            }

            @Override
            public void onFailure(NetworkError error) {
                tokenCallback.onError(new XenditError(error));
            }
        }));
    }

    private void _createCreditCardToken(Card card, final String token, String amount, boolean shouldAuthenticate, boolean isMultipleUse, final TokenCallback tokenCallback) {
        _createToken(card, token, amount, shouldAuthenticate, isMultipleUse, new NetworkHandler<Authentication>().setResultListener(new ResultListener<Authentication>() {
            @Override
            public void onSuccess(Authentication authentication) {
                if (!authentication.getStatus().equalsIgnoreCase("VERIFIED")) {
                    registerBroadcastReceiver(tokenCallback);
                    context.startActivity(XenditActivity.getLaunchIntent(context, authentication));
                } else {
                    tokenCallback.onSuccess(new Token(authentication));
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

    private void getTokenizationConfiguration(NetworkHandler<TokenConfiguration> handler) {
        String encodedKey = encodeBase64(publishableKey + ":");
        String basicAuthCredentials = "Basic " + encodedKey;
        BaseRequest request = new BaseRequest<>(Request.Method.GET, GET_TOKEN_CONFIGURATION_URL, TokenConfiguration.class, new DefaultResponseHandler<>(handler));
        request.addHeader("Authorization", basicAuthCredentials.replace("\n", ""));
        sendRequest(request, handler);
    }

    private void tokenizeCreditCard(TokenConfiguration tokenConfig, Card card, NetworkHandler<TokenCreditCard> handler) {
        String baseUrl = getEnvironment() ? tokenConfig.getFlexProductionUrl() : tokenConfig.getFlexDevelopmentUrl();
        String flexUrl = baseUrl + TOKENIZE_CREDIT_CARD_URL + tokenConfig.getFlexApiKey();

        BaseRequest request = new BaseRequest<>(Request.Method.POST, flexUrl, TokenCreditCard.class, new DefaultResponseHandler<>(handler));

        JsonObject cardInfoJson = new JsonObject();
        cardInfoJson.addProperty("cardNumber", card.getCreditCardNumber());
        cardInfoJson.addProperty("cardExpirationMonth", card.getCardExpirationMonth());
        cardInfoJson.addProperty("cardExpirationYear", card.getCardExpirationYear());
        cardInfoJson.addProperty("cardType", CardValidator.getCardType(card.getCreditCardNumber()).getCardTypeKey());

        request.addParam("keyId", tokenConfig.getTokenizationAuthKeyId());
        request.addJsonParam("cardInfo", cardInfoJson);
        sendRequest(request, handler);
    }

    private void _createToken(Card card, String token, String amount, boolean shouldAuthenticate, boolean isMultipleUse, NetworkHandler<Authentication> handler) {
        String encodedKey = encodeBase64(publishableKey + ":");
        String basicAuthCredentials = "Basic " + encodedKey;

        BaseRequest request = new BaseRequest<>(Request.Method.POST, CREATE_CREDIT_CARD_URL, Authentication.class, new DefaultResponseHandler<>(handler));
        request.addHeader("Authorization", basicAuthCredentials.replace("\n", ""));
        request.addParam("is_authentication_bundled", String.valueOf(!isMultipleUse));
        request.addParam("should_authenticate", String.valueOf(shouldAuthenticate));
        request.addParam("credit_card_token", token);
        request.addParam("card_cvn", card.getCreditCardCVN());

        if (!isMultipleUse) {
            request.addParam("amount", amount);
        }

        sendRequest(request, handler);
    }

    private void _createAuthentication(String tokenId, String amount, NetworkHandler<Authentication> handler) {
        String encodedKey = encodeBase64(publishableKey + ":");
        String basicAuthCredentials = "Basic " + encodedKey;
        String requestUrl = CREATE_CREDIT_CARD_URL + "/" + tokenId + "/authentications";

        BaseRequest request = new BaseRequest<>(Request.Method.POST, requestUrl , Authentication.class, new DefaultResponseHandler<>(handler));
        request.addHeader("Authorization", basicAuthCredentials.replace("\n", ""));
        request.addParam("amount", amount);
        sendRequest(request, handler);
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
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    private boolean getEnvironment() {
        String publishKey = publishableKey.toUpperCase();
        return publishKey.contains("PRODUCTION");
    }
}