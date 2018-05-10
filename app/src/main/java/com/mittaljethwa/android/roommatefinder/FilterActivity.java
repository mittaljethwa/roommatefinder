package com.mittaljethwa.android.roommatefinder;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

public class FilterActivity extends AppCompatActivity {

    private static final String TAG = "FilterActivity" ;
//    private RadioGroup radioFoodPref;
    private RadioGroup radioSmokePref;
    private RadioGroup radioAlcoholPref;
    private RadioGroup radioPetFriendlyPref;
//    private RadioGroup radioSharedCookingPref;
    private RadioGroup radioGender;
    private RadioGroup radioProfileCategory;
    private SeekBar seekCleanlinessScale;
    private SeekBar seekVisitorScale;
    private SeekBar seekLoudnessScale;
    private EditText inputBedTime;
    private EditText inputWakeupTime;
    private Button buttonSave;
    private Button buttonReset;
    private ProgressBar progressBar;
    
    private String userKey;
    private UserProfile userProfile;
    
    private SharedPreferences sharedPreferences;
    private HousePreference housePreference;
    private LifestylePreference lifestylePreference;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter);

        userKey = FirebaseAuth.getInstance().getCurrentUser().getUid();
        sharedPreferences = getSharedPreferences(userKey,MODE_PRIVATE);

        initializeUIElements();
        
        if(sharedPreferences.getString("Filters","")=="")
            setDefaultFilters();
        else {
//            loadCustomFilters();
            Filters customFilter = new Filters();
            String searchFilters = sharedPreferences.getString("Filters", "");
            customFilter = new Gson().fromJson(searchFilters, Filters.class);
            Log.d("Loading Filters: ",searchFilters);
        }

        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveCustomFilters();
                Toast.makeText(FilterActivity.this, R.string.message_filters_saved, Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        buttonReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetAllFilters();
            }
        });
    }

    private void saveCustomFilters() {
        Filters customFilters = new Filters();

        if (inputBedTime.length() == 0) {
            customFilters.setBedTime("");
        }

        if (inputWakeupTime.length() == 0) {
            customFilters.setWakeupTime("");
        }

        String smokePref = getSmokePrefSelected(radioSmokePref.getCheckedRadioButtonId());
        String alcoholPref = getAlcoholPrefSelected(radioAlcoholPref.getCheckedRadioButtonId());
        String petFriendlyPref = getPetFriendlyPrefSelected(radioPetFriendlyPref.getCheckedRadioButtonId());
        String cleanlinessScale = String.valueOf(seekCleanlinessScale.getProgress());
        String visitorScale = String.valueOf(seekVisitorScale.getProgress());
        String loudnessScale = String.valueOf(seekLoudnessScale.getProgress());

        if (smokePref.equals("None")) {
            customFilters.setSmokePref("");
        }

        if (alcoholPref.equals("None")) {
            customFilters.setAlcoholPref("");
        }

        if (petFriendlyPref.equals("None")) {
            customFilters.setPetFriendlyPref("");
        }

        customFilters.setCleanlinessScale(cleanlinessScale);
        customFilters.setLoudnessScale(visitorScale);
        customFilters.setVisitorScale(loudnessScale);


        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("Filters", new Gson().toJson(customFilters));
        editor.apply();

    }

    private void resetAllFilters() {
        radioGender.check(getGenderID(userProfile.getGender()));
        radioSmokePref.check(-1);
        radioAlcoholPref.check(-1);
        radioPetFriendlyPref.check(-1);
        radioProfileCategory.check(R.id.radioAny);
        seekCleanlinessScale.setProgress(0);
        seekVisitorScale.setProgress(0);
        seekLoudnessScale.setProgress(0);
        inputBedTime.setText("");
        inputWakeupTime.setText("");

        inputBedTime.clearFocus();
        inputWakeupTime.clearFocus();
    }

    private void initializeUIElements() {
        radioGender = this.findViewById(R.id.gender);
        radioSmokePref = this.findViewById(R.id.rgProfilePreferencesSmoke);
        radioAlcoholPref = this.findViewById(R.id.rgProfilePreferencesAlcohol);
        radioPetFriendlyPref = this.findViewById(R.id.rgProfilePreferencesPet);
        radioProfileCategory = this.findViewById(R.id.rgProfileCategory);
        seekCleanlinessScale = this.findViewById(R.id.sbProfilePreferencesCleanliness);
        seekVisitorScale = this.findViewById(R.id.sbProfilePreferencesLoudness);
        seekLoudnessScale = this.findViewById(R.id.sbProfilePreferencesVisitor);
        inputBedTime = this.findViewById(R.id.bedTime);
        inputWakeupTime = this.findViewById(R.id.wakeupTime);
        buttonSave = this.findViewById(R.id.saveFilterButton);
        buttonReset = this.findViewById(R.id.resetButton);
        progressBar = this.findViewById(R.id.progressBar);
    }

    private void setDefaultFilters() {

        getUserProfileFromFirebase("Default");
    }

    public void getUserProfileFromFirebase(final String requestType) {
        FirebaseUtils.getRefToSpecificUser(userKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue() == null) {
                    Log.d(TAG, "User profile data snapshot is null, No data to load");
                }
                else {
                    Log.d(TAG,"User profile data snapshot is not null, Loading Data " + dataSnapshot.getValue());
                    userProfile = dataSnapshot.getValue(UserProfile.class);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString(userKey, new Gson().toJson(userProfile));
                    editor.apply();

                    if(requestType.equals("Default")) {
                        setFilters("Default");
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private String getGenderSelected(int checkedRadioButtonId) {
        switch (checkedRadioButtonId) {
            case -1:
                return "None";
            case R.id.radioButtonFemale:
                return "Female";
            case R.id.radioButtonMale:
                return "Male";
            default:
                return "Unknown";
        }
    }

    public void setFilters(String filterType) {
        if (filterType.equals("Default")) {
            Log.d(TAG,"Setting default filters gender: "+userProfile.getGender());


            radioGender.check(getGenderID(userProfile.getGender()));
            radioProfileCategory.check(R.id.radioAny);

        }
        else {
            Log.d(TAG,"Setting custom filters");
        }
    }

    private int getGenderID(String gender) {
        switch (gender) {
            case "None":
                return -1;
            case "Female":
                return R.id.radioButtonFemale;
            case "Male":
                return R.id.radioButtonMale;
            default:
                return -1;
        }
    }

    private String getProfileCategorySelected(int checkedRadioButtonId) {
        switch (checkedRadioButtonId) {
            case -1:
                return "None";
            case R.id.radioProfileStudent:
                return "Student";
            case R.id.radioProfileProfessional:
                return "Professional";
            default:
                return "Unknown";
        }

    }

    private int getProfileCategoryID(String profileCategory) {
        switch (profileCategory) {
            case "None":
                return -1;
            case "Student":
                return R.id.radioProfileStudent;
            case "Professional":
                return R.id.radioProfileProfessional;
            default:
                return -1;
        }
    }

    private String getSmokePrefSelected(int checkedRadioButtonId) {
        switch (checkedRadioButtonId) {
            case -1:
                return "None";
            case R.id.rbProfilePreferencesSmokeYes:
                return "Yes";
            case R.id.rbProfilePreferencesSmokeNo:
                return "No";
            case R.id.rbProfilePreferencesSmokeOccasional:
                return "Occasional";
            default:
                return "No";
        }
    }

    private int getSmokePrefID(String smokePref) {
        switch (smokePref) {
            case "None":
                return -1;
            case "Yes":
                return R.id.rbProfilePreferencesSmokeYes;
            case "No":
                return R.id.rbProfilePreferencesSmokeNo;
            case "Occasional":
                return R.id.rbProfilePreferencesSmokeOccasional;
            default:
                return R.id.rbProfilePreferencesSmokeNo;
        }
    }

    private String getAlcoholPrefSelected(int checkedRadioButtonId) {
        switch (checkedRadioButtonId) {
            case -1:
                return "None";
            case R.id.rbProfilePreferencesAlcoholYes:
                return "Yes";
            case R.id.rbProfilePreferencesAlcoholNo:
                return "No";
            case R.id.rbProfilePreferencesAlcoholOccasional:
                return "Occasional";
            default:
                return "No";
        }
    }

    private int getAlcoholPrefID(String alcoholPref) {
        switch (alcoholPref) {
            case "None":
                return -1;
            case "Yes":
                return R.id.rbProfilePreferencesAlcoholYes;
            case "No":
                return R.id.rbProfilePreferencesAlcoholNo;
            case "Occasional":
                return R.id.rbProfilePreferencesAlcoholOccasional;
            default:
                return R.id.rbProfilePreferencesAlcoholNo;
        }
    }

    private String getPetFriendlyPrefSelected(int checkedRadioButtonId) {
        switch (checkedRadioButtonId) {
            case -1:
                return "None";
            case R.id.rbProfilePreferencesPetYes:
                return "Yes";
            case R.id.rbProfilePreferencesPetNo:
                return "No";
            default:
                return "No";
        }
    }

    private int getPetFriendlyPrefID(String petFriendlyPref) {
        switch (petFriendlyPref) {
            case "None":
                return -1;
            case "Yes":
                return R.id.rbProfilePreferencesPetYes;
            case "No":
                return R.id.rbProfilePreferencesPetNo;
            default:
                return R.id.rbProfilePreferencesPetYes;
        }
    }

}
