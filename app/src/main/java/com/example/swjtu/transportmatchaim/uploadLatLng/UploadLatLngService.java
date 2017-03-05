package com.example.swjtu.transportmatchaim.uploadLatLng;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.example.swjtu.transportmatchaim.util.VolleyUtil;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by tangpeng on 2017/3/5.
 */

public class UploadLatLngService extends Service {
    private static final String TAG = "UploadLatLngService";
    private static final String URL = "";

    private LocationManager locationManager;

    private LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            Date date = new Date(System.currentTimeMillis());
            int year = date.getYear();
            int month = date.getMonth() + 1;
            int day = date.getDay();
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();
            Map<String, String> map = new HashMap<>();
            map.put("date", year + "-" + month + "-" + day);
            map.put("latitude", latitude + "");
            map.put("longitude", longitude + "");

            VolleyUtil.stringRequest(UploadLatLngService.this, URL, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError volleyError) {
                    Log.e(TAG, "onErrorResponse: ", volleyError);
                }
            }, map);

            Log.i(TAG, "onLocationChanged: date " + year + "-" + month + "-" + day);
            Log.i(TAG, "onLocationChanged: " + latitude + "," + longitude);
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
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        //30s一次，距离10米以上
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 20000, 10, locationListener);
    }
}
