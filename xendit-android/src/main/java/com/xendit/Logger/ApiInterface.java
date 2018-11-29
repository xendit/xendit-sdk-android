package com.xendit.Logger;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;

/**
 * Interface for retrofit, rest call to logdna to send logs
 */
public interface ApiInterface {
    String URL_BASE = "https://logs.logdna.com/";

    @Headers({ "Content-Type: application/json;charset=UTF-8"})
    @POST("logs/ingest?hostname=xendit.co")
    Call<Void> sendLogs(@Header("Authorization") String accessToken, @Body String body);
}
