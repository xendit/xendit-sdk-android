package com.xendit.DeviceInfo;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Looper;
import android.os.Parcel;
import android.os.RemoteException;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;

import com.hypertrack.hyperlog.HyperLog;
import com.xendit.utils.PermissionUtils;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.LinkedBlockingQueue;

public final class DeviceInfo {

    private final static String TAG = "DeviceInfo";

    public static AdInfo getAdvertisingIdInfo(Context context) throws Exception {
        if(Looper.myLooper() == Looper.getMainLooper())
            throw new IllegalStateException("Cannot be called from the main thread");

        try {
            PackageManager pm = context.getPackageManager();
            pm.getPackageInfo("com.android.vending", 0);
        } catch(Exception e) {
            HyperLog.e(TAG,e.getMessage());
            throw e;
        }

        AdvertisingConnection connection = new AdvertisingConnection();
        Intent intent = new Intent("com.google.android.gms.ads.identifier.service.START");
        intent.setPackage("com.google.android.gms");
        try {
            if(context.bindService(intent, connection, Context.BIND_AUTO_CREATE)) {
                AdvertisingInterface adInterface = new AdvertisingInterface(connection.getBinder());
                return new AdInfo(adInterface.getId(), adInterface.isLimitAdTrackingEnabled(true));
            }
        } catch(Exception e) {
            HyperLog.e(TAG,e.getMessage());
            throw e;
        } finally {
            context.unbindService(connection);
        }
        throw new IOException("Google Play connection failed");
    }

    private static final class AdvertisingConnection implements ServiceConnection {
        boolean retrieved = false;
        private final LinkedBlockingQueue<IBinder> queue = new LinkedBlockingQueue<IBinder>(1);

        public void onServiceConnected(ComponentName name, IBinder service) {
            try { this.queue.put(service); }
            catch (InterruptedException localInterruptedException){
                HyperLog.e(TAG, localInterruptedException.getMessage());
            }
        }

        public void onServiceDisconnected(ComponentName name){}

        public IBinder getBinder() throws InterruptedException {
            if (this.retrieved) throw new IllegalStateException();
            this.retrieved = true;
            return (IBinder)this.queue.take();
        }
    }

    private static final class AdvertisingInterface implements IInterface {
        private IBinder binder;

        public AdvertisingInterface(IBinder pBinder) {
            binder = pBinder;
        }

        public IBinder asBinder() {
            return binder;
        }

        public String getId() throws RemoteException {
            Parcel data = Parcel.obtain();
            Parcel reply = Parcel.obtain();
            String id;
            try {
                data.writeInterfaceToken("com.google.android.gms.ads.identifier.internal.IAdvertisingIdService");
                binder.transact(1, data, reply, 0);
                reply.readException();
                id = reply.readString();
            } finally {
                reply.recycle();
                data.recycle();
            }
            return id;
        }

        public boolean isLimitAdTrackingEnabled(boolean paramBoolean) throws RemoteException {
            Parcel data = Parcel.obtain();
            Parcel reply = Parcel.obtain();
            boolean limitAdTracking;
            try {
                data.writeInterfaceToken("com.google.android.gms.ads.identifier.internal.IAdvertisingIdService");
                data.writeInt(paramBoolean ? 1 : 0);
                binder.transact(2, data, reply, 0);
                reply.readException();
                limitAdTracking = 0 != reply.readInt();
            } finally {
                reply.recycle();
                data.recycle();
            }
            return limitAdTracking;
        }
    }

    /**
     * Get IP address from first non-localhost interface
     * @param useIPv4   true=return ipv4, false=return ipv6
     * @return  address or empty string
     */
    public static String getIPAddress(boolean useIPv4) {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
                for (InetAddress addr : addrs) {
                    if (!addr.isLoopbackAddress()) {
                        String sAddr = addr.getHostAddress();
                        //boolean isIPv4 = InetAddressUtils.isIPv4Address(sAddr);
                        boolean isIPv4 = sAddr.indexOf(':')<0;

                        if (useIPv4) {
                            if (isIPv4)
                                return sAddr;
                        } else {
                            if (!isIPv4) {
                                int delim = sAddr.indexOf('%'); // drop ip6 zone suffix
                                return delim<0 ? sAddr.toUpperCase() : sAddr.substring(0, delim).toUpperCase();
                            }
                        }
                    }
                }
            }
        } catch (Exception ignored) { } // for now eat exceptions
        return "";
    }

    public static String getOSVersion() {
        return System.getProperty("os.version") + "(" +
                android.os.Build.VERSION.INCREMENTAL + ")";
    }

    public static String getAPILevel() {
        return android.os.Build.VERSION.SDK;
    }

    public static String getDevice() {
        return android.os.Build.DEVICE;
    }

    public static String getModel() {
        return android.os.Build.MODEL;
    }

    public static String getProduct() {
        return android.os.Build.PRODUCT;
    }

    public static String getFingerprint() {
        return android.os.Build.FINGERPRINT;
    }

    public static String getHardware() {
        return android.os.Build.HARDWARE;
    }

    public static String getLanguage() {
        return Locale.getDefault().getLanguage();
    }

    public static void getWifiSSID(Context context) {

        WifiManager manager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (PermissionUtils.hasPermission(context, Manifest.permission.ACCESS_WIFI_STATE)) {
            @SuppressLint("MissingPermission")
            WifiInfo info = manager.getConnectionInfo();
            HyperLog.d(TAG, "SSID: " + info.getSSID());
        } else {
            HyperLog.d(TAG, "Does not have ACCESS_WIFI_STATE permission");
        }
    }
    public static String getLACCID(Context context) {
        String info = "";
        final TelephonyManager telephony = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (telephony != null && telephony.getPhoneType() == TelephonyManager.PHONE_TYPE_GSM) {
            if (PermissionUtils.hasPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) ||
                    PermissionUtils.hasPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)) {
                @SuppressLint("MissingPermission") final GsmCellLocation location = (GsmCellLocation) telephony.getCellLocation();
                if (location != null) {
                    info = "LAC: " + location.getLac() + " CID: " + location.getCid();
                }
            } else {
                info = "Does not have ACCESS_COARSE_LOCATION or ACCESS_FINE_LOCATION permission";
            }
        }
        return info;
    }



}
