package com.xendit.DeviceInfo;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;

import com.hypertrack.hyperlog.HyperLog;
import com.xendit.DeviceInfo.Model.DeviceLocation;

import java.util.List;
import java.util.Locale;

public class GPSLocation implements LocationListener {

    private static final String TAG = "GPSLocation";
    // The minimum distance to change Updates in meters
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 0; // 0 meters
    // The minimum time between updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 28800; // 8 hours

    private Context context;
    private Double longitude;
    private Double latitude;

    @SuppressLint("MissingPermission")
    public GPSLocation(Context context) { this.context = context; }

    @SuppressLint("MissingPermission")
    /**
     * Return latitude and longitude
     */
    private Double[] getLocationLatLong() {
        Double[] latlong = new Double[2];
        latlong[0] = 0.0;
        latlong[1] = 0.0;

        try {
            // Get the location manager
            LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            // ——–Gps provider—
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    MIN_TIME_BW_UPDATES,
                    MIN_DISTANCE_CHANGE_FOR_UPDATES, this, Looper.getMainLooper());

            // getting GPS status
            Boolean isGPSEnabled = locationManager
                    .isProviderEnabled(LocationManager.GPS_PROVIDER);

            // getting network status
            Boolean isNetworkEnabled = locationManager
                    .isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (isGPSEnabled) {
                Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES,
                        MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                if (location != null) {
                    latlong[0] = location.getLatitude();
                    latlong[1] = location.getLongitude();
                    locationManager.removeUpdates(this);
                    return latlong;
                }
            }

            if (isNetworkEnabled) {
                Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES,
                        MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                if (location != null) {
                    latlong[0] = location.getLatitude();
                    latlong[1] = location.getLongitude();
                    locationManager.removeUpdates(this);
                    return latlong;
                }
            }

            // use passive provider
            Location location = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES,
                    MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
            if (location != null) {
                latlong[0] = location.getLatitude();
                latlong[1] = location.getLongitude();
                locationManager.removeUpdates(this);
                return latlong;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return latlong;
    }

    /**
     * Function to get correct address from lonitude and latitude
     * @param latitude of device
     * @param longitude of device
     * @return DeviceLocation structure
     */
    private DeviceLocation getAddressFromLocation(double latitude, double longitude) {

        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        DeviceLocation addressInfo = new DeviceLocation();
        try {
            List<Address> addressList = geocoder.getFromLocation(latitude, longitude, 1);

            if (addressList != null && addressList.size() > 0) {
                Address address = addressList.get(0);
                addressInfo.setAddressLine1(address.getAddressLine(0));
                addressInfo.setCity(address.getLocality());
                addressInfo.setPostalCode(address.getPostalCode());
                addressInfo.setState(address.getAdminArea());
                addressInfo.setCountryCode(address.getCountryCode());
                addressInfo.setLatitude(latitude);
                addressInfo.setLongitude(longitude);
                return addressInfo;
            }
        } catch (Exception e) {
            HyperLog.e("", "Unable connect to Geocoder", e);
        } finally {
            return addressInfo;
        }
    }

    /**
     * Function which will return device location to calling method
     * @return DeviceLocation structure
     */
    public DeviceLocation getLocation() {
        Double[] latlong = getLocationLatLong();
        return getAddressFromLocation(latlong[0], latlong[1]);
    }

    @Override
    public void onLocationChanged(Location location) {
        HyperLog.d(TAG, "Lat: " + location.getLatitude());
        HyperLog.d(TAG, "Long: " + location.getLongitude());

        // only latitude longitude to set up
        latitude = location.getLatitude();
        longitude = location.getLongitude();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    private GsmCellLocation getGsmCellLocation(Context context) {
        final TelephonyManager telephony = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (telephony != null && telephony.getPhoneType() == TelephonyManager.PHONE_TYPE_GSM) {
            @SuppressLint("MissingPermission")
            final GsmCellLocation location = (GsmCellLocation) telephony.getCellLocation();
            return location;
        }
        return null;
    }

    public int getLac(Context context) {
        GsmCellLocation gsmCellLocation = getGsmCellLocation(context);
        return gsmCellLocation != null ? gsmCellLocation.getLac() : 0;
    }

    public int getCid(Context context) {
        GsmCellLocation gsmCellLocation = getGsmCellLocation(context);
        return gsmCellLocation != null ? gsmCellLocation.getCid() : 0;
    }

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }
}
