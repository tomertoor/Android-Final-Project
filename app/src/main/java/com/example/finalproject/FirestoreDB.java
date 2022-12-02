package com.example.finalproject;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

public class FirestoreDB {
    private FirebaseFirestore db;
    public FirestoreDB()
    {
        this.db = FirebaseFirestore.getInstance();
    }
    public boolean login(String name, String password)
    {
        Map<String, String> user = new HashMap<>();
        user.put("username", name);
        user.put("password", password);
        QuerySnapshot result = this.db.collection("users").whereEqualTo("name", name).get().getResult();

        for(QueryDocumentSnapshot document : result)
        {
            if(document.get("password").toString().equals(password))
            {
                return true;
            }
        }
        return false;

    }
    public void register(String name, String password, String phone)
    {
        Map<String, String> user = new HashMap<>();
        user.put("username", name);
        user.put("password", password);
        user.put("phone", phone);
        this.db.collection("users").add(user).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
            @Override
            public void onComplete(@NonNull Task<DocumentReference> task) {
                Log.d("t", "Sadas");
            }
        });
    }





}
