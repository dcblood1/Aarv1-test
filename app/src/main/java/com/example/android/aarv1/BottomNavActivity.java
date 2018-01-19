package com.example.android.aarv1;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.example.android.aarv1.model.UserProfile;
import com.example.android.aarv1.viewmodel.MainActivityViewModel;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


public class BottomNavActivity extends AppCompatActivity implements
        MainFragment.OnFragmentInteractionListener,
        EditsFragment.OnFragmentInteractionListener,
        SavedFragment.OnFragmentInteractionListener,
        FilterDialogFragment.FilterListener, EventListener<DocumentSnapshot> {

    // RC (Request code) for user Sign in
    public static final int RC_SIGN_IN = 1;

    // Access a Cloud Firestore instance from your Activity
    private FirebaseFirestore db;
    private MainActivityViewModel mViewModel;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseFirestore mFirestore;
    private DocumentReference mUserProfileRef;
    private ListenerRegistration mUserProfileRegistration; // for onEvent

    // limits the amount of aars we get back... want a limit?
    private static final int LIMIT = 50;

    // create global for Query
    private Query mQuery;

    private static final String TAG = "MainActivity";

    @BindView(R.id.toolbar)
    Toolbar mToolbar;

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
        setSupportActionBar(mToolbar);

        // initialize firebase authentication
        mFirebaseAuth = FirebaseAuth.getInstance();
        // initialize firebase Firestore db
        db = FirebaseFirestore.getInstance();

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

        //mUserProfileRef = db.collection("users").document("empty_user");
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Start sign in if necessary
        if (shouldStartSignIn()) {
            startSignIn();
            return;
        }

        // this is a listener for the firestore to know to be accessed? essentially?
        //mUserProfileRegistration = mUserProfileRef.addSnapshotListener(this);

    }

    @Override
    protected void onStop() {
        super.onStop();

        // removes listener for onEvent I believe
        if(mUserProfileRegistration!= null) {
            mUserProfileRegistration.remove();
            mUserProfileRegistration = null;
        }
    }



    // Used for Signing in
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            mViewModel.setIsSigningIn(false);

            if (resultCode != RESULT_OK && shouldStartSignIn()) {
                startSignIn();
            }

            mUserProfileRef = db.collection("users").document(mFirebaseAuth.getCurrentUser().getUid());
            mUserProfileRegistration = mUserProfileRef.addSnapshotListener(this);
        }
    }

    private boolean shouldStartSignIn() {
        return (!mViewModel.getIsSigningIn() && FirebaseAuth.getInstance().getCurrentUser() == null);
    }

    private void startSignIn() {
        // Sign in with FirebaseUI

        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build(),
                new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build()
        );

        startActivityForResult(AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setIsSmartLockEnabled(false)
                        .setAvailableProviders(providers)
                        .build(),
                RC_SIGN_IN);

        mViewModel.setIsSigningIn(true);

        // get the current user... here???
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_sign_out:
                AuthUI.getInstance().signOut(this);
                startSignIn();
                break;
        }
        return super.onOptionsItemSelected(item);
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

    // Allows the user to select filters to display the correct data, used in MainFragment and ViewModel.
    // Dont necessarily understand how that all works...
    @Override
    public void onFilter(Filters filters) {

        // Construct query basic query
        Query query = db.collection("aars");

        //Category (equality filter)
        if (filters.hasCategory()){
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
        mViewModel.setFilters(filters);

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

        // set Fragmentclass Arguments
        MainFragment fragobj = new MainFragment();
        fragobj.setArguments(bundle);

        // This replaces the content placeholder with the new fragment created.
        FragmentManager manager = getSupportFragmentManager();
        manager.beginTransaction().replace(R.id.content, fragobj).commit();

    }

    @Override
    public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
        if (e != null) {
            Log.w(TAG, "aar:onEvent", e);
            return;
        }
        if (documentSnapshot.exists()) {
            Log.v(TAG,"it does indeed exist");
            //onAarLoaded(documentSnapshot.toObject(AAR.class));
            documentSnapshot.toObject(UserProfile.class);
            // idk if this will actually work...
        } else {

            generateNewUserProfile();
            Log.v(TAG, "it does not exist");
            // so we need to creaet it... here??
        }

    }

    protected void generateNewUserProfile(){
        // what do you want to do here
        // input is the users info
        // output is a generated POJO of UserProfile that I return?? nah... just created.

        // get the current user ID and adds it to the AAR POJO, has to have it...
        final String currentUserId = mFirebaseAuth.getUid();
        mFirebaseAuth.getCurrentUser();
        // it is not created yet...
        Log.v(TAG,"CurrentUserId" + currentUserId);

        Date hasTimeStamp= Calendar.getInstance().getTime();

        mUserProfileRef = db.collection("users").document(currentUserId);

        UserProfile userProfile = new UserProfile();
        userProfile.setUserId(mFirebaseAuth.getCurrentUser().getUid());
        userProfile.setUserName(mFirebaseAuth.getCurrentUser().getDisplayName());
        userProfile.setUserEmail(mFirebaseAuth.getCurrentUser().getEmail());
        userProfile.setTimestamp(hasTimeStamp);

        ArrayList<String> empty_list = new ArrayList<String>();
        userProfile.setListUpVotes(empty_list);

        mUserProfileRef.set(userProfile)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.v(TAG,"new User added");
                    }
                }). addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.v(TAG,"Unable to add new User");
            }
        });

    }
}
