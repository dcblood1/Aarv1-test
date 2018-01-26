package com.example.android.aarv1;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.android.aarv1.adapter.AarAdapter;
import com.example.android.aarv1.model.AAR;
import com.example.android.aarv1.model.UserProfile;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import butterknife.BindView;
import butterknife.ButterKnife;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SavedFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link SavedFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SavedFragment extends Fragment implements AarAdapter.OnAarSelectedListener, EventListener<DocumentSnapshot> {

    private static final String TAG = "SavedFragment";

    // Access a Cloud Firestore instance from your Activity
    private FirebaseFirestore db;
    private AarAdapter mAarAdapter;
    private DocumentReference mAarRef; // used for selecting an aar
    private DocumentReference mUserProfileRef;
    private FirebaseAuth mFirebaseAuth;
    private ListenerRegistration mUserRegistration;

    // limits the amount of aars we get back
    private static final int LIMIT = 50;

    // create global for Query
    private Query mQuery;

    // Bind Views using Butterknife from activity_main.xml
    @BindView(R.id.saved_empty_text_view)
    TextView mSavedEmptyTextView;

    @BindView(R.id.recycler_aars_saved_frag)
    RecyclerView mSavedRecycler;

    @BindView(R.id.up_vote_toolbar)
    Toolbar mUpVoteToolBar;

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "user";

    public String mUser;

    private OnFragmentInteractionListener mListener;

    public SavedFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param user Parameter 1.
     * @return A new instance of fragment SavedFragment.
     */
    public static SavedFragment newInstance(String user) {
        SavedFragment fragment = new SavedFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, user);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v(TAG,"about to run getArguments()" + getArguments());

        if (getArguments() != null) {
            mUser = getArguments().getString(ARG_PARAM1);
            Log.v(TAG,"this is mUser in SavedFragment" + mUser);
        }

        // Enable Firestore logging
        FirebaseFirestore.setLoggingEnabled(true);

        // initialize firebase authentication
        mFirebaseAuth = FirebaseAuth.getInstance();

        //mUserProfileRef = db.collection("users").document("QeZbG2U2ySaP1b2PWZHj3eE3v9r2");

        Log.v(TAG,"mFirebaseAuth.getCurrentUser()" + mFirebaseAuth.getCurrentUser().getDisplayName());

        //Log.v(TAG,"mUserProfileRef" + mUserProfileRef);

        //initialize Firestore and main RecyclerView
        init();

        mUserProfileRef = db.collection("users").document(mFirebaseAuth.getCurrentUser().getUid());

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_saved, container, false);
        mSavedRecycler = view.findViewById(R.id.recycler_aars_saved_frag);
        mSavedRecycler.setLayoutManager(new LinearLayoutManager(getActivity()));
        getAars();

        ButterKnife.bind(this,view);
        return view;

    }

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
        // triggers the onEvent
        if(mUserRegistration!= null) {
            mUserRegistration.remove();
            mUserRegistration = null;
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        Log.v(TAG,"mUserProfile Ref = " + mUserProfileRef);

        // adds listener that triggers onEvent Can I do this for fragments?
        mUserRegistration = mUserProfileRef.addSnapshotListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();

        Log.v(TAG,"mUserProfile Ref = " + mUserProfileRef);

        // adds listener that triggers onEvent Can I do this for fragments?
        //mUserRegistration = mUserProfileRef.addSnapshotListener(this);
    }

    @Override
    public void onStop() {
        super.onStop();

        // triggers the onEvent
        if(mUserRegistration!= null) {
            mUserRegistration.remove();
            mUserRegistration = null;
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        // triggers the onEvent
        //if(mUserRegistration!= null) {
        //    mUserRegistration.remove();
        //    mUserRegistration = null;
        //}
    }

    @Override
    public void onAarSelected(DocumentSnapshot aar) {

        // Firestore method to convert snapshot to POJO
        aar.toObject(AAR.class);

        // Go to the details page of the selected aar, sending aar snapshow ID
        Intent detail_intent = new Intent(getActivity(), AarDetailActivity.class);
        detail_intent.putExtra(AarDetailActivity.KEY_AAR_ID, aar.getId());

        // need to call
        startActivity(detail_intent);
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
        void onFragmentInteraction(Uri uri);
    }

    // Initiate the Firestore db, as well as an initial query
    protected void init(){
        db = FirebaseFirestore.getInstance();

        mQuery = db.collection("aars").whereEqualTo("userUpVotes." + getUid(),true).orderBy("timestamp", Query.Direction.DESCENDING);

    }

    // Initializes the Firestore adapter with the appropriate options
    // Might need to change this up due to the saves
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
                mSavedEmptyTextView.setText("No Up Voted AAR's in index");
            }

            // this removes the empty text box
            @Override
            public void onDataChanged() {
                super.onDataChanged();

                Log.v(TAG,"this is the itemCount in savedFragment = " + getItemCount());

                //if the item count is 0 in the adapter, then it sets an empty text view, or vice versa
                if (getItemCount() == 0) {
                    mSavedRecycler.setVisibility(View.GONE);
                    mSavedEmptyTextView.setVisibility(View.VISIBLE);
                } else{
                    mSavedRecycler.setVisibility(View.VISIBLE);
                    mSavedEmptyTextView.setVisibility(View.GONE);
                    //prograssBarView.setVisibility(View.GONE);
                }
            }
        };

        // notifyDataSetChanged is in a standard recyclerView, does it notify us if something changes?
        mAarAdapter.notifyDataSetChanged();

        mSavedRecycler.setLayoutManager(new LinearLayoutManager(getActivity()));

        // sets the RecyclerView to our current adapter.
        mSavedRecycler.setAdapter(mAarAdapter);
    }



    // Need to do the onEvent Listener.
    // Then I can get all of the userProfile list of likes...
    // save that list to a

    @Override
    public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
        if (e != null) {
            Log.w(TAG, "aar:onEvent", e);
            return;
        }
        //onAarLoaded(documentSnapshot.toObject(AAR.class));
        userSavedAars(documentSnapshot.toObject(UserProfile.class));

    }

    // what were you trying to do with this? // Need to get all of the users liked aars here... then pass it into a query...
    public void userSavedAars(UserProfile userProfile) {


        //userProfile.getListUpVotes();
        //Log.v(TAG,"This is the list of upVotes" + userProfile.getListUpVotes());
        //Log.v(TAG,"This is .get(0)" + userProfile.getListUpVotes().get(0));


    }

    public String getUid(){
        return mFirebaseAuth.getCurrentUser().getUid();
    }

}
