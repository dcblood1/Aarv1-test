package com.example.android.aarv1;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
import android.view.View;
import android.widget.TextView;

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
    private MainFragment mMainFragment;

    // limits the amount of aars we get back... want a limit?
    private static final int LIMIT = 50;

    // create global for Query
    private Query mQuery;

    private static final String TAG = "MainActivity";

    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    @BindView(R.id.bottom_nav_empty_text_view)
    TextView mEmptyTextView;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {


        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    mToolbar.setVisibility(View.VISIBLE);
                    switchToMainFragment();
                    return true;
                case R.id.navigation_edits:
                    switchToEditsFragment();
                    mToolbar.setVisibility(View.GONE);
                    return true;
                case R.id.navigation_saves:
                    switchToSavedFragment();
                    mToolbar.setVisibility(View.GONE);
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

        //see if we are connected to a network / wifi
        ConnectivityManager cm = (ConnectivityManager) BottomNavActivity.this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();

        // initialize firebase authentication
        mFirebaseAuth = FirebaseAuth.getInstance();
        // initialize firebase Firestore db
        db = FirebaseFirestore.getInstance();

        //this allows us to open up to the main fragment initially
        Log.v(TAG,"savedInstanceState = " + savedInstanceState);
        if (savedInstanceState == null) {
            FragmentManager manager = getSupportFragmentManager();
            manager.beginTransaction().replace(R.id.content, new MainFragment()).commit();
        }

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.bottom_navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        // View model
        // What do I need to do to update the view Model??
        mViewModel = ViewModelProviders.of(this).get(MainActivityViewModel.class);

        if (isConnected != true) {
            mEmptyTextView.setText(R.string.no_internet);
        }

        mMainFragment = new MainFragment();

        Log.v(TAG,"onCreate in BottomNavActivity called");
        // this is called once...

    }

    @Override
    protected void onStart() {
        super.onStart();

        // Start sign in if necessary
        if (shouldStartSignIn()) {
            startSignIn();
            return;
        }

        mUserProfileRef = db.collection("users").document(mFirebaseAuth.getCurrentUser().getUid());
        mUserProfileRegistration = mUserProfileRef.addSnapshotListener(this);

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
        //manager.beginTransaction().replace(R.id.content, new MainFragment()).commit();
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
    // how is this called??
    @Override
    public void onFilter(Filters filters) {

        Log.v(TAG,"onFilter in BottomNavActivity being called");

        // Save filters to mViewModel so data can be passed
        mViewModel.setFilters(filters);
        mViewModel.setSearchDescription(filters.getSearchDescription(this));
        mViewModel.setOrderDescription(filters.getOrderDescription(this));

        // This replaces the content placeholder with the new fragment created.
        FragmentManager manager = getSupportFragmentManager();

        // so this is ok to create a new one, you are updating everything...
        // the only place I don't want a new one is when I'm swapping back and nothing has changed.
        manager.beginTransaction().replace(R.id.content,new MainFragment()).commit();

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

            Log.v(TAG,"passing new Bundle of SavedFragment");

            // This will pass data to the fragment from this activity... what do I need to pass? The filters right??
            Bundle bundle = new Bundle();
            //bundle.putString("filters", String.valueOf(filters));
            //bundle.putString("filters", String.valueOf(mViewModel));
            // So you really are just passing all of the usable data to the fragment?
            // is it just allowing the viewmodel to connect or what??
            bundle.putString("user",mFirebaseAuth.getCurrentUser().getUid());

            // set Fragmentclass Arguments
            SavedFragment fragobj = new SavedFragment();
            fragobj.setArguments(bundle);

        } else {

            generateNewUserProfile();
            Log.v(TAG, "it does not exist");
            // Create user Profile
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
