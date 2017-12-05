package com.example.android.aarv1;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

public class MainActivity extends AppCompatActivity {

    // Access a Cloud Firestore instance from your Activity
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    CollectionReference collectionRef = db.collection("aars");

    // Document Reference grabs a single document
    DocumentReference docRef = db.collection("aars").document("bU2QmGtG6hCTfpkUJB5I");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set up FAB to open editorActivity (incoming)
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent editIntent = new Intent(MainActivity.this,EditorActivity.class);
                startActivity(editIntent);
            }
        });

        // Just try to pull data any way that you can.
        // I need to grab data and turn it back into a custom class...
        Log.v("MainActivity.java","collectionRef = " + collectionRef);

        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()){
                    DocumentSnapshot document = task.getResult();
                    if (document != null) {
                        Log.v("MainActivity.java","document: " + task.getResult().getData());
                    } else {
                        Log.v("MainActivity.java","no document exists");
                    }
                } else {
                    Log.v("MainActivity.java","get failed with: ", task.getException());
                }
            }
        });

        // this grabs all of the aars in the db, but it does it in real time, need to do async task?
        collectionRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (DocumentSnapshot document : task.getResult()) {
                        Log.v("MainActivity.java",document.getId() + " => " + document.getData());
                    }
                } else {
                    Log.v("MainActivity.java","Error getting documents", task.getException());
                }
            }
        });


        // query the firestore db
        Query query = db
                .collection("aars")
                .orderBy("timestamp")
                .limit(50);



    }
}
