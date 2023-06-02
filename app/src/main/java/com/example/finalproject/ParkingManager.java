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
        public final int MAX_LOCATION_LIST = 20;
        public final int PARKING_SPEED = 8;
        public final int DRIVING_SPEED = 15;
        public final float MAX_DISTANCE_FOR_PARKING = 2;
        public final float MIN_DISTANCE_FOR_DRIVING = 7;
        public static boolean isDriving = false;

        private void isDriving()
        {
            int countHighSpeed = 0;
            int countDistance = 0;
            for(Location loc : locationList)
            {
                if(loc.getSpeed() * 3.6 > DRIVING_SPEED)
                {
                    countHighSpeed++;
                }
            }

            for(int i = 0 ; i < locationList.size() - 1; i++)
            {
                if (locationList.get(i).distanceTo(locationList.get(i + 1)) > MIN_DISTANCE_FOR_DRIVING)
                {
                    countDistance++;
                }
            }
            if(countHighSpeed + countDistance >= (locationList.size() / 4))
            {
                Toast.makeText(ctx, "I detected that you are driving", Toast.LENGTH_LONG).show();
                SpeedTask.isDriving = true;
            }

        }
        private boolean inferParking()
        {
            for(Location loc : locationList)
            {
                if(loc.getSpeed() * 3.6 > PARKING_SPEED)
                {
                    isDriving();
                    return false;
                }
            }

            for(int i = 0 ; i < locationList.size() - 1; i++)
            {
                if (locationList.get(i).distanceTo(locationList.get(i + 1)) > MAX_DISTANCE_FOR_PARKING)
                {
                    isDriving();
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
                if (locationList.size() == MAX_LOCATION_LIST) {
                    locationList.poll();
                }
                locationList.add(location);
                boolean currentIsParked = false;
                if(locationList.size() == MAX_LOCATION_LIST)
                {
                    currentIsParked = inferParking();
                    //ParkingMap.speedText.setText((currentIsParked ? "True" : "False"));

                    Toast.makeText(activity, "Isparked: " + isParked + " Current is parked: " + currentIsParked + " is Driving: " + isDriving  , Toast.LENGTH_LONG).show();
                    if (isParked && !currentIsParked && locationList.get(locationList.size()-1).distanceTo(parkingLocation) < 10)
                    {

                        db.addParking(new FirebaseDB.Parking(new GeoPoint(location.getLatitude(), location.getLongitude()), Timestamp.now(), FirebaseDB.currentUser.email));


                        isParked = false;
                        MainActivity.showParkingNotification(ctx, "You have left parking");

                    }
                    else if(isDriving && !isParked && currentIsParked)
                    {
                        AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
                        builder.setTitle("Have you just parked?").setMessage("We detected that you had just parked your vehicle.").setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
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
                                SpeedTask.isDriving = false;
                                MainActivity.showParkingNotification(ctx, "You have just parked");                            }
                        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        }).show();


                    }
                }
                }

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
