package com.letv.wallet.common.util;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;

import com.letv.wallet.common.BaseApplication;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by liuliang on 16-1-6.
 */
public class LocationHelper {

    private static final double EARTH_RADIUS = 6378137.0;

    private Context mContext;
    private LocationManager mLocationManager;
    private LocationListener mLocationListener;
    private ArrayList<LocationCallback> mCallbackList = new ArrayList<LocationCallback>();
    private Address mAddress;

    private boolean isUpdating = false;
    private boolean gpsProviderEnabled = false, networkProviderEnabled = false;
    private static final int LOCATION_MIN_TIME_MS = 10, LOCATION_MIN_DISTENCE_M = 1;
    private static final int LOCATION_TIME_OUT_MIN = 15 * 1000;
    private static final int LOCATION_TIME_OUT_MAX = 60 * 1000;
    private static final int MSG_LOCATION_GPS_TIME_OUT= 1001;
    private static final int MSG_LOCATION_NETWORK_TIME_OUT= 1002;
    private static final int MSG_LOCATION_FINISH= 1003;

    public static final int LOCATE_TIME_OUT = 3;
    public static final int LOCATE_DISABLE = 4;
    public static final int LOCATE_EXCEPTION = 5;
    public static final int LOCATE_SUCCESS = 6;

    private static LocationHelper sInstance = null;
    public static synchronized LocationHelper getInstance() {
        if (sInstance == null) {
            sInstance = new LocationHelper(BaseApplication.getApplication());
        }
        return sInstance;
    }

