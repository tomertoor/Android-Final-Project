package com.example.finalproject;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    EditText etUsername, etPassword;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getSupportActionBar().hide();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        etUsername = findViewById(R.id.username);
        etPassword = findViewById(R.id.password);
        Intent intent = getIntent();

    }

    public void login(View view)
    {
        String username = etUsername.getText().toString();
        String password = etPassword.getText().toString();
        FirebaseDB db = new FirebaseDB();
        Intent intent = new Intent(this, MainActivity.class);

        if (db.login(username, password))
        {
            setResult(RESULT_OK, intent);;
        }
        else
        {
            setResult(RESULT_CANCELED, intent);
        }
        finish();
    }
    public void dontHaveAccount(View view)
    {
        Intent intent = new Intent(this, SignupActivity.class);
        startActivity(intent);
    }
}