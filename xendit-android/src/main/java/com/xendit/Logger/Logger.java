package com.xendit.Logger;

import android.Manifest;
import android.content.Context;
import android.util.Base64;
import android.util.Log;

import com.xendit.utils.PermissionUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.EnumMap;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class Logger {

    public enum Level {
        FATAL,
        ERROR,
        WARN,
        INFO,
        DEBUG,
        TRACE,
        VERBOSE
    }

    private ApiInterface apiInterface;
    private String publishableKey;
    private EnumMap<Level, String> enumMap;
    private Context context;

    public Logger(Context context, String userPublishableKey) {

        // set publishable key and context
        this.publishableKey = userPublishableKey;
        this.context = context;

        // init enums
        enumMap = new EnumMap<>(Level.class);
        enumMap.put(Level.FATAL  , "Fatal");
        enumMap.put(Level.ERROR  , "Error");
        enumMap.put(Level.WARN   , "Warn");
        enumMap.put(Level.INFO   , "Info");
        enumMap.put(Level.DEBUG  , "Debug");
        enumMap.put(Level.TRACE  , "Trace");
        enumMap.put(Level.VERBOSE, "Verbose");

        // network logger
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(interceptor)
                .build();

        // init retrofit
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ApiInterface.URL_BASE)
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();
        apiInterface = retrofit.create(ApiInterface.class);
    }

    /**
     * Send logs to server
     * @param levelCode of logs
     * @param logMessage log message that will be printout to server
     */
    public void log(Level levelCode, String logMessage) {
        if(PermissionUtils.hasPermission(context, Manifest.permission.ACCESS_NETWORK_STATE)) {
            try {
                // get string level
                String level = enumMap.get(levelCode);
                // create json body
                long unixTime = System.currentTimeMillis() / 1000L;
                JSONObject paramObject = new JSONObject();
                JSONArray linesArray = new JSONArray();
                paramObject.put("lines", linesArray);
                JSONObject lineObj = new JSONObject();
                lineObj.put("timestamp", unixTime);
                lineObj.put("line", publishableKey + " " + logMessage);
                lineObj.put("file", "Xendit SDK");
                lineObj.put("level", level);
                linesArray.put(lineObj);

                // auth part, this is INGESTION_KEY
                final String AUTH = "f324854fcb2ca3c397aa0536e0555070: ";
                String accessToken = "Basic " + Base64.encodeToString(AUTH.getBytes(), Base64.NO_WRAP);

                // send logs to server
                Call<Void> logs = apiInterface.sendLogs(accessToken, paramObject.toString());
                logs.enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        int code = response.code();
                        boolean success = response.isSuccessful();
                        Log.d("Logger", "code: " + code + " success: " + success);
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        Log.d("Logger", "Failed: " + t.getMessage());
                    }
                });
            } catch (JSONException e) {
                Log.e("Logger", "Exception: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            Log.e("Logger", "Internet permission is not granted");
        }
    }
}
