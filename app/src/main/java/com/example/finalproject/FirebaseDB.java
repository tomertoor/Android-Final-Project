package com.example.finalproject;

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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class FirebaseDB {
    private static final String PARKING_COLLECTION = "parkings";
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
        Task<SignInMethodQueryResult> task =  this.auth.fetchSignInMethodsForEmail(email);
        boolean isNewUser =  task.getResult().getSignInMethods().isEmpty();
        if(isNewUser)
        {
            auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    int x = 5;
                }
            });
            fs.collection("users").document(email).set(user);
            return SIGNUP_RESULTS.SUCCESS;
        }
        return SIGNUP_RESULTS.EMAIL_EXISTS;

    }

    public boolean login(String email, String password)
    {
        FirebaseUser user = this.auth.getCurrentUser();
        Task<AuthResult> task = this.auth.signInWithEmailAndPassword(email, password);
        user = this.auth.getCurrentUser();
        while(!task.isComplete()) {}
        if(!task.isSuccessful())
        {
            return false;
        }
        FirebaseDB.currentUser = new User(email, password, "", "", this.auth.getCurrentUser());
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
    }
    public List<Parking> getParkings()
    {
        List<Parking> parkingList = new ArrayList<>();
        List<DocumentSnapshot> parkingCollection = this.fs.collection("parking").get().getResult().getDocuments();
        for(DocumentSnapshot i : parkingCollection)
        {
            parkingList.add(ParkingManager.convertMapToParking(i.getData()));
        }
        return parkingList;
    }
}
