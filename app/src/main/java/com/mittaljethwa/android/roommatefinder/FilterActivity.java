package com.mittaljethwa.android.roommatefinder;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;

public class FilterActivity extends AppCompatActivity {

    private String userKey;
    private SharedPreferences sharedPreferences;
    private UserProfile userProfile;
    private HousePreference housePreference;
    private LifestylePreference lifestylePreference;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter);

        sharedPreferences = getSharedPreferences(MainActivity.class.getSimpleName(),MODE_PRIVATE);
        userKey = FirebaseAuth.getInstance().getCurrentUser().getUid();

        loadDefaultFilters();
    }

    private void loadDefaultFilters() {

        getUserCharacteristicsFromFirebase();
//        setFilters();
    }

    public void getUserCharacteristicsFromFirebase() {



    }
}
