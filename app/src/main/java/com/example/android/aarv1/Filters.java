package com.example.android.aarv1;

/**
 * Created by Dillon on 12/15/2017.
 */

import android.content.Context;
import android.text.TextUtils;

import com.example.android.aarv1.model.AAR;
import com.google.firebase.firestore.Query;

/**
 * Object for passing filters around.
 */

public class Filters {

    private String category = null;
    private String location = null;
    private String sortBy = null;
    private Query.Direction sortDirection = null;

    public Filters() {}


    public static Filters getDefault() {
        Filters filters = new Filters();
        filters.setSortBy(AAR.FIELD_TIMESTAMP);
        filters.setSortDirection(Query.Direction.DESCENDING);

        return filters;
    }

    public boolean hasCategory() {
        return !(TextUtils.isEmpty(category));
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getCategory() {
        return category;
    }

    public boolean hasLocation() {return !(TextUtils.isEmpty(location));}

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location ;
    }

    public boolean hasSortBy() {return !(TextUtils.isEmpty(sortBy));}

    public String getSortBy() {
        return sortBy;
    }

    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }

    public Query.Direction getSortDirection() {
        return sortDirection;
    }

    public void setSortDirection(Query.Direction sortDirection) {
        this.sortDirection = sortDirection;
    }

    // This sets the string and shows the user their filter, in the filter bar
    public String getSearchDescription(Context context) {
        StringBuilder desc = new StringBuilder();

        if (category == null && location == null) {
            desc.append("<b>");
            desc.append(context.getString(R.string.all_aars));
            desc.append("</b>");
        }

        if (category != null) {
            desc.append("<b>");
            desc.append(category);
            desc.append("</b>");
        }

        if (category != null && location != null) {
            desc.append(" in ");
        }

        if (location != null) {
            desc.append("<b>");
            desc.append(location);
            desc.append("</b>");
        }

        return desc.toString();
    }

    // Sets the string for how it is sorted.
    public String getOrderDescription(Context context) {
        if( AAR.FIELD_OLDEST.equals(sortBy) && Query.Direction.ASCENDING.equals(sortDirection)) {
            return context.getString(R.string.sort_by_oldest);
        } else if( AAR.FIELD_UPVOTES.equals(sortBy)) {
            return context.getString(R.string.sort_by_votes);
        } else if( AAR.FIELD_VIEWS.equals(sortBy)) {
            return context.getString(R.string.sort_by_views);
        } else {
            return context.getString(R.string.sort_by_most_recent);
        }
    }
}
