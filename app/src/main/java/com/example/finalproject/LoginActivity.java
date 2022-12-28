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
    Intent intent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getSupportActionBar().hide();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        etUsername = findViewById(R.id.username);
        etPassword = findViewById(R.id.password);
        intent = getIntent();

    }

    public void login(View view)
    {
        String username = etUsername.getText().toString();
        String password = etPassword.getText().toString();
        FirebaseDB db = new FirebaseDB();

        if (db.login(username, password))
        {
            setResult(RESULT_OK, intent);;
            finish();
        }
        else
        {
            setResult(RESULT_CANCELED, intent);
        }
    }
    public void dontHaveAccount(View view)
    {
        setResult(RESULT_CANCELED, intent);
        finish();
    }
}