package com.example.android.aarv1;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.android.aarv1.adapter.AarAdapter;
import com.example.android.aarv1.model.AAR;
import com.example.android.aarv1.viewmodel.MainActivityViewModel;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.Transaction;

import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity
        implements FilterDialogFragment.FilterListener,
        AarAdapter.OnAarSelectedListener,
        MainFragment.OnFragmentInteractionListener,
        EditsFragment.OnFragmentInteractionListener,
        SavedFragment.OnFragmentInteractionListener {

    private static final String TAG = "MainActivity";

    // RC (Request code) for user Sign in
    public static final int RC_SIGN_IN = 1;

    // Access a Cloud Firestore instance from your Activity
    private FirebaseFirestore db;
    private AarAdapter mAarAdapter; // the testing aar
    private FilterDialogFragment mFilterDialog;
    private MainActivityViewModel mViewModel;
    private DocumentReference mAarRef;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;


    // limits the amount of aars we get back... want a limit?
    private static final int LIMIT = 50;

    // create global for Query
    private Query mQuery;

    // Bind Views using Butterknife from activity_main.xml
    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    @BindView(R.id.text_current_search)
    TextView mCurrentSearchView;

    @BindView(R.id.text_current_sort_by)
    TextView mCurrentSortByView;

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
        setSupportActionBar(mToolbar);

        // initialize firebase authentication
        mFirebaseAuth = FirebaseAuth.getInstance();

        // View model
        mViewModel = ViewModelProviders.of(this).get(MainActivityViewModel.class);

        // Enable Firestore logging
        FirebaseFirestore.setLoggingEnabled(true);

        //initialize Firestore and main RecyclerView
        init();
        getAars();

        // Filter Dialog
        mFilterDialog = new FilterDialogFragment();
        Log.v(TAG,"creating new FilterDialogFragment in MainActivity");

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

    // Initiate the Firestore db, as well as an initial query
    protected void init(){
        db = FirebaseFirestore.getInstance();

        // grab the initial query of Firestore Collection "aars"
        mQuery = db.collection("aars").orderBy("date", Query.Direction.DESCENDING);
    }

    // Initializes the Firestore adapter with the appropriate options
    private void getAars(){

        // this sets the opions of the adapter
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

            // this removes the empty text box
            @Override
            public void onDataChanged() {
                super.onDataChanged();

                Log.v(TAG,"this is the itemCount = " + getItemCount());

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

        // notifyDataSetChanged is in a standard recyclerView, does it notify us if something changes?
        mAarAdapter.notifyDataSetChanged();

        mAarsRecycler.setLayoutManager(new LinearLayoutManager(this));

        // sets the RecyclerView to our current adapter.
        mAarsRecycler.setAdapter(mAarAdapter);

    }

    @Override
    protected void onStart() {
        super.onStart();

        // Start sign in if necessary
        if (shouldStartSignIn()) {
            startSignIn();
            return;
        }

        // Apply filters, for the first time correct?
        onFilter(mViewModel.getFilters());
    }

    // supporting method for adding a view to an AAR when the user clicks on it...
    private Task<Void> addView(final DocumentReference aarRef){
        Log.v(TAG,"addView running now" + aarRef);

        return db.runTransaction(new Transaction.Function<Void>() {
            @Nullable
            @Override
            public Void apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {

                AAR aar = transaction.get(aarRef).toObject(AAR.class);

                // Compute new number of upVotes
                int newViews = aar.getViews() + 1;
                Log.v(TAG,"adding newViews = " + newViews);

                // Set new restaurant info
                aar.setViews(newViews);

                // Commit to firestore
                transaction.set(aarRef,aar);

                return null;
            }
        });
    }


    // if the aar is selected, returns the appropriate data. adds a view to the AAR transaction... on day
    public void onAarSelected(DocumentSnapshot aar) {

        // Firestore method to convert snapshot to POJO
        aar.toObject(AAR.class);

        // Go to the details page of the selected aar, sending aar snapshow ID
        Intent detail_intent = new Intent(this, AarDetailActivity.class);
        detail_intent.putExtra(AarDetailActivity.KEY_AAR_ID, aar.getId());

        // Get reference to the aars
        mAarRef = db.collection("aars").document(aar.getId());

        // add view to the AAR
        addView(mAarRef);

        startActivity(detail_intent);
    }


    // for when the filter button is clicked on, pull up the fragment
    @OnClick(R.id.filter_bar)
    public void onFilterClicked() {
        // Show the dialog containing filter options
        mFilterDialog.show(getSupportFragmentManager(), FilterDialogFragment.TAG);

    }

    // when user clicks on cancel button, clears filters and sets them to default
    @OnClick(R.id.button_clear_filter)
    public void onClearFilter(){
        mFilterDialog.resetFilters();

        onFilter(Filters.getDefault());
    }


    // Allows the user to select filters to display the correct data
    @Override
    public void onFilter(Filters filters) {

        // Construct query basic query
        Query query = db.collection("aars");
        Log.v(TAG,"this is the first query in onFilter" + query);

         //Category (equality filter)
        if (filters.hasCategory()){
            query = query.whereEqualTo("category", filters.getCategory());
        }

        // Location (equality filters) // it also doesn't like the location filter...
        if (filters.hasLocation()) {
            query = query.whereEqualTo("location", filters.getLocation());
        }

         //Sort by (orderBy with direction), it doesn't like the sort BY!... IDK WHY.
        if (filters.hasSortBy()) {
            Log.v(TAG,"filters.hasSortBy " + filters.hasSortBy());
            query = query.orderBy(filters.getSortBy(), filters.getSortDirection());
             //if I wanted to hard code it...
            Log.v(TAG,"query in hasSortBy " + query);
        }

        // Limit items
        query = query.limit(LIMIT);

        // Update the query
        mQuery = query;

        // sets the recyclerView and options
        getAars();

        // Set header
        mCurrentSearchView.setText(Html.fromHtml(filters.getSearchDescription(this)));
        mCurrentSortByView.setText(filters.getOrderDescription(this));

        // Save filters
        mViewModel.setFilters(filters);
        // this is what calls the filters.

    }

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


    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}

