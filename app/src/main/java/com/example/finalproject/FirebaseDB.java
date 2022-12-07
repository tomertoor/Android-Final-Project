package com.example.finalproject;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.core.FirestoreClient;

public class FirebaseDB {
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    public FirebaseDB()
    {
        this.mAuth = FirebaseAuth.getInstance();
        this.db =FirebaseFirestore.getInstance();
    }
    public void register(String email, String password, String name)
    {
        this.mAuth.createUserWithEmailAndPassword(email, password);

    }

    public FirebaseUser login(String email, String password)
    {
        this.mAuth.signInWithEmailAndPassword(email, password);
        return this.mAuth.getCurrentUser();
    }

    public void createParking(GeoPoint point)
    {
        //this.db.collection("parking_list").add();
    }
}
