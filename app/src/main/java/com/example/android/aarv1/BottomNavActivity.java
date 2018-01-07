package com.example.android.aarv1;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

public class BottomNavActivity extends AppCompatActivity implements
        MainFragment.OnFragmentInteractionListener,
        EditsFragment.OnFragmentInteractionListener,
        SavedFragment.OnFragmentInteractionListener
                {

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    switchToMainFragment();
                    return true;
                case R.id.navigation_edits:
                    switchToEditsFragment();
                    return true;
                case R.id.navigation_saves:
                    switchToSavedFragment();
                    return true;
            }
            return false;
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bottom_nav);

        //this allows us to open up to the main fragment initially
        if (savedInstanceState == null){
            FragmentManager manager = getSupportFragmentManager();
            manager.beginTransaction().replace(R.id.content, new MainFragment()).commit();
        }

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.bottom_navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
    }

    // function called to switch to main fragment
     public void switchToMainFragment(){
        FragmentManager manager = getSupportFragmentManager();
        manager.beginTransaction().replace(R.id.content, new MainFragment()).commit();
     }

     // function called to switch to edits fragment
    public void switchToEditsFragment(){
        FragmentManager manager = getSupportFragmentManager();
        manager.beginTransaction().replace(R.id.content, new EditsFragment()).commit();
    }

    // function called to switch to saved fragment
    public void switchToSavedFragment(){
        FragmentManager manager = getSupportFragmentManager();
        manager.beginTransaction().replace(R.id.content, new SavedFragment()).commit();
    }

    // This controls how fragments can communicate with each other.
    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}
