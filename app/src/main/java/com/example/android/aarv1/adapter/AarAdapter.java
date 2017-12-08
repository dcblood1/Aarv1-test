package com.example.android.aarv1.adapter;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.android.aarv1.R;
import com.example.android.aarv1.model.AAR;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Dillon on 12/4/2017.
 */


// might need to make a Firestore adapter with this?
public class AarAdapter extends FirestoreRecyclerAdapter<AarAdapter,AarAdapter.AARHolder> {


    /**
     * Create a new RecyclerView adapter that listens to a Firestore Query.  See
     * {@link FirestoreRecyclerOptions} for configuration options.
     *
     * @param options
     */
    public AarAdapter(FirestoreRecyclerOptions options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(AARHolder holder, int position, AarAdapter model) {

    }

    @Override
    public AARHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return null;
    }

    public class AARHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.aar_item_image)
        ImageView imageView;

        @BindView(R.id.aar_title)
        TextView titleTextView;

        @BindView(R.id.aar_category_name)
        TextView categoryTextView;

        @BindView(R.id.aar_recommendations)
        TextView recommendationsTextView;

        @BindView(R.id.up_votes)
        TextView upVotesTextView;

        @BindView(R.id.down_votes)
        TextView downVotesTextView;

        @BindView(R.id.up_vote_button)
        Button upVoteButton;

        @BindView(R.id.down_vote_button)
        Button downVoteButton;

        public AARHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

        }


        public void bind(AAR aar) {

            titleTextView.setText(aar.getTitle());
            categoryTextView.setText(aar.getCategory());
            recommendationsTextView.setText(aar.getRecommendations());


// this doesn't work right now becuase it is trying to set text with an int
//        upVotesTextView.setText(aar.getUpVotes());
//        downVotesTextView.setText(aar.getDownVotes());

            // Load aar image into imageView
            Glide.with(imageView.getContext())
                    .load(aar.getPhoto())
                    .into(imageView);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    Log.v("AARHolder.java", "you clicked here : " + position);
                }
            });

        }
    }
}
