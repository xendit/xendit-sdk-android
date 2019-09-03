package com.xendit.Tracker;

import com.snowplowanalytics.snowplow.tracker.*;
import com.snowplowanalytics.snowplow.tracker.emitter.HttpMethod;
import com.snowplowanalytics.snowplow.tracker.emitter.RequestCallback;
import com.snowplowanalytics.snowplow.tracker.emitter.RequestSecurity;

import android.content.Context;
import android.util.Log;

public class SnowplowTrackerBuilder {
    public static Tracker getTracker(Context context) {
        Emitter emitter = getEmitter(context);
        Subject subject = getSubject(context); // Optional

        return Tracker.init(new Tracker.TrackerBuilder(emitter, "TPI", "xendit-android-sdk", context)
                .subject(subject) // Optional
                .mobileContext(true)
                .build()
        );
    }

    private static Emitter getEmitter(Context context) {
        return new Emitter.EmitterBuilder("snowplow-collector.iluma.ai", context)
                .security(RequestSecurity.HTTPS)
                .method(HttpMethod.POST)
                .build();
    }

    private static Subject getSubject(Context context) {
        return new Subject.SubjectBuilder()
                .context(context)
                .build();
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
