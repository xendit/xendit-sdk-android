package com.xendit.network;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.stream.JsonReader;
import com.xendit.network.errors.NetworkError;
import com.xendit.network.interfaces.TokenExpiredListener;

import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class BaseRequest<T> extends Request<T> {

    private static final String NO_AUTHENTICATION_CHALLENGES_FOUND_ERROR = "java.io.IOException: No authentication challenges found";
    private static final String PROTOCOL_CHARSET = "utf-8";
    private static final String PROTOCOL_CONTENT_TYPE = String.format("application/json; charset=%s", PROTOCOL_CHARSET);

    private final Response.Listener<T> listener;
    private Map<String, String> headers;
    private final GsonBuilder gsonBuilder = new GsonBuilder();
    private final Type type;
    private TokenExpiredListener tokenExpiredListener;
    private boolean isRefreshToken = true;
    private JsonObject jsonBody;
    private Gson gson;

    private BaseRequest(int method, String url, Type type, Response.Listener<T> successListener, Response.ErrorListener errorListener) {
        super(method, url, errorListener);
        this.listener = successListener;
        this.type = type;
        setShouldCache(false);
        setRetryPolicy(new BaseRetryPolicy());
    }

    public BaseRequest(int method, String url, Type type, DefaultResponseHandler<T> responseListener) {
        this(method, url, type, responseListener, responseListener);
    }

    public void addParam(String key, String value) {
        if (jsonBody == null) {
            jsonBody = new JsonObject();
        }
        jsonBody.addProperty(key, value);
    }

    public void addJsonParam(String key, JsonElement jsonParam) {
        if (jsonBody == null) {
            jsonBody = new JsonObject();
        }
        jsonBody.add(key, jsonParam);
    }

    public void addHeader(String key, String value) {
        if (headers == null) {
            headers = new HashMap<>();
        }
        headers.put(key, value);
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        return headers != null ? headers : super.getHeaders();
    }

    @Override
    protected void deliverResponse(T response) {
        if (null != listener) {
            listener.onResponse(response);
        }
    }

    @Override
    protected Response<T> parseNetworkResponse(NetworkResponse response) {
        try {
            String jsonString = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
            return Response.success(parseResult(jsonString), HttpHeaderParser.parseCacheHeaders(response));
        } catch (UnsupportedEncodingException | JsonParseException e) {
            return Response.error(new ParseError(e));
        }
    }

    private T parseResult(String response) {
        T result;
        if (type == String.class) {
            result = (T) response;
        } else {
            JsonReader reader = new JsonReader(new StringReader(response));
            reader.setLenient(true);
            result = getGson().fromJson(reader, type);
        }
        return result;
    }

    private Gson createGson() {
        return gsonBuilder.create();
    }

    private Gson getGson() {
        if (gson == null) {
            gson = createGson();
        }
        return gson;
    }

    @Override
    public String getBodyContentType() {
        return PROTOCOL_CONTENT_TYPE;
    }

    @Override
    public byte[] getBody() throws AuthFailureError {
        byte[] body = null;
        if (jsonBody != null) {
            try {
                body = getGson().toJson(jsonBody).getBytes(PROTOCOL_CHARSET);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        return body;
    }

    void setTokenExpiredListener(TokenExpiredListener listener) {
        tokenExpiredListener = listener;
    }

    @SuppressWarnings("ThrowableInstanceNeverThrown")
    @Override
    public void deliverError(VolleyError error) {
        boolean isTokenExpired;
        NetworkError networkError = new NetworkError(error);
        isTokenExpired = networkError.responseCode == 401 || NO_AUTHENTICATION_CHALLENGES_FOUND_ERROR.equalsIgnoreCase(networkError.getMessage());
        if (isTokenExpired && isRefreshToken && tokenExpiredListener != null) {
            isRefreshToken = false;
            tokenExpiredListener.onTokenExpired(this);
        } else {
            super.deliverError(error);
        }
    }

    private class BaseRetryPolicy implements RetryPolicy {

        private static final int DEFAULT_TIMEOUT_MS = 15 * 1000;
        private static final int RETRY_COUNT = 0;

        @Override
        public int getCurrentTimeout() {
            return DEFAULT_TIMEOUT_MS;
        }

        @Override
        public int getCurrentRetryCount() {
            return RETRY_COUNT;
        }

        @Override
        public void retry(VolleyError error) throws VolleyError {
            throw error;
        }
    }
}