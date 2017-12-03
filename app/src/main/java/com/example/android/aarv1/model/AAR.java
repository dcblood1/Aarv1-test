package com.example.android.aarv1.model;

/**
 * Created by Dillon on 12/1/2017.
 */

public class AAR {

    private String category;
    private String title;
    private String good;
    private String bad;
    private String recommendations;
    private String location;
    private String photo;
    private int upVotes;
    private int downVotes;

    public AAR() {}

    public AAR(String category, String title, String good, String bad, String recommendations, String location, String photo, int upVotes, int downVotes){
        this.category = category;
        this.title = title;
        this.good = good;
        this.bad = bad;
        this.recommendations = recommendations;
        this.location = location;
        this.photo = photo;
        this.upVotes = upVotes;
        this.downVotes = downVotes;
    }

    public String getCategory() {return category;}

    public void setCategory(String category) {this.category = category;}

    public String getTitle() {return title;}

    public void setTitle(String title) {this.title = title;}

    public String getGood() {return good;}

    public void setGood( String good) {this.good = good;}

    public String getBad() { return bad;}

    public void setBad( String bad) {this.bad = bad;}

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

}
