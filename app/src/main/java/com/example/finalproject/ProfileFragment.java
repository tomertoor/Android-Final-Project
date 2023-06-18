package com.example.finalproject;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class ProfileFragment extends Fragment {


    TextView email, fullName, gpsEnabled, parkingsReported, isParked;
    ImageView profileView;
    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        email = view.findViewById(R.id.emailProfile);
        fullName = view.findViewById(R.id.fullNameProfile);
        gpsEnabled = view.findViewById(R.id.gpsEnabled);
        parkingsReported = view.findViewById(R.id.parkingsReported);
        profileView = view.findViewById(R.id.profileicon);
        isParked = view.findViewById(R.id.isParked);

        FirebaseDB db = new FirebaseDB();
        LocationManager manager = (LocationManager) getActivity().getSystemService( Context.LOCATION_SERVICE );
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            gpsEnabled.setText(((manager.isLocationEnabled() ? "Enabled" : "Disabled")));
        }
        try
        {
            long parkingAmounts = db.getAmountOfParkings(FirebaseDB.currentUser.email);
            parkingsReported.setText(parkingAmounts +"");
            StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("images/" + FirebaseDB.currentUser.email);

            storageReference.getBytes(Long.MAX_VALUE).addOnSuccessListener(bytes -> {
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                profileView.setImageBitmap(bitmap);
            });

        }
        finally
        {
        }
        email.setText(FirebaseDB.currentUser.email);
        fullName.setText(FirebaseDB.currentUser.fullName);




        if(MainActivity.isParking || ParkingManager.isParked)
        {
            isParked.setText("Parking");
        }
        else
        {
            isParked.setText("Not parked");
        }

        return view;
    }

}