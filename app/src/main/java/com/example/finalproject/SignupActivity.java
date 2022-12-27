package com.example.finalproject;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;

public class SignupActivity extends AppCompatActivity {

    EditText etUsername, etPassword, etEmail, etFullname;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getSupportActionBar().hide();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        Intent intent = getIntent();
        etUsername = findViewById(R.id.username);
        etPassword = findViewById(R.id.password);
        etEmail = findViewById(R.id.email);
        etFullname = findViewById(R.id.fullname);
    }

    public void haveAccount(View view)
    {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    public void signup(View view) {
        String username, password, email, fullname;
        username = etUsername.getText().toString();
        password = etPassword.getText().toString();
        email = etEmail.getText().toString();
        fullname = etFullname.getText().toString();
        FirebaseDB db = new FirebaseDB();
        try
        {
            FirebaseDB.SIGNUP_RESULTS result = db.register(email, password, fullname, username);
        }
        catch (FirebaseAuthInvalidCredentialsException e)
        {
            return;
        }
        Intent intent = new Intent();
        if (db.login(email, password))
        {
            setResult(RESULT_OK, intent);
            finish();
        }
        else
        {
            setResult(RESULT_CANCELED, intent);
        }
    }
}