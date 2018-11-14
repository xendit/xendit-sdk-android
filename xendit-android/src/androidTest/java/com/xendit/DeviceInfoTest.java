package com.xendit;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.SmallTest;
import android.support.test.rule.GrantPermissionRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.v4.app.ActivityCompat;


import com.xendit.DeviceInfo.AdInfo;
import com.xendit.DeviceInfo.DeviceInfo;
import com.xendit.DeviceInfo.GPSLocation;
import com.xendit.DeviceInfo.Model.DeviceLocation;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.InstrumentationRegistry.getTargetContext;
import static com.google.common.truth.Truth.assertThat;

@RunWith(AndroidJUnit4.class)
@SmallTest
public class DeviceInfoTest {

    private Context appContext = getTargetContext();

    @Test
    public void test_getDeviceInfo() {
        new Thread(new Runnable() {
            public void run() {
                try {
                    AdInfo adInfo = DeviceInfo.getAdvertisingIdInfo(appContext);
                    String advertisingId = adInfo.getId();
                    assertThat(advertisingId).isNotEmpty();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();

        String wifiSSID = DeviceInfo.getWifiSSID(appContext);
        assertThat(wifiSSID).isEmpty();
        String language = DeviceInfo.getLanguage();
        assertThat(language).isNotEmpty();
        String product = DeviceInfo.getProduct();
        assertThat(product).isNotEmpty();
        String model = DeviceInfo.getModel();
        assertThat(model).isNotEmpty();
        String device = DeviceInfo.getDevice();
        assertThat(device).isNotEmpty();
        String apiLevel = DeviceInfo.getAPILevel();
        assertThat(apiLevel).isNotEmpty();
        String osVerson = DeviceInfo.getOSVersion();
        assertThat(osVerson).isNotEmpty();
        String fingerprint = DeviceInfo.getFingerprint();
        assertThat(fingerprint).isNotEmpty();
        String hardware = DeviceInfo.getHardware();
        assertThat(hardware).isNotEmpty();
        String ipAddress = DeviceInfo.getIPAddress(true);
        assertThat(ipAddress).isEmpty();

        GPSLocation gpsLocation = new GPSLocation(appContext);
        DeviceLocation deviceLocation = gpsLocation.getLocation();
        if (deviceLocation != null) {
            Double latitude = deviceLocation.getLatitude();
            assertThat(latitude).isNonZero();
            Double longitude = deviceLocation.getLongitude();
            assertThat(longitude).isNonZero();
        }
        int lac = gpsLocation.getLac(appContext);
        assertThat(lac).isEqualTo(0);
        int cid = gpsLocation.getCid(appContext);
        assertThat(cid).isEqualTo(0);
    }
}
