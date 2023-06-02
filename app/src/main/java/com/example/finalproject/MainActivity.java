package com.example.finalproject;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.Places;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.GeoPoint;

import java.util.List;
import java.util.Locale;


public class MainActivity extends AppCompatActivity {

    public static boolean isParking = false;

    BottomNavigationView bottomNavigationView;
    Settings settingsFragment = new Settings();
    ParkingMap mapFragment = new ParkingMap();
    SearchParkingFragment searchParking = new SearchParkingFragment();
    SearchMenu searchMenu = new SearchMenu();
    ProfileFragment profile = new ProfileFragment();
    FirebaseDB.User loggedUser;
    public static FloatingActionButton btnAddParking;
    FirebaseDB db;


    /*private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }*/

    public static void showParkingNotification(Context ctx, String message)
    {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(ctx, "parking_channel")
                .setSmallIcon(R.drawable.baseline_local_parking_24)
                .setContentTitle("Parking Alert")
                .setContentText(message)
                .setCategory(NotificationCompat.CATEGORY_CALL )
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        NotificationManager notificationManager = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);

        // Create a notification channel for Android Oreo and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("parking_channel", "Parking Channel", NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Alerts for parking.");
            notificationManager.createNotificationChannel(channel);
        }

        // Display the notification
        notificationManager.notify(0, builder.build());
    }
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
    public void hanldeSignup()
    {
        Intent signupIntent = new Intent(this, SignupActivity.class);
        getUser.launch(signupIntent);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getSupportActionBar().hide();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        db = new FirebaseDB();
        //FirebaseDB.Parking parking = new FirebaseDB.Parking(new GeoPoint(0,0), Timestamp.now(), "tomertom150@gmail.com");
        //
        //db.addParking(parking);
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), getString(R.string.MAPS_API_KEY), Locale.US);

        }
        if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED) {

        } else {
            ActivityCompat.requestPermissions(this, new String[] {
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION},
                    0);
        }
        ParkingManager.init(this);
        ParkingManager.SpeedTask manager = new ParkingManager.SpeedTask(this);
        manager.execute();


        db.login("tomer@gmail.com", "tomer123!");
        loggedUser = FirebaseDB.currentUser;

        //Intent signupIntent = new Intent(this, SignupActivity.class);
        //getUser.launch(signupIntent);
        btnAddParking = findViewById(R.id.addParking);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);

        //ParkingManager.init(this);
        //ParkingManager.SpeedTask task = new ParkingManager.SpeedTask(this);
       // task.execute("string");
        getSupportFragmentManager().beginTransaction().replace(R.id.container, mapFragment).commit();
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                item.setChecked(true);
                switch(item.getItemId())
                {
                    case R.id.home:
                        getSupportFragmentManager().beginTransaction().replace(R.id.container, mapFragment).commit();

                        break;
                    case R.id.search:
                        Bundle args = new Bundle();
                        args.putBoolean("isFirst", true);
                        searchParking.setArguments(args);
                        getSupportFragmentManager().beginTransaction().replace(R.id.container, searchParking).commit();
                        break;
                    case R.id.settings:
                        getSupportFragmentManager().beginTransaction().replace(R.id.container, settingsFragment).commit();
                        break;
                    case R.id.profile:
                        getSupportFragmentManager().beginTransaction().replace(R.id.container, profile).commit();
                        break;
                }
                return false;
            }
        });
    }

    public void toggleParking(View view)
    {
        if(!Settings.isAutomated)
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Are you sure?");
            builder.setMessage("Are you sure you want to toggle parking mode?");
            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    if(isParking)
                    {
                        db.addParking(new FirebaseDB.Parking(new GeoPoint(ParkingManager.currentLocation.getLatitude(), ParkingManager.currentLocation.getLongitude()), Timestamp.now(), loggedUser.email));//loggedUser.username));
                        btnAddParking.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.remove_parking)));
                        isParking = false;

                        showParkingNotification(MainActivity.this, "You have left parking");
                    }
                    else
                    {
                        db.removeParking(FirebaseDB.currentUser.email);

                        btnAddParking.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.add_parking)));
                        isParking = true;
                        showParkingNotification(MainActivity.this, "You have just parked");
                    }
                }
            });
            builder.setNegativeButton("No", null);
            builder.show();
        }


    }

    public void searchFragment()
    {
        getSupportFragmentManager().beginTransaction().replace(R.id.container, searchMenu).commit();
    }

}