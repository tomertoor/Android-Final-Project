package com.example.finalproject;

import android.content.Context;
import android.content.res.Resources;
import android.location.Location;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.SignInMethodQueryResult;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import com.example.finalproject.R;

public class FirebaseDB {
    private static final String PARKING_COLLECTION = "parkings";
    private static final String USERS_COLLECTION = "users";
    private FirebaseAuth auth;
    private FirebaseFirestore fs;
    public static User currentUser = null;
    static enum SIGNUP_RESULTS {
        SUCCESS,
        EMAIL_EXISTS,
        INVALID_USERNAME,
        INVALID_PASSWORD,
        INVALID_NAME,

    }

    class User implements Serializable
    {
        public String email, password, fullName, username;
        public FirebaseUser user;
        User(String email, String password, String fullName, String username, FirebaseUser user)
        {
            this.email = email;
            this.password = password;
            this.fullName = fullName;
            this.username = username;
            this.user = user;
        }
        User(String email, String password, String fullName, String username)
        {
            this.email = email;
            this.password = password;
            this.fullName = fullName;
            this.username = username;
            this.user = null;
        }
    }

    static class Parking
    {
        public GeoPoint location;
        public Timestamp creationTime;
        public String parkerName;
        Parking(GeoPoint location, Timestamp creationTime, String parkerName)
        {
            this.location = location;
            this.creationTime = creationTime;
            this.parkerName = parkerName;
        }
    }


    public FirebaseDB()
    {
        this.auth = FirebaseAuth.getInstance();
        this.fs = FirebaseFirestore.getInstance();
    }
    public SIGNUP_RESULTS register(String email, String password, String fullName, String username) throws FirebaseAuthInvalidCredentialsException
    {
        User user = new User(email, password, fullName, username);
        boolean exists;
        Task<SignInMethodQueryResult> task = this.auth.fetchSignInMethodsForEmail(email);
        while(!task.isComplete()) {}
        boolean isNewUser =  task.getResult().getSignInMethods().isEmpty();
        if(isNewUser)
        {
            auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    int x = 5;
                }
            });
            fs.collection(USERS_COLLECTION).document(email).set(user);
            fs.collection(USERS_COLLECTION).document(email).update("parkingTimes", 0);
            return SIGNUP_RESULTS.SUCCESS;
        }
        return SIGNUP_RESULTS.EMAIL_EXISTS;

    }

    public boolean login(String email, String password, boolean isAfterRegister)
    {
        FirebaseUser user = this.auth.getCurrentUser();
        Task<AuthResult> task = this.auth.signInWithEmailAndPassword(email, password);
        user = this.auth.getCurrentUser();
        while(!task.isComplete()) {}
        if(isAfterRegister && !task.isSuccessful())
        {
            while(!task.isSuccessful())
            {
                task = this.auth.signInWithEmailAndPassword(email, password);
                while(!task.isComplete()) {}
            }
        }
        else if(!isAfterRegister && !task.isSuccessful())
        {
            return false;
        }

        Task<DocumentSnapshot> fireStoreTask = fs.collection("users").document(email).get();
        while(!fireStoreTask.isComplete()) {}

        FirebaseDB.currentUser = new User(email, password, fireStoreTask.getResult().get("fullName").toString(), fireStoreTask.getResult().get("username").toString(), this.auth.getCurrentUser());
        return true;
    }

    public void logout()
    {
        this.auth.signOut();
    }

    public void removeParking(String identifier)
    {
        this.fs.collection(PARKING_COLLECTION).document(identifier).delete();
    }
    public void addParking(Parking parking)
    {
        this.fs.collection(PARKING_COLLECTION).document(parking.parkerName).set(parking);
        this.incrementParkingTimes(parking.parkerName);
    }
    public List<Parking> getParkings()
    {
        List<Parking> parkingList = new ArrayList<>();
        //List<DocumentSnapshot> parkingCollection = this.fs.collection("parking").get().getResult().getDocuments();
        Task<QuerySnapshot> task = this.fs.collection("parkings").get();
        while(!task.isComplete()) {}
        List<DocumentSnapshot> parkingCollection = task.getResult().getDocuments();
        for(DocumentSnapshot i : parkingCollection)
        {
            parkingList.add(ParkingManager.convertMapToParking(i.getData()));
        }
        return parkingList;
    }

    private void incrementParkingTimes(String identifier)
    {
        this.fs.collection(USERS_COLLECTION).document(identifier).update("parkingTimes", FieldValue.increment(1));
    }
    public long getAmountOfParkings(String identifier)
    {
        Task<DocumentSnapshot> task = this.fs.collection(USERS_COLLECTION).document(identifier).get();
        while(!task.isComplete()) {}
        return (long)task.getResult().get("parkingTimes");
    }
}
