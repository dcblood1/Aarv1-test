package com.example.android.aarv1;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.android.aarv1.adapter.AARHolder;
import com.example.android.aarv1.model.AAR;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    // Access a Cloud Firestore instance from your Activity
    private FirebaseFirestore db;
    private FirestoreRecyclerAdapter adapter;
    LinearLayoutManager mLinearLayoutManager;


    static {
        //helps me see what the f is going on maybe
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

    private void getAars(){
        mQuery = db.collection("aars");

        FirestoreRecyclerOptions<AAR> response = new FirestoreRecyclerOptions.Builder<AAR>()
                .setQuery(mQuery, AAR.class)
                .setLifecycleOwner(this)
                .build();

        adapter = new FirestoreRecyclerAdapter<AAR, AARHolder>(response) {
            @Override
            protected void onBindViewHolder(AARHolder holder, int position, AAR model) {
                holder.bind(model);

                Log.v(TAG,"adapter.getItem(position) : " + adapter.getItem(position));
                Log.v(TAG,"holder.getAdapterPosition(): " + holder.getAdapterPosition());

                holder.onClick(adapter.getItem(position));


            }

            @Override
            public AARHolder onCreateViewHolder(ViewGroup group, int viewType) {
                View view = LayoutInflater.from(group.getContext())
                        .inflate(R.layout.aar_item, group, false);

                return new AARHolder(view);
            }

            @Override
            public void onError(FirebaseFirestoreException e) {
                Log.e("error",e.getMessage());
                emptyTextView.setText("Some error occured");
            }

            @Override
            public void onDataChanged() {
                super.onDataChanged();

                if (getItemCount() == 0) {
                    mAarsRecycler.setVisibility(View.GONE);
                    emptyTextView.setVisibility(View.VISIBLE);
                } else{
                    mAarsRecycler.setVisibility(View.VISIBLE);
                    emptyTextView.setVisibility(View.GONE);
                    prograssBarView.setVisibility(View.GONE);
                }
            }
        };

        adapter.notifyDataSetChanged();
        mAarsRecycler.setAdapter(adapter);
    }

    //can use Butterknife here to set up buttons for on click
    //@OnClick()




}

