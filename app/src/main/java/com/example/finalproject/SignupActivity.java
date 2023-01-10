package com.example.finalproject;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;

public class SignupActivity extends AppCompatActivity {

    Intent receiveIntent;
    EditText etUsername, etPassword, etEmail, etFullname;
    ActivityResultLauncher<Intent> onResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if(result.getResultCode() == RESULT_OK)
            {
                setResult(RESULT_OK, receiveIntent);
                finish();

            }
            else
            {
                setResult(RESULT_CANCELED, receiveIntent);
            }
        }
    });
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getSupportActionBar().hide();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        receiveIntent = getIntent();
        etUsername = findViewById(R.id.username);
        etPassword = findViewById(R.id.password);
        etEmail = findViewById(R.id.email);
        etFullname = findViewById(R.id.fullname);
    }

    public void haveAccount(View view)
    {
        Intent intent = new Intent(this, LoginActivity.class);

        onResult.launch(intent);
    }

    public void signup(View view) {
        String username, password, email, fullname;
        username = etUsername.getText().toString();
        password = etPassword.getText().toString();
        email = etEmail.getText().toString();
        fullname = etFullname.getText().toString();
        FirebaseDB db = new FirebaseDB();
        if(!isNameValid(fullname))
        {
            Toast.makeText(getApplicationContext(), "Name invalid", Toast.LENGTH_SHORT).show();
            return;
        }
        if(!isEmailValid(email))
        {
            Toast.makeText(getApplicationContext(), "Email invalid", Toast.LENGTH_SHORT).show();
            return;
        }
        if(!isPasswordValid(password))
        {
            Toast.makeText(getApplicationContext(), "Password invalid", Toast.LENGTH_SHORT).show();
            return;
        }
        
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
    public boolean isNameValid(String name)
    {
        /*if (firstname.length() == 0) {
            return false;
        }
        for (int i = 0; i < firstname.length(); i++) {
            if (!(firstname.charAt(i) > 'a' && firstname.charAt(i) < 'z' || firstname.charAt(i) > 'A' && firstname.charAt(i) < 'Z')) {
                return false;
            }
            else if(firstname.charAt(i) == ' ')
            {
                continue;
            }
        }
        return true;*/
        String expression = "^[a-zA-Z ]*$";
        return name.matches(expression);
    }
    public  boolean isEmailValid(String email)
    {
        String checkEmail = "[a-zA-Z0-9._-]+@[a-z]+.+[a-z]+";
        if (email.isEmpty())
            return false;
        else if (!email.matches(checkEmail))
            return false;
        return true;
    }

    public boolean isPasswordValid(String password)
    {
        String checkPassword = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}$";
        if (password.isEmpty())
            return false;
        else if (!password.matches(checkPassword))
            return false;
        else
            return true;
    }

}