package com.example.finalproject;

import android.content.Context;
import android.content.res.Resources;
import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.GeoPoint;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.ExecutionException;

public class ParkingCell extends FirebaseDB.Parking{
    private GeoPoint searchedLocation;
    private String timeToGetThere;
    private int distance;
    private Context ctx;
    ParkingCell(GeoPoint searchedLocation, GeoPoint location, Timestamp creationTime, String parkerName, Context ctx) throws IOException, ExecutionException, InterruptedException {
        super(location, creationTime, parkerName);
        this.ctx = ctx;
        this.searchedLocation = searchedLocation;
        float[] results = new float[3]; // according to docs the function can return up to 3 cells in the array
        Location.distanceBetween(this.searchedLocation.getLatitude(), this.searchedLocation.getLongitude(), this.location.getLatitude(), this.location.getLongitude(), results); //Checks distance between the searched location and the actual parking location
        this.distance = (int) results[0];
        this.timeToGetThere = (new RetrieveTimeToGetThere().execute()).get();

    }


    public GeoPoint getSearchedLocation()
    {
        return this.searchedLocation;
    }
    public String getTimeToGetThere()
    {
        return this.timeToGetThere;
    }
    public int getDistance()
    {
        return this.distance;
    }

    private class RetrieveTimeToGetThere extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... voids) {
            Resources resources = ctx.getResources();
            String url = "https://maps.googleapis.com/maps/api/directions/json?origin="+ (searchedLocation.getLatitude() + "," + searchedLocation.getLongitude()) + "&destination=" + (location.getLatitude() + "," + location.getLongitude()) + "&key=" + resources.getString(R.string.MAPS_API_KEY);
            URLConnection con = null;
            StringBuffer buffer = new StringBuffer();
            try {
                con = new URL(url).openConnection();
                con.connect();
                InputStream stream = con.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                }
            } catch (IOException e) {
            }

            JSONObject jsonObject = null;
            try {
                jsonObject = new JSONObject(buffer.toString());
                return jsonObject.getJSONArray("routes").getJSONObject(0).getJSONArray("legs").getJSONObject(0).getJSONObject("duration").getString("text");


            } catch (JSONException e) {
            }
            return "Can't reach there";

        }
        /*@Override
        protected void onPostExecute(String s) {
            try {
                JSONObject jsonObject = new JSONObject(s);

                timeToGetThere = jsonObject.getJSONArray("routes").getJSONObject(0).getJSONArray("legs").getJSONObject(0).getJSONObject("duration").getString("text");

            } catch (JSONException e) {
            }

        }*/
    }

}
