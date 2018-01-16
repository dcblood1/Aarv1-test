package com.example.android.aarv1;

/**
 * Created by Dillon on 12/15/2017.
 */

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.example.android.aarv1.model.AAR;
import com.google.firebase.firestore.Query;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Dialog Fragment containing filter form.
 * a dialog fragment is one that is a pop up (floating)
 */

public class FilterDialogFragment extends DialogFragment {

    public static final String TAG = "FilterDialog";

    public interface FilterListener {

        void onFilter(Filters filters);

    }

    private View mRootView;

    @BindView(R.id.spinner_category)
    Spinner mCategorySpinner;

    @BindView(R.id.spinner_location)
    Spinner mLocationSpinner;

    @BindView(R.id.spinner_sort)
    Spinner mSortSpinner;

    //@BindView(R.id.search_function_view)
    //EditText mSearchView;

    // declares the interface we set up above.
    private FilterListener mFilterListener;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String PARAM_1 = "category";
    private static final String PARAM_2 = "location";
    private static final String PARAM_3 = "sortBy";

    // TODO: Rename and change types of parameters
    private String mCategory;
    private String mLocation;
    private String mSortBy;

    // TODO: Rename and change types and number of parameters
    public static FilterDialogFragment newInstance(String category, String location, String sortBy) {
        FilterDialogFragment fragment = new FilterDialogFragment();
        Bundle args = new Bundle();
        args.putString(PARAM_1, category);
        args.putString(PARAM_2, location);
        args.putString(PARAM_3, sortBy);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        mRootView = inflater.inflate(R.layout.dialog_filters, container, false);

        Spinner categorySpinner = (Spinner) mRootView.findViewById(R.id.spinner_category);
        Spinner locationSpinner = (Spinner) mRootView.findViewById(R.id.spinner_location);
        Spinner sortBySpinner = (Spinner) mRootView.findViewById(R.id.spinner_sort);


        if (getArguments() != null) {
            Log.v(TAG,"getArguments in filterdialog" + getArguments());
            mCategory= getArguments().getString(PARAM_1);
            mLocation= getArguments().getString(PARAM_2);
            mSortBy= getArguments().getString(PARAM_3);
            categorySpinner.setSelection(((ArrayAdapter<String>)categorySpinner.getAdapter()).getPosition(mCategory));
            locationSpinner.setSelection(((ArrayAdapter<String>)locationSpinner.getAdapter()).getPosition(mLocation));
            sortBySpinner.setSelection(((ArrayAdapter<String>)sortBySpinner.getAdapter()).getPosition(mSortBy));

        }

        ButterKnife.bind(this, mRootView);

        return mRootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof FilterListener) {
            mFilterListener = (FilterListener) context;
        }else {
            throw new RuntimeException(context.toString()
                    + " must implement FilterListener");
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

        Log.v(TAG,"button_cancel clicked");
        dismiss();
    }

    //
    @Nullable
    public String getSelectedCategory() {
        String selected = (String) mCategorySpinner.getSelectedItem();
        if (getString(R.string.value_any_category).equals(selected)) {
            return null;
        } else {
            return selected;
        }
    }

    @Nullable
    public String getSelectedLocation() {
        String selected = (String) mLocationSpinner.getSelectedItem();
        if (getString(R.string.value_any_location).equals(selected)) {
            return null;
        } else {
            return selected;
        }
    }

    @Nullable
    public String getSelectedSortBy() {
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
    public Query.Direction getSortDirection() {
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

    // resets all filters back to defaults
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
        Log.v(TAG,"these are the filters in FilterDialogFragment: " + filters);

        //Bundle stuff??

        // also return selections??
        return filters;
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


}
