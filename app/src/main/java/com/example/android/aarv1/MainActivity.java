package com.example.android.aarv1;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.aarv1.adapter.AarAdapter;
import com.example.android.aarv1.model.AAR;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements AarAdapter.OnAarSelectedListener {

    private static final String TAG = "MainActivity";

    // Access a Cloud Firestore instance from your Activity
    private FirebaseFirestore db;
    private FirestoreRecyclerAdapter adapter;
    private AarAdapter mAarAdapter; // the testing aar
    LinearLayoutManager mLinearLayoutManager;


    static {
        //helps me see what the f is going on maybe in the firestore
        FirebaseFirestore.setLoggingEnabled(true);
    }


    //recycler view state
    private final String KEY_RECYCLER_STATE = "recycler_state";
    private static Bundle mBundleRecyclerViewState;
    private static Parcelable mListState;

    // query the firestore db
    private Query mQuery;

    @BindView(R.id.recycler_aars)
    RecyclerView mAarsRecycler;

    @BindView(R.id.empty_text_view)
    TextView emptyTextView;

    @BindView(R.id.progress_bar_view)
    ProgressBar prograssBarView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        Log.v(TAG,("OnCreate being called here:"));
        init();
        getAars();

        // Set up FAB to open editorActivity (incoming)
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent editIntent = new Intent(MainActivity.this, EditorActivity.class);
                startActivity(editIntent);
            }
        });

    };// end onCreate()

    protected void init(){
        //set new LinearLayoutManager, attach recyclerView to layoutManager, then initialize firestore
        mLinearLayoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false);
        mAarsRecycler.setLayoutManager(mLinearLayoutManager);
        db = FirebaseFirestore.getInstance();
    }

    // this is called in the create method.
    private void getAars(){
        // grab the initial query of Firestore Collection "aars"
        mQuery = db.collection("aars");

        // this sets the opions of
        FirestoreRecyclerOptions<AAR> response = new FirestoreRecyclerOptions.Builder<AAR>()
                .setQuery(mQuery, AAR.class)
                .setLifecycleOwner(this)
                .build();

        // create new AarAdapter, taking in the Query, the Firestore options(response), and the listener for the onClick
        mAarAdapter = new AarAdapter(mQuery,response, this){

            // in case of an error, it lets the user know something happened instead of showing a blank box
            @Override
            public void onError(FirebaseFirestoreException e) {
                super.onError(e);
                Log.e("error",e.getMessage());
                emptyTextView.setText("Some error occured");
            }

            // this removes the empty text box, removes the
            @Override
            public void onDataChanged() {
                super.onDataChanged();

                //if the item count is 0 in the adapter, then it sets an empty text view, or vice versa
                if (getItemCount() == 0) {
                    mAarsRecycler.setVisibility(View.GONE);
                    emptyTextView.setVisibility(View.VISIBLE);
                } else{
                    mAarsRecycler.setVisibility(View.VISIBLE);
                    emptyTextView.setVisibility(View.GONE);
                    prograssBarView.setVisibility(View.GONE);
                }
            }
        };////////////////////

        mAarAdapter.notifyDataSetChanged();
        mAarsRecycler.setAdapter(mAarAdapter);
        
    }



    //can use Butterknife here to set up buttons for on click
    //@OnClick()
    public void onAarSelected(DocumentSnapshot aar) {

        aar.toObject(AAR.class);
        aar.getId();

        Toast.makeText(this,"this is the text" + aar.getId(),Toast.LENGTH_SHORT).show();

        // Go to the details page for the selected restaurant
        //Intent intent = new Intent(this, RestaurantDetailActivity.class);
        //intent.putExtra(RestaurantDetailActivity.KEY_RESTAURANT_ID, restaurant.getId());

        //startActivity(intent);
    }



}

