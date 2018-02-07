package com.example.android.aarv1.viewmodel;

import android.arch.lifecycle.ViewModel;
import android.util.Log;

import com.example.android.aarv1.Filters;

/**
 * Created by Dillon on 12/16/2017.
 */

// This manages the data passed between the fragment activity, and the main activity, so data is kept.
    // IDK about the sign in feature though, or why it is needed here...
    // only show if the user is signed in?? idk...
public class MainActivityViewModel extends ViewModel {

    private boolean mIsSigningIn;
    private Filters mFilters;
    private String mSearchDescriptionString;
    private String mOrderDescriptionString;

    public MainActivityViewModel(){
        mIsSigningIn = false;
        mFilters = Filters.getDefault();
    }

    public boolean getIsSigningIn() {
        return mIsSigningIn;
    }

    public void setIsSigningIn(boolean mIsSigningIn) {
        this.mIsSigningIn = mIsSigningIn;
    }

    public Filters getFilters(){
        Log.v(String.valueOf(MainActivityViewModel.class),"this is getFilters() in mainActivityViewModel");
        return mFilters;}

    public void setFilters(Filters mFilters) {
        Log.v(String.valueOf(MainActivityViewModel.class),"this is setFilters() in mainActivityViewModel");
        this.mFilters = mFilters; }

    public String getSearchDescription() {
        return mSearchDescriptionString;
    }
    public void setSearchDescription(String mSearchDescriptionString) {
        this.mSearchDescriptionString = mSearchDescriptionString;
    }

    public String getOrderDescription() {
        return mOrderDescriptionString;
    }

    public void setOrderDescription(String mOrderDescriptionString) {
        this.mOrderDescriptionString = mOrderDescriptionString;
    }
}