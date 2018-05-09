package com.mittaljethwa.android.roommatefinder;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBufferResponse;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
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

public class UpdateHousingPreferencesActivity extends AppCompatActivity {

    private static final String TAG = "UpdateHousingActivity";
    private EditText inputLocation;
    private EditText inputRadius;
    private EditText inputMinBudget;
    private EditText inputMaxBudget;
    private RadioGroup radioRoomType;
    private Button buttonSave;

    private String userKey;
    private ProgressBar progressBar;
    private SharedPreferences sharedPreferences;
    private HousePreference housePreference;

    FirebaseAuth auth;
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference databaseReference = FirebaseUtils.getRootRef();
    private DatabaseReference userDatabase = FirebaseUtils.getRefToUsersNode();

    /**
     * GeoDataClient wraps our service connection to Google Play services and provides access
     * to the Google Places API for Android.
     */
    protected GeoDataClient mGeoDataClient;
    private PlaceAutocompleteAdapter mAdapter;
    private AutoCompleteTextView mAutocompleteView;
    private static final LatLngBounds BOUNDS_SAN_DIEGO = new LatLngBounds(
            new LatLng(32.7157, 117.1611), new LatLng(32.7157, 117.1611));
    private String preferredLocationPlaceID;
    private String preferredLocationPlaceName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_housing_preferences);

        auth = FirebaseAuth.getInstance();
        sharedPreferences = getSharedPreferences(MainActivity.class.getSimpleName(),MODE_PRIVATE);

        initializeUIElements();

        // Construct a GeoDataClient for the Google Places API for Android.
        mGeoDataClient = Places.getGeoDataClient(this, null);

        // Register a listener that receives callbacks when a suggestion has been selected
        mAutocompleteView.setOnItemClickListener(mAutocompleteClickListener);

        // Set up the adapter that will retrieve suggestions from the Places Geo Data Client.
        mAdapter = new PlaceAutocompleteAdapter(this, mGeoDataClient, BOUNDS_SAN_DIEGO, null);
        mAutocompleteView.setAdapter(mAdapter);

        //get current user
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        readUserDataFromFirebase();

        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                saveHousingPreference(user);
            }
        });
    }

    /**
     * Listener that handles selections from suggestions from the AutoCompleteTextView that
     * displays Place suggestions.
     * Gets the place id of the selected item and issues a request to the Places Geo Data Client
     * to retrieve more details about the place.
     *
     * @see GeoDataClient#getPlaceById(String...)
     */
    private AdapterView.OnItemClickListener mAutocompleteClickListener
            = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            /*
             Retrieve the place ID of the selected item from the Adapter.
             The adapter stores each Place suggestion in a AutocompletePrediction from which we
             read the place ID and title.
              */
            final AutocompletePrediction item = mAdapter.getItem(position);
            final String placeId = item.getPlaceId();
            final CharSequence primaryText = item.getPrimaryText(null);

            Log.i(TAG, "Autocomplete item selected: " + primaryText);

            preferredLocationPlaceID = placeId;
            /*
             Issue a request to the Places Geo Data Client to retrieve a Place object with
             additional details about the place.
              */
            Task<PlaceBufferResponse> placeResult = mGeoDataClient.getPlaceById(placeId);
            placeResult.addOnCompleteListener(mUpdatePlaceDetailsCallback);

            Toast.makeText(getApplicationContext(), "Clicked: " + primaryText,
                    Toast.LENGTH_SHORT).show();
            Log.i(TAG, "Called getPlaceById to get Place details for " + placeId);
        }
    };

    /**
     * Callback for results from a Places Geo Data Client query that shows the first place result in
     * the details view on screen.
     */
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

