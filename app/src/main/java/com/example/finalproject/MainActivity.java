package com.example.finalproject;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.GeoPoint;


public class MainActivity extends AppCompatActivity {

    BottomNavigationView bottomNavigationView;
    Settings settingsFragment = new Settings();
    ParkingMap mapFragment = new ParkingMap();
    FirebaseDB.User loggedUser;
    FloatingActionButton btnAddParking;
    FirebaseDB db;
    ActivityResultLauncher<Intent> getUser = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if(result.getResultCode() == RESULT_OK)
            {
                loggedUser = FirebaseDB.currentUser;
            }
            else
            {

            }
        }
    });
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getSupportActionBar().hide();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseDB.Parking parking = new FirebaseDB.Parking(new GeoPoint(0,0), Timestamp.now(), "tomertom150@gmail.com");
        db = new FirebaseDB();
        db.addParking(parking);
        if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED) {

        } else {
            ActivityCompat.requestPermissions(this, new String[] {
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION},
                    0);        }
        Intent signupIntent = new Intent(this, SignupActivity.class);
        getUser.launch(signupIntent);
        btnAddParking = findViewById(R.id.addParking);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        //getSupportFragmentManager().beginTransaction().replace(R.id.container, homeFragement).commit();
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch(item.getItemId())
                {
                    //case R.id.home:
                    //  getSupportFragmentManager().beginTransaction().replace(R.id.container, exampleFragment).commit();
                    //break;
                    case R.id.home:
                        getSupportFragmentManager().beginTransaction().replace(R.id.container, mapFragment).commit();
                        break;
                    case R.id.settings:
                        //getSupportFragmentManager().beginTransaction().replace(R.id.container, settingsFragment).commit();
                        break;
                }
                return false;
            }
        });
    }

    public void toggleParking(View view)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Are you sure?");
        builder.setMessage("Are you sure you want to toggle parking mode?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        db.addParking(new FirebaseDB.Parking(new GeoPoint(ParkingMap.currentLocation.latitude, ParkingMap.currentLocation.longitude), Timestamp.now(), loggedUser.username));//loggedUser.username));
                    }
                });
        builder.setNegativeButton("No", null);
        builder.show();
        btnAddParking.setImageResource(R.drawable.ic_baseline_remove_24);
        btnAddParking.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.remove_parking)));

    }
}