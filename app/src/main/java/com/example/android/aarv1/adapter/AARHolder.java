package com.example.android.aarv1.adapter;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.android.aarv1.R;
import com.example.android.aarv1.model.AAR;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Dillon on 12/5/2017.
 * RecyclerView Holder
 */

public class AARHolder extends RecyclerView.ViewHolder{

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


    // Removed buttons, because I dont want them cluttering up the MainActivity View
    //@BindView(R.id.up_vote_button)
    //Button upVoteButton;

    //@BindView(R.id.down_vote_button)
    //Button downVoteButton;

    public AARHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);

    }

    // don't know if this is the correct thing to do... using AAR... but we'll dam sure try
    // dont think I need to set the button on here... but not sure honestly
    public void bind (AAR aar){

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
                long itemId = getItemId();
                Log.v("AARHolder.java","you clicked here : " + position);
                Log.v("AARHolder.java","this is the itemID : " + itemId);

            }
        });

    }

    //an abstract is needed for the bindView
    public void onClick(Object item) {
        // an abstract is needed for the bindView in MainActivity
    }

}
