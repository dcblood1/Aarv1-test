package com.example.android.aarv1;

import android.arch.lifecycle.ViewModelProviders;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;

import com.example.android.aarv1.adapter.AarAdapter;
import com.example.android.aarv1.viewmodel.MainActivityViewModel;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import butterknife.ButterKnife;

public class BottomNavActivity extends AppCompatActivity implements
        MainFragment.OnFragmentInteractionListener,
        EditsFragment.OnFragmentInteractionListener,
        SavedFragment.OnFragmentInteractionListener,
        FilterDialogFragment.FilterListener {

    // Access a Cloud Firestore instance from your Activity
    private FirebaseFirestore db;
    private MainActivityViewModel mViewModel;
    private AarAdapter mAarAdapter;
    private DocumentReference mAarRef;

    // limits the amount of aars we get back... want a limit?
    private static final int LIMIT = 50;

    // create global for Query
    private Query mQuery;

    private static final String TAG = "MainActivity";

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {


        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    switchToMainFragment();
                    return true;
                case R.id.navigation_edits:
                    switchToEditsFragment();
                    return true;
                case R.id.navigation_saves:
                    switchToSavedFragment();
                    return true;
            }
            return false;
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bottom_nav);
        ButterKnife.bind(this);

        //this allows us to open up to the main fragment initially
        if (savedInstanceState == null) {
            FragmentManager manager = getSupportFragmentManager();
            manager.beginTransaction().replace(R.id.content, new MainFragment()).commit();
        }

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.bottom_navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        // View model
        // What do I need to do to update the view Model??
        mViewModel = ViewModelProviders.of(this).get(MainActivityViewModel.class);

        db = FirebaseFirestore.getInstance();
    }


    // function called to switch to main fragment
    public void switchToMainFragment() {
        FragmentManager manager = getSupportFragmentManager();
        manager.beginTransaction().replace(R.id.content, new MainFragment()).commit();
    }

    // function called to switch to edits fragment
    public void switchToEditsFragment() {
        FragmentManager manager = getSupportFragmentManager();
        manager.beginTransaction().replace(R.id.content, new EditsFragment()).commit();
    }

    // function called to switch to saved fragment
    public void switchToSavedFragment() {
        FragmentManager manager = getSupportFragmentManager();
        manager.beginTransaction().replace(R.id.content, new SavedFragment()).commit();
    }

    // This controls how fragments can communicate with each other.
    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    // Allows the user to select filters to display the correct data
    @Override
    public void onFilter(Filters filters) {
        Log.v(TAG,"This is the onFilter in BottomNavActivity");

        // Construct query basic query
        Query query = db.collection("aars");
        Log.v(TAG,"this is the first query in onFilter in BottomNavActivity" + query);

        //Category (equality filter)
        if (filters.hasCategory()){
            Log.v(TAG,"category in bottomNav" + filters.getCategory());
            query = query.whereEqualTo("category", filters.getCategory());
        }

        // Location (equality filters)
        if (filters.hasLocation()) {
            query = query.whereEqualTo("location", filters.getLocation());
        }

        //Sort by (orderBy with direction)
        if (filters.hasSortBy()) {
            query = query.orderBy(filters.getSortBy(), filters.getSortDirection());
        }

        // Limit items
        query = query.limit(LIMIT);

        // Update the query
        mQuery = query;

        // Save filters to mViewModel so data can be passed
        Log.v(TAG,"this is the mViewModel prior to setFilters" + mViewModel);
        mViewModel.setFilters(filters);
        Log.v(TAG,"these are the filters= " + filters);
        Log.v(TAG,"this is the mViewModel after setFilters" + mViewModel);

        // This will pass data to the fragment from this activity... what do I need to pass? The filters right??
        Bundle bundle = new Bundle();
        //bundle.putString("filters", String.valueOf(filters));
        //bundle.putString("filters", String.valueOf(mViewModel));
        // So you really are just passing all of the usable data to the fragment?
        // is it just allowing the viewmodel to connect or what??
        bundle.putString("filters","cats");
        bundle.putString("category",filters.getCategory());
        bundle.putString("location",filters.getLocation());
        bundle.putString("sortBy", filters.getOrderDescription(this));

        Log.v(TAG,"this is the getOrderDescription" + filters.getOrderDescription(this));

        //filters.getOrderDescription()
        // set Fragmentclass Arguments
        MainFragment fragobj = new MainFragment();
        fragobj.setArguments(bundle);

        Log.v(TAG,"this is the fragobj being passed" + fragobj);
        Log.v(TAG,"this is the bundle being passed" + bundle);

        // This replaces the content placeholder with the new fragment created.
        FragmentManager manager = getSupportFragmentManager();
        manager.beginTransaction().replace(R.id.content, fragobj).commit();

    }

}
