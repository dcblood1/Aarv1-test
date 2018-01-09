package com.example.android.aarv1;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.android.aarv1.adapter.AarAdapter;
import com.example.android.aarv1.model.AAR;
import com.example.android.aarv1.viewmodel.MainActivityViewModel;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.Transaction;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MainFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MainFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MainFragment extends Fragment implements
        FilterDialogFragment.FilterListener, AarAdapter.OnAarSelectedListener{

    private static final String TAG = "MainFragment";

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

    @BindView(R.id.recycler_aars_frag)
    RecyclerView mAarsRecycler;

    @BindView(R.id.empty_text_view)
    TextView emptyTextView;

    @BindView(R.id.progress_bar_view)
    ProgressBar prograssBarView;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public MainFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MainFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MainFragment newInstance(String param1, String param2) {
        MainFragment fragment = new MainFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        // idk why this wont work... Do I need it?
        //setSupportActionBar(mToolbar);

        // initialize firebase authentication
        mFirebaseAuth = FirebaseAuth.getInstance();

        // View model
        // What do I need to do to update the view Model??
        mViewModel = ViewModelProviders.of(this).get(MainActivityViewModel.class);

        // Enable Firestore logging
        FirebaseFirestore.setLoggingEnabled(true);

        //initialize Firestore and main RecyclerView
        //init();
        //getAars();

        // Filter Dialog
        mFilterDialog = new FilterDialogFragment();
        //mFilterDialog.getActivity();
        //mFilterDialog.instantiate(getActivity(),FilterDialogFragment.TAG);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.fragment_main, container, false);
        // I think this is how I need to use it??
        mAarsRecycler = view.findViewById(R.id.recycler_aars_frag);
        mAarsRecycler.setLayoutManager(new LinearLayoutManager(getActivity()));
        init();
        getAars();

        //mFilterDialog = new FilterDialogFragment();

        ButterKnife.bind(this,view);
        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    // Initiate the Firestore db, as well as an initial query
    protected void init(){
        db = FirebaseFirestore.getInstance();

        // grab the initial query of Firestore Collection "aars"
        mQuery = db.collection("aars").orderBy("date", Query.Direction.DESCENDING);
        Log.v(TAG,"this is the mquery in the MainFragment");
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
        Log.v(TAG,"This is mAarAdapter=" + mAarAdapter);
        mAarAdapter.notifyDataSetChanged();

        Log.v(TAG,"This is mAarsRecycler=" + mAarsRecycler);
        //mAarsRecycler.setLayoutManager(new LinearLayoutManager(this));
        mAarsRecycler.setLayoutManager(new LinearLayoutManager(getActivity()));

        // sets the RecyclerView to our current adapter.
        mAarsRecycler.setAdapter(mAarAdapter);
    }

    @Override
    public void onStart() {
        super.onStart();

        // Apply filters
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
        Intent detail_intent = new Intent(getActivity(), AarDetailActivity.class);
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
        Log.v(TAG,"onFilterClicked now");

        // These both work... idk which one is correct to use.
        mFilterDialog.show(getChildFragmentManager(), FilterDialogFragment.TAG);
        //mFilterDialog.show(getActivity().getSupportFragmentManager(), FilterDialogFragment.TAG);
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
        Log.v(TAG,"getAars in onFilter about to be called");
        getAars();

    // Set header
        mCurrentSearchView.setText(Html.fromHtml(filters.getSearchDescription(getActivity())));
        mCurrentSortByView.setText(filters.getOrderDescription(getActivity()));

    // Save filters
        Log.v(TAG,"this is the mViewModel" + mViewModel);
        mViewModel.setFilters(filters);
        Log.v(TAG,"this is the mViewModel" + mViewModel);

    }


    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }


}
