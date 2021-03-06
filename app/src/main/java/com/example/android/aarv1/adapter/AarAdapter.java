package com.example.android.aarv1.adapter;

import android.content.res.Resources;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.android.aarv1.R;
import com.example.android.aarv1.model.AAR;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Dillon on 12/4/2017.
 */

// RecyclerView Adapter for a list of aar's
public class AarAdapter extends FirestoreRecyclerAdapter<AarAdapter,AarAdapter.AARHolder> {

    // this method is called in the selected activity
    public interface OnAarSelectedListener {
        void onAarSelected(DocumentSnapshot aar);
    }

    // creates a listener for the onclick
    private OnAarSelectedListener mListener;


    /**
     * Create a new RecyclerView adapter that listens to a Firestore Query.  See
     * {@link FirestoreRecyclerOptions} for configuration options.
     *
     * @param query
     * @param options
     * @param listener
     */
    public AarAdapter(Query query, FirestoreRecyclerOptions options, OnAarSelectedListener listener) {
        super(options);
        mListener = listener; // needed for the onclick listener

    }

    @Override
    protected void onBindViewHolder(AARHolder holder, int position, AarAdapter model) {
        holder.bind(model.getSnapshots().getSnapshot(position), mListener);

    }

    @Override
    public void onBindViewHolder(AARHolder holder, int position) {
//        super.onBindViewHolder(holder, position);
        holder.bind(getSnapshots().getSnapshot(position), mListener);
    }

    @Override
    public AARHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.aar_item, parent, false);

        return new AARHolder(view);
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

        @BindView(R.id.up_vote_text)
        TextView upVotesText;

        @BindView(R.id.date_text)
        TextView dateText;

        @BindView(R.id.aar_item_progress_bar)
        ProgressBar mItemProgressBar;


        public AARHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        //public void bind(AAR aar) {
        public void bind(final DocumentSnapshot snapshot,
                         final OnAarSelectedListener listener) {


            AAR aar = snapshot.toObject(AAR.class);
            Resources resources = itemView.getResources();

            titleTextView.setText(aar.getTitle());
            categoryTextView.setText(aar.getCategory());
            recommendationsTextView.setText(aar.getRecommendations());
            upVotesTextView.setText(resources.getString(R.string.fmt_up_votes,aar.getUpVotes()));
            dateText.setText(resources.getString(R.string.fmt_date, aar.getDate()));



            // Load image into adapter, otherwise load default rig photo
            if (aar.getPhoto() !=  null) {
                Glide.with(imageView.getContext())
                        .load(aar.getPhoto())
                        .listener(new RequestListener<String, GlideDrawable>() {
                            @Override
                            public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                                mItemProgressBar.setVisibility(View.GONE);
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                                mItemProgressBar.setVisibility(View.GONE);
                                return false;
                            }
                        })
                        .into(imageView);
            } else {
                Glide.with(imageView.getContext())
                        .load(R.drawable.rig_pic_501)
                        .into(imageView);
            }

            // Click on item to bring up aar activity detail
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        listener.onAarSelected(snapshot);
                    }
                }
            });
        }

    }
}
