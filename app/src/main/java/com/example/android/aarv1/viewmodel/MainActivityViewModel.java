package com.example.android.aarv1.viewmodel;

import android.arch.lifecycle.ViewModel;

import com.example.android.aarv1.Filters;

/**
 * Created by Dillon on 12/16/2017.
 */

// idk what this does... needed for filters though, as well as signing in stuff...
public class MainActivityViewModel extends ViewModel {

    private Filters mFilters;

    public MainActivityViewModel(){
        mFilters = Filters.getDefault();
    }

    public Filters getFilters(){ return mFilters;}

    public void setFilters(Filters mFilters) { this.mFilters = mFilters; }

}