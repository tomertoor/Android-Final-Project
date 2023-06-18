package com.example.finalproject;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.dynamic.SupportFragmentWrapper;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.LinkedList;
import java.util.List;


public class ParkingMap extends Fragment implements OnMapReadyCallback, LocationListener {
    GoogleMap map;
    protected LocationManager locationManager;
    public static LatLng currentLocation;
    private boolean isFirstLocation = true;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Initialize view
        View view = inflater.inflate(R.layout.fragment_parking_map, container, false);

        // Initialize map fragment
        SupportMapFragment supportMapFragment = (SupportMapFragment)
                getChildFragmentManager().findFragmentById(R.id.map);

        // Async map
        supportMapFragment.getMapAsync(this);
        ActivityResultLauncher<String[]> locationPermissionRequest =
                registerForActivityResult(new ActivityResultContracts
                                .RequestMultiplePermissions(), result -> {
                            Boolean fineLocationGranted = result.get(
                                    Manifest.permission.ACCESS_FINE_LOCATION);
                            Boolean coarseLocationGranted = result.get(
                                    Manifest.permission.ACCESS_COARSE_LOCATION);
                            if (fineLocationGranted != null && fineLocationGranted) {
                                // Precise location access granted.
                            } else if (coarseLocationGranted != null && coarseLocationGranted) {
                                // Only approximate location access granted.
                            } else {
                                // No location access granted.
                            }
                        }
                );
        locationPermissionRequest.launch(new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        });

        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        UpdateParkingsTask task = new UpdateParkingsTask();
        task.execute();

        isFirstLocation = true;
        if(this.map != null)
        {
            CameraUpdate center=CameraUpdateFactory.zoomTo(19.785f);
            this.map.animateCamera(center);
        }

        return view;
    }

    @Override
    public void onLocationChanged(Location location) {

        LatLng newLocation = new LatLng(location.getLatitude(), location.getLongitude());


        ParkingMap.currentLocation = newLocation;
        if (isFirstLocation && this.map != null) {
            map.moveCamera(CameraUpdateFactory.newLatLng(ParkingMap.currentLocation));

            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 19.875f);
            map.animateCamera(cameraUpdate);
            isFirstLocation = false;
        }
        else
        {

            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLng(new LatLng(location.getLatitude(), location.getLongitude()));

            map.animateCamera(cameraUpdate);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.map = googleMap;
        this.map.setMapStyle(MapStyleOptions.loadRawResourceStyle(getContext(), R.raw.mapstyle));
        this.map.setMyLocationEnabled(true);
        CameraUpdate center=CameraUpdateFactory.zoomTo(19.785f);
        this.map.animateCamera(center);
        map.setOnCameraChangeListener(cameraPosition -> {
            if(cameraPosition.zoom < 15)
            {
                CameraUpdate zoomUpdate = CameraUpdateFactory.zoomTo(19.2f);
                map.animateCamera(zoomUpdate);

            }
        });


    }

    private class UpdateParkingsTask extends AsyncTask<Void, Integer, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {

                    handler.postDelayed(this, 10000);
                    FirebaseDB db = new FirebaseDB();
                    List<FirebaseDB.Parking> parkingList = db.getParkings();
                    ParkingMap.this.map.clear();
                    for(FirebaseDB.Parking parking : parkingList)
                    {
                        LatLng position = new LatLng(parking.location.getLatitude(), parking.location.getLongitude()); // convert from geopoint to latlng
                        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("images/" + parking.parkerName);
                        storageReference.getBytes(Long.MAX_VALUE).addOnSuccessListener(bytes -> {
                            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                            BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(bitmap);
                            ParkingMap.this.map.addMarker(new MarkerOptions().position(position).title("Free since: " + parking.creationTime.toDate().toString())).setIcon(bitmapDescriptor);
                        }).addOnFailureListener(runnable -> {
                            ParkingMap.this.map.addMarker(new MarkerOptions().position(position).title("Free since: " + parking.creationTime.toDate().toString()));
                        });
                    }

                }
            }, 5000);
        }
    }

}