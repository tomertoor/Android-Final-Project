package com.example.finalproject;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import android.location.LocationListener;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.GeoPoint;
import com.google.maps.GeoApiContext;
import com.google.maps.RoadsApi;
import com.google.maps.SpeedLimitsApiRequest;
import com.google.maps.model.SpeedLimit;

import org.w3c.dom.Node;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

public class ParkingManager {
    private FirebaseDB db = new FirebaseDB();
    private final static int UPDATE_INTERVAL = 1000 * 20; // 20 seconds
    public static List<FirebaseDB.Parking> parkingList;
    public static Handler handler = new Handler();
    public static Location currentLocation;
    private static Context ctx;

    public static boolean isParked;
    public static void init(Context ctx)
    {
        ParkingManager.isParked = false;
        ParkingManager.ctx = ctx;
    }

    public static FirebaseDB.Parking convertMapToParking(Map<String, Object> parking)
    {
        return new FirebaseDB.Parking((GeoPoint) parking.get("location"), (Timestamp) parking.get("creationTime"), (String) parking.get("parkerName"));
    }


    public static class SpeedTask extends AsyncTask<String, Void, String> implements LocationListener {
        GeoApiContext context = new GeoApiContext.Builder().apiKey(ctx.getString(R.string.MAPS_API_KEY)).build();
        SpeedLimit[] speedLimits = new SpeedLimit[0];
        final MainActivity activity;
        float speed = 0.0f;
        double lat;
        private double maxSpeed = -100.0;
        private static Location parkingLocation;
        LinkedList<Location> locationList = new LinkedList<>();
        LocationManager locationManager;
        final int MAX_LOCATION_LIST = 20;
        final int PARKING_SPEED = 8;
        final float MAX_DISTANCE_FOR_PARKING = 2;


        private boolean inferParking()
        {
            for(Location loc : locationList)
            {
                if(loc.getSpeed() > PARKING_SPEED)
                {
                    return false;
                }
            }

            for(int i = 0 ; i < locationList.size() - 1; i++)
            {
                if (locationList.get(i).distanceTo(locationList.get(i + 1)) > MAX_DISTANCE_FOR_PARKING)
                {
                    return false;
                }
            }
            return true;

        }
        public SpeedTask(MainActivity activity) {
            this.activity = activity;
        }

        @Override
        protected String doInBackground(String... params) {
            locationManager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);

            return null;

        }

        @Override
        public void onLocationChanged(Location location) {
            currentLocation = location;
            if(Settings.isAutomated)
            {
                FirebaseDB db = new FirebaseDB();
                Toast.makeText(activity, "SSS1", Toast.LENGTH_LONG).show();
                if (locationList.size() == MAX_LOCATION_LIST) {
                    locationList.poll();
                }
                locationList.add(location);
                boolean currentIsParked = false;
                if(locationList.size() == MAX_LOCATION_LIST)
                {
                    currentIsParked = inferParking();
                    ParkingMap.speedText.setText((currentIsParked ? "True" : "False"));

                    if (isParked && !currentIsParked && locationList.get(locationList.size()-1).distanceTo(parkingLocation) < 10)
                    {
                        AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
                        builder.setTitle("Have you just parked?").setMessage("We detected that you had just parked your vehicle.").setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                db.addParking(new FirebaseDB.Parking(new GeoPoint(location.getLatitude(), location.getLongitude()), Timestamp.now(), FirebaseDB.currentUser.email));
                            }
                        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        });

                        isParked = false;
                    }
                    else if(!isParked && currentIsParked)
                    {
                        List<FirebaseDB.Parking> parkings = db.getParkings();
                        if(parkings.size() > 0)
                        {
                            FirebaseDB.Parking closestParking = parkings.get(0);
                            for(FirebaseDB.Parking parking : parkings)
                            {
                                if(location.distanceTo(new Location(parking.location.toString())) < location.distanceTo(new Location(closestParking.location.toString())))
                                {
                                    closestParking = parking;
                                }
                            }
                            float [] results = new float[5];
                            Location.distanceBetween(location.getLatitude(), location.getLongitude(), closestParking.location.getLatitude(), closestParking.location.getLongitude(), results);
                            if(results[0] < 10)
                            {
                                db.removeParking(closestParking.parkerName);
                            }
                        }

                        parkingLocation = locationList.get(locationList.size()-1);
                        isParked = true;
                    }
                }
                }






            /*speed = location.getSpeed();
            float multiplier = 3.6f;
            if(speed > 5)
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
                    }

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

*/
        }


        @SuppressLint("MissingPermission")
        protected void onPostExecute(String result) {
            FirebaseDB db = new FirebaseDB();
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);

            /*LocationListener listener = new LocationListener() {
                float filtSpeed;
                float localspeed;



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
                    tvAccuracy.setText("ACCURACY");

                }

                @Override
                public void onProviderDisabled(String provider) {
                    ParkingMap.speedText.setText("nofix");



                }

            };*/


            //locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, listener);


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
