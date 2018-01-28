package com.example.android.aarv1;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Dillon on 1/26/2018.
 */

public class DetailPictureActivity extends AppCompatActivity {

    private static final String TAG = "DetailPictureAcitivity";

    public static final String KEY_AAR_ID = "key_aar_id";

    private String mAarId;

    @BindView(R.id.detail_image_view)
    ImageView mDetailImageView;

    @BindView(R.id.image_progress_bar_detail)
    ProgressBar mProgressBar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_picture);
        ButterKnife.bind(this);

        Intent callingActivityIntent = getIntent();
        if (callingActivityIntent!= null){
            Uri imageUri = callingActivityIntent.getData();
            if (imageUri != null && mDetailImageView != null){
                Glide.with(this)
                        .load(imageUri)
                        .listener(new RequestListener<Uri, GlideDrawable>() {
                            @Override
                            public boolean onException(Exception e, Uri model, Target<GlideDrawable> target, boolean isFirstResource) {
                                // set a progress bar
                                mProgressBar.setVisibility(View.GONE);
                                return false;

                            }

                            @Override
                            public boolean onResourceReady(GlideDrawable resource, Uri model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                                // set a progress bar
                                mProgressBar.setVisibility(View.GONE);
                                return false;
                            }
                        })
                        .into(mDetailImageView);
            }


            Log.v(TAG,"imageUri in DetailPicture = " + imageUri);
            Log.v(TAG,"mDetailImageView = " + mDetailImageView);
            // does it not know what to do with this??
            // passing in the correct data... but doesnt do anything with it...
        }
    }
}
