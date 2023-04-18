package com.example.finalproject;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.GeoPoint;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ParkingManager {
    private FirebaseDB db = new FirebaseDB();
    private final static int UPDATE_INTERVAL = 1000 * 20; // 20 seconds
    public static List<FirebaseDB.Parking> parkingList;
    public static Handler handler = new Handler();
    private static Context ctx;
    public static void init(Context ctx)
    {
        ParkingManager.ctx = ctx;
    }

    public static FirebaseDB.Parking convertMapToParking(Map<String, Object> parking)
    {
        return new FirebaseDB.Parking((GeoPoint) parking.get("location"), (Timestamp) parking.get("creationTime"), (String) parking.get("parkerName"));
    }


    public static class SpeedTask extends AsyncTask<String, Void, String> {
        final MainActivity activity;
        float speed = 0.0f;
        double lat;
        private double maxSpeed = -100.0;

        LocationManager locationManager;

        public SpeedTask(MainActivity activity) {
            this.activity = activity;
        }

        @Override
        protected String doInBackground(String... params) {
            locationManager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);


            return null;

        }

        @SuppressLint("MissingPermission")
        protected void onPostExecute(String result) {
            LocationListener listener = new LocationListener() {
                float filtSpeed;
                float localspeed;

                @Override
                public void onLocationChanged(Location location) {
                    speed = location.getSpeed();
                    float multiplier = 3.6f;

                    if (maxSpeed < speed) {
                        maxSpeed = speed;
                    }


                    localspeed = speed * multiplier;

                    filtSpeed = filter(filtSpeed, localspeed, 2);



                    NumberFormat numberFormat = NumberFormat.getNumberInstance();
                    numberFormat.setMaximumFractionDigits(0);



                    lat = location.getLatitude();
                    //speed=(float) location.getLatitude();
                    Log.d("net.mypapit.speedview", "Speed " + localspeed + "latitude: " + lat + " longitude: " + location.getLongitude());
                    ParkingMap.speedText.setText(numberFormat.format(filtSpeed));

                    /*if (location.hasAltitude()) {
                        tvAccuracy.setText(numberFormat.format(location.getAccuracy()) + " m");
                    } else {
                        tvAccuracy.setText("NIL");
                    }*/

                    numberFormat.setMaximumFractionDigits(0);


                    if (location.hasBearing()) {

                        double bearing = location.getBearing();
                        String strBearing = "NIL";
                        if (bearing < 20.0) {
                            strBearing = "North";
                        } else if (bearing < 65.0) {
                            strBearing = "North-East";
                        } else if (bearing < 110.0) {
                            strBearing = "East";
                        } else if (bearing < 155.0) {
                            strBearing = "South-East";
                        } else if (bearing < 200.0) {
                            strBearing = "South";
                        } else if (bearing < 250.0) {
                            strBearing = "South-West";
                        } else if (bearing < 290.0) {
                            strBearing = "West";
                        } else if (bearing < 345.0) {
                            strBearing = "North-West";
                        } else if (bearing < 361.0) {
                            strBearing = "North";
                        }

                        //tvHeading.setText(strBearing);
                    } else {
                        //tvHeading.setText("NIL");
                    }

                    NumberFormat nf = NumberFormat.getInstance();

                    nf.setMaximumFractionDigits(4);


                    //tvLat.setText(nf.format(location.getLatitude()));
                    //tvLon.setText(nf.format(location.getLongitude()));


                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {
                    // TODO Auto-generated method stub

                }

                @Override
                public void onProviderEnabled(String provider) {
                    ParkingMap.speedText.setText("STDBY");
                    /*tvMaxSpeed.setText("NIL");

                    tvLat.setText("LATITUDE");
                    tvLon.setText("LONGITUDE");
                    tvHeading.setText("HEADING");
                    tvAccuracy.setText("ACCURACY");*/

                }

                @Override
                public void onProviderDisabled(String provider) {
                    ParkingMap.speedText.setText("nofix");



                }

            };


            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, listener);


        }

        /**
         * Simple recursive filter
         *
         * @param prev Previous value of filter
         * @param curr New input value into filter
         * @return New filtered value
         */
        private float filter(final float prev, final float curr, final int ratio) {
            // If first time through, initialise digital filter with current values
            if (Float.isNaN(prev))
                return curr;
            // If current value is invalid, return previous filtered value
            if (Float.isNaN(curr))
                return prev;
            // Calculate new filtered value
            return (float) (curr / ratio + prev * (1.0 - 1.0 / ratio));
        }


    }

    private boolean isLocationEnabled(Context mContext) {


        LocationManager locationManager = (LocationManager)
                mContext.getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }
}
