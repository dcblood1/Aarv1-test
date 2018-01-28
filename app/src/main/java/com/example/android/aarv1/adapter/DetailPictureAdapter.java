package com.example.android.aarv1.adapter;

import com.google.firebase.firestore.DocumentSnapshot;

/**
 * Created by Dillon on 1/26/2018.
 */

// Do I need an adapter for all of these?
// want to set it up so I can go through multiple pictures...

public class DetailPictureAdapter {

    // this method is called in the selected activity
    public interface OnPictureSelectedListener {
        void onPictureSelected(DocumentSnapshot aar);
    }


}
