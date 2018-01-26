package com.example.android.aarv1;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
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
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;

import butterknife.BindView;
import butterknife.ButterKnife;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link EditsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link EditsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class EditsFragment extends Fragment implements
            AarAdapter.OnAarSelectedListener{

    private static final String TAG = "EditsFragment";

    // Access a Cloud Firestore instance from your Activity
    private FirebaseFirestore db;
    private AarAdapter mAarAdapter;
    private DocumentReference mAarRef; // used for selecting an aar
    private FirebaseAuth mFirebaseAuth;

    // limits the amount of aars we get back
    private static final int LIMIT = 50;

    // create global for Query
    private Query mQuery;

    @BindView(R.id.edit_toolbar)
    Toolbar mEditToolbar;
    // Bind Views using Butterknife from activity_main.xml
    @BindView(R.id.edits_empty_text_view)
    TextView editsEmptyTextView;

    @BindView(R.id.recycler_aars_edit_frag)
    RecyclerView mEditsRecycler;

    @BindView(R.id.fab)
    FloatingActionButton mFAB;

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public EditsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment EditsFragment.
     */
    public static EditsFragment newInstance(String param1, String param2) {
        EditsFragment fragment = new EditsFragment();
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

        // Enable Firestore logging
        FirebaseFirestore.setLoggingEnabled(true);

        // initialize firebase authentication
        mFirebaseAuth = FirebaseAuth.getInstance();

        //initialize Firestore and main RecyclerView
        init();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_edits, container, false);
        mEditsRecycler = view.findViewById(R.id.recycler_aars_edit_frag);
        mEditsRecycler.setLayoutManager(new LinearLayoutManager(getActivity()));
        getAars();

        mFAB= view.findViewById(R.id.fab);
        mFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent editIntent = new Intent(getActivity(), EditorActivity.class);
                startActivity(editIntent);
            }
        });


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
    }

    @Override
    public void onAarSelected(DocumentSnapshot aar) {
        Log.v(TAG,"aar clicked on in EditsFragment");

        // Firestore method to convert snapshot to POJO
        aar.toObject(AAR.class);

        // Go to the details page of the selected aar, sending aar snapshow ID
        Intent editor_intent = new Intent(getActivity(), EditorActivity.class);
        editor_intent.putExtra(EditorActivity.KEY_AAR_ID, aar.getId());

        // start activity
        startActivity(editor_intent);

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

        // grab the initial query of Firestore Collection "aars"
        mQuery = db.collection("aars").whereEqualTo("user",mFirebaseAuth.getUid()).orderBy("timestamp", Query.Direction.DESCENDING);
        // if it doesn't show up for other users, let me know... me...

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
                editsEmptyTextView.setText("Add an AAR to index");
            }

            // this removes the empty text box
            @Override
            public void onDataChanged() {
                super.onDataChanged();

                Log.v(TAG,"this is the itemCount = " + getItemCount());

                //if the item count is 0 in the adapter, then it sets an empty text view, or vice versa
                if (getItemCount() == 0) {
                    mEditsRecycler.setVisibility(View.GONE);
                    editsEmptyTextView.setVisibility(View.VISIBLE);
                } else{
                    mEditsRecycler.setVisibility(View.VISIBLE);
                    editsEmptyTextView.setVisibility(View.GONE);
                }
            }
        };

        // notifyDataSetChanged is in a standard recyclerView, does it notify us if something changes?
        mAarAdapter.notifyDataSetChanged();

        mEditsRecycler.setLayoutManager(new LinearLayoutManager(getActivity()));

        // sets the RecyclerView to our current adapter.
        mEditsRecycler.setAdapter(mAarAdapter);
    }


}
