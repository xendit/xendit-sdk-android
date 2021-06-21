package com.xendit.Tracker;

import com.snowplowanalytics.snowplow.Snowplow;
import com.snowplowanalytics.snowplow.configuration.EmitterConfiguration;
import com.snowplowanalytics.snowplow.configuration.GdprConfiguration;
import com.snowplowanalytics.snowplow.configuration.NetworkConfiguration;
import com.snowplowanalytics.snowplow.configuration.SessionConfiguration;
import com.snowplowanalytics.snowplow.configuration.TrackerConfiguration;
import com.snowplowanalytics.snowplow.controller.TrackerController;
import com.snowplowanalytics.snowplow.network.HttpMethod;
import com.snowplowanalytics.snowplow.network.RequestCallback;
import com.snowplowanalytics.snowplow.tracker.DevicePlatform;
import com.snowplowanalytics.snowplow.util.Basis;
import com.snowplowanalytics.snowplow.util.TimeMeasure;

import android.content.Context;
import android.util.Log;

import java.util.concurrent.TimeUnit;

public class SnowplowTrackerBuilder {
    private static String COLLECTOR_URL = "https://snowplow-collector.iluma.ai";

    public static TrackerController getTracker(Context context) {
        if (Snowplow.getDefaultTracker() != null) {
            return Snowplow.getDefaultTracker();
        }
        NetworkConfiguration networkConfig = new NetworkConfiguration(
                COLLECTOR_URL,
                HttpMethod.POST
        );
        EmitterConfiguration emitterConfiguration = new EmitterConfiguration()
                .threadPoolSize(20)
                .emitRange(500)
                .byteLimitPost(52000);
        TrackerConfiguration trackerConfiguration =
                new TrackerConfiguration("Xendit Android SDK")
                        .base64encoding(false)
                        .devicePlatform(DevicePlatform.Mobile)
                        .sessionContext(true)
                        .platformContext(true)
                        .applicationContext(true)
                        .geoLocationContext(false)
                        .lifecycleAutotracking(true)
                        .screenViewAutotracking(true)
                        .screenContext(true)
                        .exceptionAutotracking(true)
                        .installAutotracking(true);
        GdprConfiguration gdprConfiguration = new GdprConfiguration(
                Basis.CONSENT,
                "",
                "",
                ""
        );
        SessionConfiguration sessionConfig = new SessionConfiguration(
                new TimeMeasure(30, TimeUnit.MINUTES),
                new TimeMeasure(30, TimeUnit.MINUTES)
        );
        TrackerController tracker = Snowplow.createTracker(
                context,
                "appTracker",
                networkConfig,
                trackerConfiguration,
                emitterConfiguration,
                sessionConfig,
                gdprConfiguration);
        Snowplow.setTrackerAsDefault(tracker);
        return tracker;
    }

    private static RequestCallback callback = new RequestCallback() {
        @Override
        public void onSuccess(int successCount) {
            Log.d("Tracker", "Buffer length for POST/GET:" + successCount);
        }

        @Override
        public void onFailure(int successCount, int failureCount) {
            Log.d("Tracker", "Failures: " + failureCount + "; Successes: " + successCount);
        }
    };
}
