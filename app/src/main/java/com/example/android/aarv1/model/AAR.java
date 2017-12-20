package com.example.android.aarv1.model;

import com.google.firebase.firestore.IgnoreExtraProperties;

/**
 * Created by Dillon on 12/1/2017.
 */


@IgnoreExtraProperties
public class AAR {

    // these are used for the filters, HAS TO BE A FIELD IN THE AAR... DUMMY
    public static final String FIELD_LOCATION = "location";
    public static final String FIELD_CATEGORY = "category";
    public static final String FIELD_DATE = "date";
    public static final String FIELD_NEWEST = "newest";
    public static final String FIELD_OLDEST = "date";
    public static final String FIELD_VIEWS= "views";
    public static final String FIELD_UPVOTES = "upVotes";
    public static final String FIELD_HOTTEST = "hottest"; // going to implement this later, for when it is looked at the most
    // need to add all of these fileds to the actual AAR POJO

    private String category;
    private String title;
    private String description;
    private String cause;
    private String recommendations;
    private String location;
    private String photo;
    private String date;
    private int upVotes;
    private int views;
    private int hotness;

    // firebase requires you to have object with no calls in it.
    public AAR() {}

    // create object, with place for photo
    public AAR(String category, String title, String description, String cause,
               String recommendations, String location, String photo, int upVotes, int views, String date){
        this.category = category;
        this.title = title;
        this.description = description;
        this.cause = cause;
        this.recommendations = recommendations;
        this.location = location;
        this.photo = photo;
        this.upVotes = upVotes;
        this.views = views;
        this.date = date;
    }

    // create object, without place for photo
    public AAR(String category, String title, String description, String cause,
               String recommendations, String location, int upVotes, int views, String date){
        this.category = category;
        this.title = title;
        this.description = description;
        this.cause = cause;
        this.recommendations = recommendations;
        this.location = location;
        this.upVotes = upVotes;
        this.views = views;
        this.date = date;
    }

    public String getCategory() {return category;}

    public void setCategory(String category) {this.category = category;}

    public String getTitle() {return title;}

    public void setTitle(String title) {this.title = title;}

    public String getDescription() {return description;}

    public void setDescription( String description) {this.description= description;}

    public String getCause() { return cause;}

    public void setCause( String cause) {this.cause = cause;}

    public String getRecommendations() {return recommendations;}

    public void setRecommendations( String recommendations) {this.recommendations = recommendations;}

    public String getLocation() {return location;}

    public void setLocation( String location) {this.location = location;}

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public int getUpVotes() {return upVotes;}

    public void setUpVotes(int upVotes) {this.upVotes = upVotes;}

    public int getViews() {return views;}

    public void setViews(int views) {this.views = views;}

    public String getDate() {return date;}

    public void setDate(String date) {this.date = date;}

}
