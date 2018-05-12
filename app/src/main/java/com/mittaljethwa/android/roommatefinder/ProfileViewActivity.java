package com.mittaljethwa.android.roommatefinder;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBufferResponse;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.RuntimeRemoteException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

public class ProfileViewActivity extends AppCompatActivity {
    private static final String TAG = "ProfileViewActivity";
    private TextView txtViewName;
    private TextView txtViewGender;
    private TextView txtViewProfileCategory;
    private TextView txtViewBirthDate;
    private TextView txtViewRoomType;
    private TextView txtViewBio;
    private TextView txtViewLocation;
    private TextView txtViewRadius;
    private TextView txtViewBedTime;
    private TextView txtViewWakeUpTime;
    private TextView txtViewMinBudget;
    private TextView txtViewMaxBudget;
    private TextView txtViewFood;
    private TextView txtViewSmoke;
    private TextView txtViewAlcohol;
    private TextView txtViewPetFriendly;
    private TextView txtViewSharedCook;
    private TextView txtViewCleanlinessScale;
    private TextView txtViewLoudnessScale;
    private TextView txtViewVisitorScale;
    private Button buttonSendEmail;
    private ProgressBar progressBar;
    private RoommateDetails userDetails;
    private SharedPreferences sharedPreferences;

    FirebaseAuth auth;
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference databaseReference = FirebaseUtils.getRootRef();
    private DatabaseReference userDatabase = FirebaseUtils.getRefToUsersNode();

