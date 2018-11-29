package com.xendit;

import android.Manifest;
import android.content.Context;
import android.support.test.filters.SmallTest;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;


import com.xendit.DeviceInfo.AdInfo;
import com.xendit.DeviceInfo.DeviceInfo;
import com.xendit.DeviceInfo.GPSLocation;
import com.xendit.DeviceInfo.Model.DeviceLocation;
import com.xendit.utils.PermissionUtils;

import org.junit.Test;
import org.junit.runner.RunWith;

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
        assertThat(wifiSSID).isNotEmpty();
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
        if(!PermissionUtils.hasPermission(appContext, Manifest.permission.ACCESS_WIFI_STATE)) {
            Log.e("DeviceInfoTest", "DeviceInfoTest: Access Wifi State permission is not granted");
        } else {
            String ipAddress = DeviceInfo.getIPAddress(true);
            assertThat(ipAddress).isNotEmpty();
        }
        if(!PermissionUtils.hasPermission(appContext, Manifest.permission.ACCESS_FINE_LOCATION)) {
            Log.e("DeviceInfoTest", "DeviceInfoTest: Access Fine Location permission is not granted");
        } else {
            GPSLocation gpsLocation = new GPSLocation(appContext);
            DeviceLocation deviceLocation = gpsLocation.getLocation();
            if (deviceLocation != null && deviceLocation.getLatitude() != null) {
                Double latitude = deviceLocation.getLatitude();
                assertThat(latitude).isNonZero();
                Double longitude = deviceLocation.getLongitude();
                assertThat(longitude).isNonZero();
            }
            assertThat(gpsLocation.getLatitude()).isNonZero();
            assertThat(gpsLocation.getLongitude()).isNonZero();
            int lac = gpsLocation.getLac(appContext);
            assertThat(lac).isAtLeast(0);
            int cid = gpsLocation.getCid(appContext);
            assertThat(cid).isAtLeast(0);
        }
    }
}
