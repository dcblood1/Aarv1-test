package com.example.android.aarv1.model;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;
import java.util.List;

/**
 * Created by Dillon on 1/18/2018.
 */
// This is needed to save liked aars to the profile to display them when running a query
    // Do I need to save other user data here?? does it matter? will it matter later?
    // If people want to be put into teams... etc


public class UserProfile {

    // Empty Constructor for Firebase
    public UserProfile() {}

    public static final String FIELD_UP_VOTED = "upVotes"; // did the user upVote a post

    private String userId;
    private String userName;
    private String userEmail;
    private @ServerTimestamp Date timestamp;
    private List<String> list_up_Votes;


    public UserProfile(FirebaseUser user, List<String> list_up_Votes){
        this.userId = user.getUid();
        this.userName = user.getDisplayName();
        this.userEmail = user.getEmail();
        this.list_up_Votes = list_up_Votes;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public List<String> getListUpVotes() {return list_up_Votes;}

    public void setListUpVotes(List<String> list_up_Votes) {this.list_up_Votes = list_up_Votes;}

    public void addUserUpVote(String userUpVote){
        list_up_Votes.add(userUpVote);
    }

    public void removeUserUpVote(String userUpVote){
        list_up_Votes.remove(userUpVote);
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }




}
