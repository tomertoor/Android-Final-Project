package com.example.finalproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseUser;


public class MainActivity extends AppCompatActivity {

    BottomNavigationView bottomNavigationView;
    Settings settingsFragment = new Settings();
    FirebaseDB.User loggedUser;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Intent signupIntent = new Intent(this, SignupActivity.class);
        startActivity(signupIntent);

        Intent userIntent = getIntent();
        loggedUser = (FirebaseDB.User) userIntent.getSerializableExtra("user");
        //FirebaseDB db = new FirebaseDB();
        //FirebaseUser user = db.login("tomer@gmail.com", "tomer123");
        //String s = user.getEmail();
        //db.register("hgfhfgh@gmail.com", "sadasd", "dsad", "dsdad");

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
                        //getSupportFragmentManager().beginTransaction().replace(R.id.container, homeFragement).commit();
                        break;
                    case R.id.settings:
                        //getSupportFragmentManager().beginTransaction().replace(R.id.container, settingsFragment).commit();
                        break;
                }
                return false;
            }
        });
    }
}