package com.example.android.aarv1;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.android.aarv1.model.AAR;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Transaction;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Dillon on 12/9/2017.
 */

public class AarDetailActivity extends AppCompatActivity implements EventListener<DocumentSnapshot>{

    private static final String TAG = "AarDetailActivity";

    public static final String KEY_AAR_ID = "key_aar_id";

    @BindView(R.id.aar_image_view)
    ImageView mImageView;

    @BindView(R.id.title_text_view)
    TextView mTitleView;

    @BindView(R.id.category_text_view)
    TextView mCategoryTextView;

    @BindView(R.id.location_text_view)
    TextView mLocationTextView;

    @BindView(R.id.description_text_view)
    TextView mDescriptionTextView;

    @BindView(R.id.cause_text_view)
    TextView mCauseTextView;

    @BindView(R.id.recommendations_text_view)
    TextView mRecommendationsTextView;

    @BindView(R.id.time_view)
    TextView mTimeView;

    @BindView(R.id.up_votes_view)
    TextView mUpVotesTextView;

    @BindView(R.id.up_vote_button)
    Button mUpVoteButton;

    private FirebaseFirestore mFirestore;
    private DocumentReference mAarRef;
    private ListenerRegistration mAarRegistration;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aar_detail);
        ButterKnife.bind(this);

        // Get aar ID from extras
        String aarId = getIntent().getExtras().getString(KEY_AAR_ID);
        if (aarId == null) {
            throw new IllegalArgumentException("Must pass extra " + KEY_AAR_ID);
        }

        // Initialize Firestore
        mFirestore = FirebaseFirestore.getInstance();

        // Get reference to the aars
        mAarRef = mFirestore.collection("aars").document(aarId);

    }

    @Override
    protected void onStart() {
        super.onStart();

        // idk what this does
        mAarRegistration = mAarRef.addSnapshotListener(this);

    }

    @Override
    protected void onStop() {
        super.onStop();

        // I also dont know what this does
        if(mAarRegistration != null) {
            mAarRegistration.remove();
            mAarRegistration = null;
        }

    }

    // when the back button is pressed, it sends the user back to the previous page...
    @OnClick(R.id.aar_back_button)
    public void onBackArrowClicked(View view) {
        onBackPressed();
    }


    // supporting method for adding upVote to an AAR
    private Task<Void> addUpVote(final DocumentReference aarRef){

        return mFirestore.runTransaction(new Transaction.Function<Void>() {
            @Nullable
            @Override
            public Void apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {

                AAR aar = transaction.get(aarRef).toObject(AAR.class);

                // Compute new number of upVotes
                int newUpVotes = aar.getUpVotes()+ 1;

                // Set new restaurant info
                aar.setUpVotes(newUpVotes);

                // Commit to firestore
                transaction.set(aarRef,aar);

                return null;
            }
        });
    }

    // Adds an upvote
    @OnClick(R.id.up_vote_button)
    public void onUpVote(){
        // In a transaction, add the new rating and update the aggregate totals
        addUpVote(mAarRef)
                .addOnSuccessListener(this, new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG,"addUpVote added");
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG,"addUpVote Fail",e);

                        Snackbar.make(findViewById(android.R.id.content), "Failed to add vote",
                                Snackbar.LENGTH_SHORT).show();
                    }
                });
    }

    // when the user clicks on the aar from the main page, event occurs to show detail view.
    @Override
    public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
        if (e != null) {
            Log.w(TAG, "aar:onEvent", e);
            return;
        }

        onAarLoaded(documentSnapshot.toObject(AAR.class));
    }

    private void onAarLoaded(AAR aar) {
        mTitleView.setText(aar.getTitle());
        mCategoryTextView.setText(aar.getCategory());
        mLocationTextView.setText(aar.getLocation());
        mDescriptionTextView.setText(aar.getDescription());
        mCauseTextView.setText(aar.getCause());
        mRecommendationsTextView.setText(aar.getRecommendations());
        mUpVotesTextView.setText(getString(R.string.fmt_up_votes,aar.getUpVotes()));
        mTimeView.setText(getString(R.string.fmt_time,aar.getDate()));

        // Background Image
        if (aar.getPhoto() !=  null) {
            Glide.with(mImageView.getContext())
                    .load(aar.getPhoto())
                    .into(mImageView);
        } else {
            Glide.with(mImageView.getContext())
                    .load(R.drawable.food_1)
                    .into(mImageView);
        }
    }
}
