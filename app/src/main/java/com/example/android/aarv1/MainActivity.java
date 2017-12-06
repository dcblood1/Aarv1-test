package com.example.android.aarv1;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.android.aarv1.adapter.AARHolder;
import com.example.android.aarv1.model.AAR;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    // Access a Cloud Firestore instance from your Activity
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    CollectionReference collectionRef = db.collection("aars");

    // Document Reference grabs a single document
    DocumentReference docRef = db.collection("aars").document("bU2QmGtG6hCTfpkUJB5I");

    // query the firestore db
    Query query = db
            .collection("aars")
    .orderBy("timeStamp", Query.Direction.DESCENDING)
    .limit(50);

    Query query2 = db.collection("aars").orderBy("timeStamp", Query.Direction.ASCENDING);

    @BindView(R.id.recycler_aars)
    RecyclerView mAarsRecycler;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // dont know what this does...
        ButterKnife.bind(this);

        mAarsRecycler.setHasFixedSize(true);
        mAarsRecycler.setLayoutManager(new LinearLayoutManager(this));


        // Set up FAB to open editorActivity (incoming)
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent editIntent = new Intent(MainActivity.this, EditorActivity.class);
                startActivity(editIntent);
            }
        });

        // Just try to pull data any way that you can.
        // I need to grab data and turn it back into a custom class...
        Log.v("MainActivity.java", "collectionRef = " + collectionRef);

        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document != null) {
                        Log.v("MainActivity.java", "document: " + document.exists());
                    } else {
                        Log.v("MainActivity.java", "no document exists");
                    }
                } else {
                    Log.v("MainActivity.java", "get failed with: ", task.getException());
                }
            }
        });

        // this grabs all of the aars in the db, but it does it in real time, need to do async task?
        collectionRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (DocumentSnapshot document : task.getResult()) {
                        Log.v("MainActivity.java", document.getId() + " => " + document.getData());
                    }
                } else {
                    Log.v("MainActivity.java", "Error getting documents", task.getException());
                }
            }
        });

    };// end onCreate()


    @Override
    protected void onStart() {
        super.onStart();
        attachRecyclerViewAdapter();
    }

    private void attachRecyclerViewAdapter() {
        final RecyclerView.Adapter adapter = newAdapter();
        mAarsRecycler.setAdapter(adapter);
    }

    protected RecyclerView.Adapter newAdapter() {
        FirestoreRecyclerOptions<AAR> options =
                new FirestoreRecyclerOptions.Builder<AAR>()
                        .setQuery(query, AAR.class)
                        .setLifecycleOwner(this)
                        .build();

        Log.v(TAG,"this is the query: " + query);

        return new FirestoreRecyclerAdapter<AAR, AARHolder>(options) {
            @Override
            public AARHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                return new AARHolder(LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.aar_item, parent, false));
            }

            @Override
            protected void onBindViewHolder(AARHolder holder, int position, AAR model) {
                holder.bind(model);
            }
        };
    }

}