    protected GeoDataClient mGeoDataClient;
    private String preferredLocationPlaceName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_view);

        auth = FirebaseAuth.getInstance();
        sharedPreferences = getSharedPreferences(MainActivity.class.getSimpleName(),MODE_PRIVATE);

        initializeUIElements();

        // Construct a GeoDataClient for the Google Places API for Android.
        mGeoDataClient = Places.getGeoDataClient(this, null);

        //get current user
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        progressBar.setVisibility(View.VISIBLE);
        readRoommateProfile();

        buttonSendEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchEmail();
            }
        });
    }

    private void launchEmail() {
        String subject = getString(R.string.emailSubject);
        String bodyText = getString(R.string.emailBody);
        String mailto = "mailto:"  + userDetails.getEmail() +
                "?cc=" + "" +
                "&subject=" + Uri.encode(subject) +
                "&body=" + Uri.encode(bodyText);

        Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
        emailIntent.setData(Uri.parse(mailto));

        startActivity(emailIntent);
    }

    private OnCompleteListener<PlaceBufferResponse> mUpdatePlaceDetailsCallback
            = new OnCompleteListener<PlaceBufferResponse>() {
        @Override
        public void onComplete(Task<PlaceBufferResponse> task) {
            try {
                PlaceBufferResponse places = task.getResult();

                // Get the Place object from the buffer.
                final Place place = places.get(0);

                preferredLocationPlaceName = place.getName().toString();

                Log.i(TAG,"Place loaded: "+ preferredLocationPlaceName);

                loadData();

                places.release();
            } catch (RuntimeRemoteException e) {
                // Request did not complete successfully
                Log.e(TAG, "Place query did not complete.", e);
                return;
            }
        }
    };

    private void readRoommateProfile() {

        String roommateProfile = sharedPreferences.getString("RoommateProfile", "");
        userDetails = new Gson().fromJson(roommateProfile, RoommateDetails.class);
        String locationPlaceID = userDetails.getHousingPreferences().get(getString(R.string.place_id)).toString();
        Task<PlaceBufferResponse> placeResult = mGeoDataClient.getPlaceById(locationPlaceID);
        placeResult.addOnCompleteListener(mUpdatePlaceDetailsCallback);
        loadData();

    }

    /*
    private void readUserDataFromFirebase() {
        String key = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseUtils.getRefToSpecificUser(key).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue() == null) {
                    Log.d(TAG, "User data snapshot is null, No data to load");
                }
                else {
                    Log.d(TAG,"User data snapshot is not null, Loading Data" + dataSnapshot.getValue());
                    userDetails = dataSnapshot.getValue(RoommateDetails.class);
                    String locationPlaceID = userDetails.getHousingPreferences().get(getString(R.string.place_id)).toString();
                    Task<PlaceBufferResponse> placeResult = mGeoDataClient.getPlaceById(locationPlaceID);
                    placeResult.addOnCompleteListener(mUpdatePlaceDetailsCallback);
                    loadData();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
    */

    private void loadData() {
        txtViewName.setText(userDetails.getFirstname() + " " + userDetails.getLastname());
        txtViewGender.setText(userDetails.getGender());
        txtViewProfileCategory.setText(userDetails.getProfileCategory());
        txtViewBirthDate.setText(userDetails.getBirthdate());
        txtViewRoomType.setText(userDetails.getHousingPreferences().get(getString(R.string.room_type)).toString());
        txtViewBio.setText(userDetails.getBio());
        txtViewLocation.setText(preferredLocationPlaceName);
        txtViewRadius.setText(userDetails.getHousingPreferences().get(getString(R.string.search_radius)).toString() + getString(R.string.label_miles));
        txtViewBedTime.setText(userDetails.getLifestylePreferences().get(getString(R.string.bed_Time)).toString());
        txtViewWakeUpTime.setText(userDetails.getLifestylePreferences().get(getString(R.string.wakeup_Time)).toString());
        txtViewMinBudget.setText(getString(R.string.dollar_sign) + userDetails.getHousingPreferences().get(getString(R.string.min_Budget)).toString());
        txtViewMaxBudget.setText(getString(R.string.dollar_sign) + userDetails.getHousingPreferences().get(getString(R.string.max_Budget)).toString());
        txtViewCleanlinessScale.setText(userDetails.getLifestylePreferences().get(getString(R.string.cleanliness_scale)).toString());
        txtViewLoudnessScale.setText(userDetails.getLifestylePreferences().get(getString(R.string.loudness_scale)).toString());
        txtViewVisitorScale.setText(userDetails.getLifestylePreferences().get(getString(R.string.visitor_scale)).toString());
        String food = userDetails.getLifestylePreferences().get(getString(R.string.food)).toString();
        String smoke = userDetails.getLifestylePreferences().get(getString(R.string.smoke)).toString();
        String alcohol = userDetails.getLifestylePreferences().get(getString(R.string.alcohol)).toString();
        String petFriendly = userDetails.getLifestylePreferences().get(getString(R.string.pet_Friendly)).toString();
        String sharedCook = userDetails.getLifestylePreferences().get(getString(R.string.shared_cook)).toString();

        int yesIcon = R.drawable.ic_check_circle_black_12dp;
        int noIcon = R.drawable.ic_cancel_black_12dp;

        if(food.equals(getString(R.string.str_veg))){
            txtViewFood.setCompoundDrawablesRelativeWithIntrinsicBounds(yesIcon,0,0,0);
        }else{
            txtViewFood.setCompoundDrawablesRelativeWithIntrinsicBounds(noIcon,0,0,0);
        }

        if(smoke.equals(getString(R.string.str_no))){
            txtViewSmoke.setCompoundDrawablesRelativeWithIntrinsicBounds(noIcon,0,0,0);
        }else{
            txtViewSmoke.setCompoundDrawablesRelativeWithIntrinsicBounds(yesIcon,0,0,0);
        }

        if(alcohol.equals(getString(R.string.str_no))){
            txtViewAlcohol.setCompoundDrawablesRelativeWithIntrinsicBounds(noIcon,0,0,0);
        }else{
            txtViewAlcohol.setCompoundDrawablesRelativeWithIntrinsicBounds(yesIcon,0,0,0);
        }

        if(petFriendly.equals(getString(R.string.str_yes))){
            txtViewPetFriendly.setCompoundDrawablesRelativeWithIntrinsicBounds(yesIcon,0,0,0);
        }else{
            txtViewPetFriendly.setCompoundDrawablesRelativeWithIntrinsicBounds(noIcon,0,0,0);
        }

        if(sharedCook.equals(getString(R.string.str_yes))){
            txtViewSharedCook.setCompoundDrawablesRelativeWithIntrinsicBounds(yesIcon,0,0,0);
        }else{
            txtViewSharedCook.setCompoundDrawablesRelativeWithIntrinsicBounds(noIcon,0,0,0);
        }

        Boolean available = userDetails.getActivelySearching();
        for (Drawable drawable : txtViewName.getCompoundDrawables()) {
            if (drawable != null) {
                if(available)
                    drawable.setColorFilter( Color.parseColor("#b2d604"), PorterDuff.Mode.SRC_IN);
                else
                    drawable.setColorFilter( Color.parseColor("#bc2929"), PorterDuff.Mode.SRC_IN);
            }
        }

        progressBar.setVisibility(View.GONE);
    }

    private void initializeUIElements() {
        txtViewName = this.findViewById(R.id.tvName);
        txtViewGender = this.findViewById(R.id.tvGender);
        txtViewProfileCategory = this.findViewById(R.id.tvProfileCategory);
        txtViewBirthDate = this.findViewById(R.id.tvBirthDate);
        txtViewRoomType = this.findViewById(R.id.tvRoomType);
        txtViewBio = this.findViewById(R.id.tvBio);
        txtViewLocation = this.findViewById(R.id.tvLocation);
        txtViewRadius = this.findViewById(R.id.tvRadius);
        txtViewBedTime = this.findViewById(R.id.tvBedTime);
        txtViewWakeUpTime = this.findViewById(R.id.tvWakeupTime);
        txtViewMinBudget = this.findViewById(R.id.tvMinBudget);
        txtViewMaxBudget = this.findViewById(R.id.tvMaxBudget);
        txtViewFood = this.findViewById(R.id.tvFood);
        txtViewSmoke = this.findViewById(R.id.tvSmoke);
        txtViewAlcohol = this.findViewById(R.id.tvAlcohol);
        txtViewPetFriendly = this.findViewById(R.id.tvPetFriendly);
        txtViewSharedCook = this.findViewById(R.id.tvSharedCook);
        txtViewCleanlinessScale = this.findViewById(R.id.tvCleanlinessScale);
        txtViewLoudnessScale = this.findViewById(R.id.tvLoudnessScale);
        txtViewVisitorScale = this.findViewById(R.id.tvVisitorScale);
        buttonSendEmail = this.findViewById(R.id.actionSendEmail);
        progressBar = this.findViewById(R.id.progressBar);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return (super.onOptionsItemSelected(item));
    }
}
