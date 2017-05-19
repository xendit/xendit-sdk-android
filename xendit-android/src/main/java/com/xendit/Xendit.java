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

import org.json.JSONException;

import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Dimon_GDA on 3/7/17.
 */

public class Xendit {

    private static final String NUMBER_REGEX = "^\\d+$";

    private static final String STAGING_XENDIT_BASE_URL = "https://api-staging.xendit.co";
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

    public static boolean isCardNumberValid(String creditCardNumber) {
        return creditCardNumber != null
                && creditCardNumber.length()
                >= 12 && creditCardNumber.length()
                <= 19 && creditCardNumber.matches(NUMBER_REGEX)
                && getCardType(creditCardNumber) != null;
    }

    public static boolean isExpiryValid(String cardExpirationMonth, String cardExpirationYear) {
        return cardExpirationMonth != null
                && cardExpirationYear != null
                && cardExpirationMonth.matches(NUMBER_REGEX)
                && cardExpirationYear.matches(NUMBER_REGEX)
                && number(cardExpirationMonth) >= 1
                && number(cardExpirationMonth) <= 12
                && number(cardExpirationYear) >= 2017
                && number(cardExpirationYear) <= 2100
                && getCurrentMonth(cardExpirationMonth, cardExpirationYear);
    }

    private static boolean getCurrentMonth(String cardExpirationMonth, String cardExpirationYear) {
        DateFormat monthFormat = new SimpleDateFormat("MM", Locale.US);
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        Date monthDate = new Date();
        int currentMonth = number(monthFormat.format(monthDate));
        int expMonth = number(cardExpirationMonth);
        int expYear = number(cardExpirationYear);
        if (expYear == currentYear) {
            if (expMonth >= currentMonth) {
                return true;
            }
        } else if (expYear > currentYear) {
            return true;
        }
        return false;
    }

    public static boolean isCvnValid(String creditCardCVN) {
        return creditCardCVN != null
                && creditCardCVN.matches(NUMBER_REGEX)
                && number(creditCardCVN) > 0
                && creditCardCVN.length() > 2
                && creditCardCVN.length() <= 4;
    }

    private static CYBCardTypes getCardType(String cardNumber) {
        if (cardNumber == null) {
            return null;
        } else if (cardNumber.indexOf("4") == 0) {
            if (isCardVisaElectron(cardNumber)) {
                return CYBCardTypes.VISA_ELECTRON;
            } else {
                return CYBCardTypes.VISA;
            }
        } else if (isCardAmex(cardNumber)) {
            return CYBCardTypes.AMEX;
        } else if (isCardMastercard(cardNumber)) {
            return CYBCardTypes.MASTERCARD;
        } else if (isCardDiscover(cardNumber)) {
            return CYBCardTypes.DISCOVER;
        } else if (isCardJCB(cardNumber)) {
            return CYBCardTypes.JCB;
        } else if (isCardDankort(cardNumber)) {
            return CYBCardTypes.DANKORT;
        } else if (isCardMaestro(cardNumber)) {
            return CYBCardTypes.MAESTRO;
        } else {
            return null;
        }
    }

    private static boolean isCardAmex(String cardNumber) {
        return cardNumber != null && (cardNumber.indexOf("34") == 0 || cardNumber.indexOf("37") == 0);
    }

    private static boolean isCardMastercard(String cardNumber) {
        if (cardNumber != null && cardNumber.length() >= 2) {
            int startingNumber = number(cardNumber.substring(0, 2));
            return startingNumber >= 51 && startingNumber <= 55;
        } else {
            return false;
        }
    }

