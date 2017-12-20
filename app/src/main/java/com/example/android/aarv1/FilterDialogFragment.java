package com.example.android.aarv1;

/**
 * Created by Dillon on 12/15/2017.
 */

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Spinner;

import com.example.android.aarv1.model.AAR;
import com.google.firebase.firestore.Query;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Dialog Fragment containing filter form.
 */

public class FilterDialogFragment extends DialogFragment {

    public static final String TAG = "FilterDialog";

    interface FilterListener {

        void onFilter(Filters filters);

    }

    private View mRootView;

    @BindView(R.id.spinner_category)
    Spinner mCategorySpinner;

    @BindView(R.id.spinner_location)
    Spinner mLocationSpinner;

    @BindView(R.id.spinner_sort)
    Spinner mSortSpinner;

    @BindView(R.id.search_function_view)
    EditText mSearchView;

    // declares the interface we set up above.
    private FilterListener mFilterListener;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        mRootView = inflater.inflate(R.layout.dialog_filters, container, false);
        ButterKnife.bind(this, mRootView);

        return mRootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof FilterListener) {
            mFilterListener = (FilterListener) context;
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        // sets the dimensions of the filter fragment
        getDialog().getWindow().setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    // applies the filter changes when user clicks the apply button
    @OnClick(R.id.button_search)
    public void onSearchClicked() {
        Log.v(TAG,"mFilterListener = " + mFilterListener);
        if (mFilterListener != null) {
            mFilterListener.onFilter(getFilters());
        }

        dismiss();
    }

    // closes the menu when the user presses the cancel button
    @OnClick(R.id.button_cancel)
    public void onCancelClicked() {
        dismiss();
    }

    //
    @Nullable
    private String getSelectedCategory() {
        String selected = (String) mCategorySpinner.getSelectedItem();
        if (getString(R.string.value_any_category).equals(selected)) {
            return null;
        } else {
            return selected;
        }
    }

    @Nullable
    private String getSelectedLocation() {
        String selected = (String) mLocationSpinner.getSelectedItem();
        if (getString(R.string.value_any_location).equals(selected)) {
            return null;
        } else {
            return selected;
        }
    }

    @Nullable
    private String getSelectedSortBy() {
        String selected = (String) mSortSpinner.getSelectedItem();
        if (getString(R.string.sort_by_most_recent).equals(selected)) {
            return AAR.FIELD_DATE;
        } if (getString(R.string.sort_by_oldest).equals(selected)) {
            return AAR.FIELD_DATE;
        } if (getString(R.string.sort_by_votes).equals(selected)) {
            return AAR.FIELD_UPVOTES;
        } if (getString(R.string.sort_by_views).equals(selected)) {
            return AAR.FIELD_VIEWS;
        }

        return null;
    }

    // handles the sort direction... need to handle for each getSelectedSortBy
    @Nullable
    private Query.Direction getSortDirection() {
        String selected = (String) mSortSpinner.getSelectedItem();
        if (getString(R.string.sort_by_most_recent).equals(selected)){
            return Query.Direction.DESCENDING;
        } if (getString(R.string.sort_by_oldest).equals(selected)){
            return Query.Direction.ASCENDING;
        } if (getString(R.string.sort_by_votes).equals(selected)){
            return Query.Direction.DESCENDING;
        } if (getString(R.string.sort_by_views).equals(selected)){
            return Query.Direction.DESCENDING;
        }

        return null;
    }

    // resets all filters back to defaults??
    public void resetFilters() {
        if (mRootView != null) {
            mCategorySpinner.setSelection(0);
            mLocationSpinner.setSelection(0);
            mSortSpinner.setSelection(0);
        }
    }


    public Filters getFilters() {
        Filters filters = new Filters();

        if (mRootView != null) {
            filters.setCategory(getSelectedCategory());
            filters.setLocation(getSelectedLocation());
            filters.setSortBy(getSelectedSortBy());
            filters.setSortDirection(getSortDirection());
        }
        Log.v(TAG,"these are the filters: " + filters);

        return filters;
    }


}