    private Handler mMainHandler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_LOCATION_NETWORK_TIME_OUT:
                    isUpdating = false;
                    requestLoaction(LocationManager.GPS_PROVIDER);
                    break;
                case MSG_LOCATION_GPS_TIME_OUT:
                    isUpdating = false;
                    if(mLocationManager != null){
                        Location lastLocation = null ;
                        if(mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){
                            lastLocation = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        }
                        if(lastLocation == null){
                            lastLocation = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        }
                        if(lastLocation != null){
                            updateLocationAddress(lastLocation);
                        }else {
                            callback(null, LOCATE_TIME_OUT);
                        }
                    }
                    break;
                case MSG_LOCATION_FINISH:
                    locationUpdate((Address) msg.obj, msg.arg1);
                    break;
            }
        }
    };

    private void removeUpdates(){
        if (mLocationManager != null && mLocationListener != null) {
            mLocationManager.removeUpdates(mLocationListener);
        }
    }

    public interface LocationCallback {
        void onLocationUpdateFinished(Address address, int responseCode);
    }

    private LocationHelper(Context context) {
        mContext = context;
        mLocationManager = getLocationManager(context);
    }

    public void addLocationCallback(LocationCallback locationCallback) {
        mCallbackList.add(locationCallback);
    }

    public void removeLocationCallback(LocationCallback callback) {
        mCallbackList.remove(callback);
    }

    public static LocationManager getLocationManager(Context context) {
        return (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
    }

    public void getAddress(boolean forceUpdate) {
        if (!forceUpdate && mAddress != null) {
            callback(mAddress, LOCATE_SUCCESS);
            return;
        }
        if (isUpdating) {
            return;
        }

        List<String> enabledProviders = mLocationManager.getProviders(true);
        if(enabledProviders == null || enabledProviders.size() == 0){
            callback(null, LOCATE_DISABLE);
            return;
        }

        gpsProviderEnabled = enabledProviders.contains(LocationManager.GPS_PROVIDER);
        networkProviderEnabled = enabledProviders.contains(LocationManager.NETWORK_PROVIDER);

        if (!gpsProviderEnabled && !networkProviderEnabled) {
            callback(null, LOCATE_DISABLE);
            return;
        }
        requestLoaction(networkProviderEnabled ? LocationManager.NETWORK_PROVIDER : LocationManager.GPS_PROVIDER);
    }

    private void requestLoaction(String provider){
        if(isUpdating || mLocationManager == null || TextUtils.isEmpty(provider)){
            return;
        }

        if (mLocationListener == null) {
            mLocationListener = new MyLocationListener(mLocationManager);
        }

        try {
            if (mLocationManager.isProviderEnabled(provider)){
                LogHelper.d("startLocation >>> " + provider);
                isUpdating = true;
                mLocationManager.requestLocationUpdates(provider, LOCATION_MIN_TIME_MS, LOCATION_MIN_DISTENCE_M, mLocationListener);
                int mLocationTimeOut = LOCATION_TIME_OUT_MIN;
                int msgWhat = MSG_LOCATION_NETWORK_TIME_OUT;
                if(LocationManager.GPS_PROVIDER.equalsIgnoreCase(provider)){
                    mLocationTimeOut = LOCATION_TIME_OUT_MAX;
                    msgWhat = MSG_LOCATION_GPS_TIME_OUT;
                }
                mMainHandler.sendEmptyMessageDelayed(msgWhat, mLocationTimeOut);
            }
        } catch (Exception e) {
            LogHelper.e(e.toString());
            callback(null , LOCATE_EXCEPTION);
        }
    }

    private void callback(final Address address, final int responseCode) {
        LogHelper.d("location finished >>> responseCode  = " + responseCode);
        isUpdating = false;
        if (address != null) {
            mAddress = address;
        }
        if (isOnUIThread()) {
            locationUpdate(address, responseCode);
        } else {
            Message.obtain(mMainHandler, MSG_LOCATION_FINISH, responseCode, 0, address).sendToTarget();
        }
    }

    private void locationUpdate(Address address,  int responseCode){
        LogHelper.d("location update >>> responseCode  = " + responseCode);
        removeUpdates();
        for (LocationCallback callback : mCallbackList) {
            callback.onLocationUpdateFinished(address, responseCode);
        }
    }

    private boolean isOnUIThread(){
        return Looper.myLooper() == Looper.getMainLooper();
    }

    private void updateLocationAddress(Location location) {
        new UpdateLocationAddressTask().execute(location);
    }

    public static double getDistance(double latitude1, double longitude1, double latitude2, double longitude2) {
        double Lat1 = rad(latitude1);
        double Lat2 = rad(latitude2);
        double a = Lat1 - Lat2;
        double b = rad(longitude1) - rad(longitude2);
        double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2)
                + Math.cos(Lat1) * Math.cos(Lat2)
                * Math.pow(Math.sin(b / 2), 2)));
        s = s * EARTH_RADIUS;
        s = Math.round(s * 10000) / 10000;
        return s;
    }

    private static double rad(double d) {
        return d * Math.PI / 180.0;
    }

    class MyLocationListener implements LocationListener {

        private LocationManager mLocationManager;

        public MyLocationListener(LocationManager locationManager) {
            mLocationManager = locationManager;
        }

        @Override
        public void onLocationChanged(Location location) {
            updateLocationAddress(location);
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
    }

    class UpdateLocationAddressTask extends AsyncTask<Location, Void, Address> {

        public UpdateLocationAddressTask() {
        }

        @Override
        protected Address doInBackground(Location... params) {
            Geocoder geocoder = new Geocoder(BaseApplication.getApplication(), Locale.getDefault());
            Location location = params[0];
            List<Address> addressList = null;
            try {
                addressList = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (addressList != null && addressList.size() > 0) {
                return addressList.get(0);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Address address) {
            if (mMainHandler != null) {
                mMainHandler.removeMessages(MSG_LOCATION_GPS_TIME_OUT);
                mMainHandler.removeMessages(MSG_LOCATION_NETWORK_TIME_OUT);
            }
            callback(address, LOCATE_SUCCESS);
        }
    }

    public String getDeviceLocation(){
        if(mAddress!=null){
            return mAddress.getLatitude()+"/"+mAddress.getLongitude();
        }
        else{
            getAddress(true);
            return null;
        }
    }
}
