package com.example.android.aarv1;

import android.support.v4.app.Fragment;

import com.example.android.aarv1.adapter.AarAdapter;
import com.google.firebase.firestore.DocumentSnapshot;

/**
 * Created by Dillon on 1/1/2018.
 * Need to create a Fragment class for each of the bottom nav buttons
 */

public class AarFragment extends Fragment
                implements FilterDialogFragment.FilterListener, AarAdapter.OnAarSelectedListener{






    @Override
    public void onFilter(Filters filters) {

    }

    @Override
    public void onAarSelected(DocumentSnapshot aar) {

    }
}