//                // Format details of the place for display and show it in a TextView.
//                mPlaceDetailsText.setText(formatPlaceDetails(getResources(), place.getName(),
//                        place.getId(), place.getAddress(), place.getPhoneNumber(),
//                        place.getWebsiteUri()));
//
//                // Display the third party attributions if set.
//                final CharSequence thirdPartyAttribution = places.getAttributions();
//                if (thirdPartyAttribution == null) {
//                    mPlaceDetailsAttribution.setVisibility(View.GONE);
//                } else {
//                    mPlaceDetailsAttribution.setVisibility(View.VISIBLE);
//                    mPlaceDetailsAttribution.setText(
//                            Html.fromHtml(thirdPartyAttribution.toString()));
//                }
//
//                Log.i(TAG, "Place details received: " + place.getName());

                places.release();
            } catch (RuntimeRemoteException e) {
                // Request did not complete successfully
                Log.e(TAG, "Place query did not complete.", e);
                return;
            }
        }
    };


    private void saveHousingPreference(FirebaseUser user) {

        if(Utils.isNetworkConnected(getApplicationContext())) {

            housePreference = new HousePreference();
            String location = mAutocompleteView.getText().toString().trim();
            float radius = Float.valueOf(inputRadius.getText().toString().trim());
            float minBudget = Float.valueOf(inputMinBudget.getText().toString().trim());
            float maxBudget = Float.valueOf(inputMaxBudget.getText().toString().trim());
            String roomType = getRoomTypeSelected(radioRoomType.getCheckedRadioButtonId());

            if (TextUtils.isEmpty(location)) {
                Toast.makeText(getApplicationContext(), "Enter Location!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (TextUtils.isEmpty(inputRadius.getText().toString())) {
                Toast.makeText(getApplicationContext(), "Enter Preferred Radius!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (TextUtils.isEmpty(inputMinBudget.getText().toString())) {
                Toast.makeText(getApplicationContext(), "Enter Minimum Budget!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (TextUtils.isEmpty(inputMaxBudget.getText().toString())) {
                Toast.makeText(getApplicationContext(), "Enter Maximum Budget!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (roomType.equals("None")) {
                Toast.makeText(getApplicationContext(), "Please Select Preferred Room Type!",Toast.LENGTH_SHORT).show();
                return;
            }

            housePreference.setPlaceID(preferredLocationPlaceID);
            housePreference.setSearchRadius(radius);
            housePreference.setMinBudget(minBudget);
            housePreference.setMaxBudget(maxBudget);
            housePreference.setRoomType(roomType);

            progressBar.setVisibility(View.VISIBLE);

            saveUserProfileOnServer(housePreference);

        }
        else {

            Toast.makeText(UpdateHousingPreferencesActivity.this, R.string.error_no_connection, Toast.LENGTH_SHORT).show();

        }
    }

    private void saveUserProfileOnServer(final HousePreference housePreference) {

        final String userKey = auth.getCurrentUser().getUid();

        userDatabase.child(userKey).child("housing").setValue(housePreference, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                //If databaseError -> NULL, transaction was successful, redirect to home activity
                if (databaseError==null) {

                    saveUserProfileOnDevice(housePreference);

                    startActivity(new Intent(UpdateHousingPreferencesActivity.this, HomeActivity.class));
                    finish();

                }
                else {

                    progressBar.setVisibility(View.GONE);

                    Toast.makeText(UpdateHousingPreferencesActivity.this, getString(R.string.error_saving_data) + databaseError.getMessage(),
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void saveUserProfileOnDevice(HousePreference housePreference) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(HousePreference.class.getSimpleName(), new Gson().toJson(housePreference));
        editor.apply();
    }

    private String getRoomTypeSelected(int checkedRadioButtonId) {
        switch (checkedRadioButtonId) {
            case -1:
                return "None";
            case R.id.radioPreferredRoomTypePrivate:
                return "Private";
            case R.id.radioPreferredRoomTypeShared:
                return "Shared";
            default:
                return "Private";
        }
    }

    private int getRoomTypeID(String roomType) {
        switch (roomType) {
            case "None":
                return -1;
            case "Private":
                return R.id.radioPreferredRoomTypePrivate;
            case "Shared":
                return R.id.radioPreferredRoomTypeShared;
            default:
                return R.id.radioPreferredRoomTypePrivate;
        }
    }


    private void readUserDataFromFirebase() {
        String key = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseUtils.getRefToSpecificUser(key).child("housing").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue() == null) {
                    Log.d(TAG, "Housing data snapshot is null, No data to load");
                }
                else {
                    Log.d(TAG,"Housing data snapshot is not null, Loading Data" + dataSnapshot.getValue());
                    housePreference = dataSnapshot.getValue(HousePreference.class);
                    loadData();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void loadData() {
        String locationPlaceID = housePreference.getPlaceID();
        float radius = housePreference.getSearchRadius();
        float minBudget = housePreference.getMinBudget();
        float maxBudget = housePreference.getMaxBudget();
        String roomType = housePreference.getRoomType();

        Task<PlaceBufferResponse> placeResult = mGeoDataClient.getPlaceById(locationPlaceID);
        placeResult.addOnCompleteListener(mUpdatePlaceDetailsCallback);

        Log.i(TAG,"Place name to set: "+preferredLocationPlaceName);
        mAutocompleteView.setText(preferredLocationPlaceName);
        inputRadius.setText(String.valueOf(radius));
        inputMinBudget.setText(String.valueOf(minBudget));
        inputMaxBudget.setText(String.valueOf(maxBudget));
        radioRoomType.check(getRoomTypeID(roomType));
    }

    private void initializeUIElements() {

        mAutocompleteView = this.findViewById(R.id.preferredLocation);
//        inputLocation = this.findViewById(R.id.preferredLocation);
        inputRadius = this.findViewById(R.id.preferredRadius);
        inputMinBudget = this.findViewById(R.id.preferredMinBudget);
        inputMaxBudget = this.findViewById(R.id.preferredMaxBudget);
        radioRoomType = this.findViewById(R.id.preferredRoomType);
        buttonSave = this.findViewById(R.id.saveHousingButton);

        progressBar = this.findViewById(R.id.progressBar);
    }

}
