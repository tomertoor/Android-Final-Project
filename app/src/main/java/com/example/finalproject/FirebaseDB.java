package com.example.finalproject;

import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.SignInMethodQueryResult;
import com.google.firebase.firestore.FirebaseFirestore;

public class FirebaseDB {
    private FirebaseAuth auth;
    private FirebaseFirestore fs;

    static enum SIGNUP_RESULTS {
        SUCCESS,
        EMAIL_EXISTS,
        INVALID_USERNAME,
        INVALID_PASSWORD,
        INVALID_NAME,

    }

    class User
    {
        public String email, password, fullName, username;
        User(String email, String password, String fullName, String username)
        {
            this.email = email;
            this.password = password;
            this.fullName = fullName;
            this.username = username;
        }
    }

    public FirebaseDB()
    {
        this.auth = FirebaseAuth.getInstance();
        this.fs = FirebaseFirestore.getInstance();
    }
    public SIGNUP_RESULTS register(String email, String password, String fullName, String username)
    {
        User user = new User(email, password, fullName, username);
        boolean exists;
        Task<SignInMethodQueryResult> task =  this.auth.fetchSignInMethodsForEmail(email);
        boolean isNewUser =  task.getResult().getSignInMethods().isEmpty();
        if(isNewUser)
        {
            auth.createUserWithEmailAndPassword(email, password);
            fs.collection("users").document(email).set(user);
            return SIGNUP_RESULTS.SUCCESS;
        }
        return SIGNUP_RESULTS.EMAIL_EXISTS;

    }

    public FirebaseUser login(String email, String password)
    {
        this.auth.signInWithEmailAndPassword(email, password);
        return this.auth.getCurrentUser();
    }

    public void logout()
    {
        this.auth.signOut();
    }
}
