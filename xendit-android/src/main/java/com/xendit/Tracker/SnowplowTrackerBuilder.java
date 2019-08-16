package com.xendit.Tracker;

import com.snowplowanalytics.snowplow.tracker.*;
import com.snowplowanalytics.snowplow.tracker.emitter.RequestSecurity;
import com.snowplowanalytics.snowplow.tracker.emitter.TLSVersion;

import android.content.Context;

public class SnowplowTrackerBuilder {
    public static Tracker getTracker(Context context) {
        Emitter emitter = getEmitter(context);
        Subject subject = getSubject(context); // Optional

        return Tracker.init(new Tracker.TrackerBuilder(emitter, "TPI", "xendit-android-sdk", context)
                .subject(subject) // Optional
                .build()
        );
    }

    private static Emitter getEmitter(Context context) {
        return new Emitter.EmitterBuilder("snowplow-collector.iluma.ai", context)
                .security(RequestSecurity.HTTPS)
                .tls(TLSVersion.TLSv1_2)
                .build();
    }

    private static Subject getSubject(Context context) {
        return new Subject.SubjectBuilder()
                .context(context)
                .build();
    }
}
