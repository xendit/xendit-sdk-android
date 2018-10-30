package com.xendit.deviceInfo;


public final class AdInfo {
    private final String advertisingId;
    private final boolean limitAdTrackingEnabled;

    AdInfo(String advertisingId, boolean limitAdTrackingEnabled) {
        this.advertisingId = advertisingId;
        this.limitAdTrackingEnabled = limitAdTrackingEnabled;
    }

    public String getId() {
        return this.advertisingId;
    }

    public boolean isLimitAdTrackingEnabled() {
        return this.limitAdTrackingEnabled;
    }
}
