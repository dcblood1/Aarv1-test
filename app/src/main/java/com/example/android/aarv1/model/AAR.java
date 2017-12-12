package com.example.android.aarv1.model;

import com.google.firebase.firestore.IgnoreExtraProperties;

/**
 * Created by Dillon on 12/1/2017.
 */


@IgnoreExtraProperties
public class AAR {

    private String category;
    private String title;
    private String description;
    private String cause;
    private String recommendations;
    private String location;
    private String photo;
    private String date;
    private int upVotes;
    private int downVotes;

    // firebase requires you to have object with no calls in it.
    public AAR() {}

    // create object, with place for photo
    public AAR(String category, String title, String description, String cause,
               String recommendations, String location, String photo, int upVotes, int downVotes, String date){
        this.category = category;
        this.title = title;
        this.description = description;
        this.cause = cause;
        this.recommendations = recommendations;
        this.location = location;
        this.photo = photo;
        this.upVotes = upVotes;
        this.downVotes = downVotes;
        this.date = date;
    }

    // create object, without place for photo
    public AAR(String category, String title, String description, String cause,
               String recommendations, String location, int upVotes, int downVotes, String date){
        this.category = category;
        this.title = title;
        this.description = description;
        this.cause = cause;
        this.recommendations = recommendations;
        this.location = location;
        this.upVotes = upVotes;
        this.downVotes = downVotes;
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

    public int getDownVotes() {return downVotes;}

    public void setDownVotes(int downVotes) {this.downVotes = downVotes;}

    public String getDate() {return date;}

    public void setDate(String date) {this.date = date;}

}
