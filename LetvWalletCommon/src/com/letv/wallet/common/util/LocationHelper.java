package com.letv.wallet.common.util;

import android.content.Context;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
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

    private static LocationHelper sInstance = null;
    public static synchronized LocationHelper getInstance() {
        if (sInstance == null) {
            sInstance = new LocationHelper(BaseApplication.getApplication());
        }
        return sInstance;
    }

    public interface LocationCallback {
        void onLocationUpdateFinished(Address address);
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
            callback(mAddress);
            return;
        }
        if (isUpdating) {
            return;
        }
        isUpdating = true;
        Location lastLocation = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (lastLocation != null) {
            updateLocationAddress(lastLocation);
        }

        try {
            Criteria criteria = new Criteria();
            criteria.setAccuracy(Criteria.ACCURACY_FINE);
            criteria.setAltitudeRequired(false);
            criteria.setBearingRequired(false);
            criteria.setCostAllowed(false);
            criteria.setPowerRequirement(Criteria.POWER_LOW);

            String provider = mLocationManager.getBestProvider(criteria, true);
            if (mLocationListener == null) {
                mLocationListener = new MyLocationListener(mLocationManager);
            }
            if (!TextUtils.isEmpty(provider)) {
                mLocationManager.requestLocationUpdates(provider, 10, 1, mLocationListener);
            }

        } catch (Exception e) {
            isUpdating = false;
            LogHelper.e(e.toString());
        }
    }

    private void callback(Address address) {
        if (address != null) {
            mAddress = address;
        }
        for (LocationCallback callback : mCallbackList) {
            callback.onLocationUpdateFinished(mAddress);
        }
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
            if (mLocationManager != null) {
                if (LocationManager.NETWORK_PROVIDER.equals(location.getProvider())
                        || LocationManager.GPS_PROVIDER.equals(location.getProvider())) {
                    mLocationManager.removeUpdates(this);
                }
            }
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
            isUpdating = false;
            callback(address);
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