    private static int number(String sNumber) {
        int number = -1;
        if (sNumber != null) {
            try {
                number = Integer.parseInt(sNumber);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return number;
    }

    private static boolean isCardDiscover(String cardNumber) {
        if (cardNumber != null && cardNumber.length() >= 6) {
            int firstStartingNumber = number(cardNumber.substring(0, 3));
            int secondStartingNumber = number(cardNumber.substring(0, 6));
            return (firstStartingNumber >= 644 && firstStartingNumber <= 649)
                    || (secondStartingNumber >= 622126 && secondStartingNumber <= 622925)
                    || cardNumber.indexOf("65") == 0
                    || cardNumber.indexOf("6011") == 0;
        } else {
            return false;
        }
    }

    private static boolean isCardMaestro(String cardNumber) {
        if (cardNumber != null && cardNumber.length() >= 2) {
            int startingNumber = number(cardNumber.substring(0, 2));
            return startingNumber == 50
                    || (startingNumber >= 56 && startingNumber <= 64)
                    || (startingNumber >= 66 && startingNumber <= 69);
        }

        return false;
    }

    private static boolean isCardDankort(String cardNumber) {
        return cardNumber != null && cardNumber.indexOf("5019") == 0;
    }

    private static boolean isCardJCB(String cardNumber) {
        if (cardNumber != null && cardNumber.length() >= 4) {
            int startingNumber = number(cardNumber.substring(0, 4));
            return startingNumber >= 3528 && startingNumber <= 3589;
        } else {
            return false;
        }
    }

    private static boolean isCardVisaElectron(String cardNumber) {
        return cardNumber != null && (cardNumber.indexOf("4026") == 0
                || cardNumber.indexOf("417500") == 0
                || cardNumber.indexOf("4405") == 0
                || cardNumber.indexOf("4508") == 0
                || cardNumber.indexOf("4844") == 0
                || cardNumber.indexOf("4913") == 0
                || cardNumber.indexOf("4917") == 0);
    }

    public enum CYBCardTypes {
        VISA("VISA", "001"),
        MASTERCARD("MASTERCARD", "002"),
        AMEX("AMEX", "003"),
        DISCOVER("DISCOVER", "004"),
        JCB("JCB", "007"),
        VISA_ELECTRON("VISA_ELECTRON", "033"),
        DANKORT("DANKORT", "034"),
        MAESTRO("MAESTRO", "042");

        private String cardName;
        private String cardKey;

        CYBCardTypes(String cardName, String cardKey) {
            this.cardName = cardName;
            this.cardKey = cardKey;
        }

        public String getCardName() {
            return cardName;
        }

        public String getCardKey() {
            return cardKey;
        }
    }

    public void createToken(final Card card, final String amount, final boolean isMultipleUse, final TokenCallback tokenCallback) {

        if (card != null && tokenCallback != null) {
            if (!isCardNumberValid(card.getCreditCardNumber())) {
                tokenCallback.onError(new XenditError(context.getString(R.string.create_token_error_card_number)));
                return;
            }

            if (!isExpiryValid(card.getCardExpirationMonth(), card.getCardExpirationYear())) {
                tokenCallback.onError(new XenditError(context.getString(R.string.create_token_error_card_expiration)));
                return;
            }

            if (!isCvnValid(card.getCreditCardCVN())) {
                tokenCallback.onError(new XenditError(context.getString(R.string.create_token_error_card_cvn)));
                return;
            }

            getTokenizationConfiguration(new NetworkHandler<TokenConfiguration>().setResultListener(new ResultListener<TokenConfiguration>() {
                @Override
                public void onSuccess(TokenConfiguration tokenConfiguration) {
                    tokenizeCreditCardRequest(tokenConfiguration, card, amount, isMultipleUse, tokenCallback);
                }

                @Override
                public void onFailure(NetworkError error) {
                    tokenCallback.onError(new XenditError(error));
                }
            }));
        }
    }

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

            _createAuthentication(tokenId, cardCvn, amount, new NetworkHandler<Authentication>().setResultListener(new ResultListener<Authentication>() {
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

    private void tokenizeCreditCardRequest(final TokenConfiguration tokenConfiguration, final Card card, final String amount, final boolean isMultipleUse, final TokenCallback tokenCallback) {
        tokenizeCreditCard(tokenConfiguration, card, new NetworkHandler<TokenCreditCard>().setResultListener(new ResultListener<TokenCreditCard>() {
            @Override
            public void onSuccess(TokenCreditCard tokenCreditCard) {
                createCreditCardToken(card, tokenCreditCard.getToken(), amount, isMultipleUse, tokenCallback);
            }

            @Override
            public void onFailure(NetworkError error) {
                tokenCallback.onError(new XenditError(error));
            }
        }));
    }

    public void createCreditCardToken(Card card, final String token, String amount, boolean isMultipleUse, final TokenCallback tokenCallback) {
        if (!isCvnValid(card.getCreditCardCVN())) {
            tokenCallback.onError(new XenditError(context.getString(R.string.create_token_error_card_cvn)));
            return;
        }

        _createToken(card, token, amount, isMultipleUse, new NetworkHandler<Authentication>().setResultListener(new ResultListener<Authentication>() {
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
        cardInfoJson.addProperty("cardType", getCardType(card.getCreditCardNumber()).getCardKey());

        request.addParam("keyId", tokenConfig.getTokenizationAuthKeyId());
        request.addJsonParam("cardInfo", cardInfoJson);
        sendRequest(request, handler);
    }

    private void _createToken(Card card, String token, String amount, boolean isMultipleUse, NetworkHandler<Authentication> handler) {
        String encodedKey = encodeBase64(publishableKey + ":");
        String basicAuthCredentials = "Basic " + encodedKey;

        BaseRequest request = new BaseRequest<>(Request.Method.POST, CREATE_CREDIT_CARD_URL, Authentication.class, new DefaultResponseHandler<>(handler));
        request.addHeader("Authorization", basicAuthCredentials.replace("\n", ""));
        request.addParam("is_authentication_bundled", String.valueOf(!isMultipleUse));
        request.addParam("amount", amount);
        request.addParam("credit_card_token", token);
        request.addParam("card_cvn", card.getCreditCardCVN());
        sendRequest(request, handler);
    }

    private void _createAuthentication(String tokenId, String cardCvn, String amount, NetworkHandler<Authentication> handler) {
        String encodedKey = encodeBase64(publishableKey + ":");
        String basicAuthCredentials = "Basic " + encodedKey;
        String requestUrl = CREATE_CREDIT_CARD_URL + "/" + tokenId + "/authentications";

        BaseRequest request = new BaseRequest<>(Request.Method.POST, requestUrl , Authentication.class, new DefaultResponseHandler<>(handler));
        request.addHeader("Authorization", basicAuthCredentials.replace("\n", ""));
        request.addParam("amount", amount);
        request.addParam("card_cvn", cardCvn);
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