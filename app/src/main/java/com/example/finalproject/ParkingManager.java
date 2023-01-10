package com.example.finalproject;

import android.os.Handler;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.GeoPoint;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ParkingManager implements Runnable{
    private FirebaseDB db = new FirebaseDB();
    private final static int UPDATE_INTERVAL = 1000 * 20; // 20 seconds
    public static List<FirebaseDB.Parking> parkingList;
    public static Handler handler = new Handler();
    @Override
    public void run()
    {
        parkingList = db.getParkings();
        handler.postDelayed(this::run, UPDATE_INTERVAL);
    }

    public void addParking() {

    }

    public static FirebaseDB.Parking convertMapToParking(Map<String, Object> parking)
    {
        return new FirebaseDB.Parking((GeoPoint) parking.get("location"), (Timestamp) parking.get("creationTime"), (String) parking.get("parkerName"));
    }
}
