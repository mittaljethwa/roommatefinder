package com.mittaljethwa.android.roommatefinder;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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
    private TextView txtViewCleanlinessScale;
    private TextView txtViewLoudnessScale;
    private TextView txtViewVisitorScale;
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

        progressBar.setVisibility(View.VISIBLE);
        readRoommateProfile();
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
        txtViewRadius.setText(userDetails.getHousingPreferences().get(getString(R.string.search_radius)).toString());
        txtViewBedTime.setText(userDetails.getLifestylePreferences().get(getString(R.string.bed_Time)).toString());
        txtViewWakeUpTime.setText(userDetails.getLifestylePreferences().get(getString(R.string.wakeup_Time)).toString());
        txtViewMinBudget.setText(userDetails.getHousingPreferences().get(getString(R.string.min_Budget)).toString());
        txtViewMaxBudget.setText(userDetails.getHousingPreferences().get(getString(R.string.max_Budget)).toString());
        txtViewCleanlinessScale.setText(userDetails.getLifestylePreferences().get(getString(R.string.cleanliness_scale)).toString());
        txtViewLoudnessScale.setText(userDetails.getLifestylePreferences().get(getString(R.string.loudness_scale)).toString());
        txtViewVisitorScale.setText(userDetails.getLifestylePreferences().get(getString(R.string.visitor_scale)).toString());

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
        txtViewCleanlinessScale = this.findViewById(R.id.tvCleanlinessScale);
        txtViewLoudnessScale = this.findViewById(R.id.tvLoudnessScale);
        txtViewVisitorScale = this.findViewById(R.id.tvVisitorScale);
        progressBar = this.findViewById(R.id.progressBar);
    }
}
