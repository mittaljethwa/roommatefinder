package com.mittaljethwa.android.roommatefinder;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.View;
import android.util.Log;
import android.app.TimePickerDialog;
import com.google.gson.Gson;

import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.TimePicker;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.DatabaseError;

public class UpdateLifestylePreferencesActivity extends AppCompatActivity {

    private static final String TAG = "UpdateLifestyleActivity";
    private RadioGroup radioFoodPref;
    private RadioGroup radioSmokePref;
    private RadioGroup radioAlcoholPref;
    private RadioGroup radioPetFriendlyPref;
    private RadioGroup radioSharedCookingPref;
    private SeekBar seekCleanlinessScale;
    private SeekBar seekVisitorScale;
    private SeekBar seekLoudnessScale;
    private EditText inputBedTime;
    private EditText inputWakeupTime;
    private Button buttonSave;

    private String userKey;
    private String bedTime = "";
    private String wakeupTime = "";
    private ProgressBar progressBar;
    private SharedPreferences sharedPreferences;
    private LifestylePreference lifestylePreference;

    FirebaseAuth auth;
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference databaseReference = FirebaseUtils.getRootRef();
    private DatabaseReference userDatabase = FirebaseUtils.getRefToUsersNode();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_lifestyle_preferences);

        auth = FirebaseAuth.getInstance();
        sharedPreferences = getSharedPreferences(MainActivity.class.getSimpleName(),MODE_PRIVATE);

        initializeUIElements();

        //set time listeners
        setTimeListeners();

        //get current user
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        progressBar.setVisibility(View.VISIBLE);

        readUserDataFromFirebase();

        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                saveLifestylePreference(user);
            }
        });
    }

    void setTimeListeners() {
        inputBedTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                int initialHour = 0;
                int initialMinute = 0;

                // Set Initial Time
                if(bedTime.length()!=0) {

                    String bedTimeArr[] = bedTime.substring(0,5).split(":");
                    String am_pm = bedTime.substring(6);
                    if(bedTimeArr.length == 2){
                        initialHour = Integer.parseInt(bedTimeArr[0]);
                        initialMinute = Integer.parseInt(bedTimeArr[1]);
                    }

                    if(am_pm.equals("PM")){
                        if(initialHour != 12)
                            initialHour = initialHour + 12;
                    }else{
                        if(initialHour == 12)
                            initialHour = 0;
                    }

                }
                TimePickerDialog mTimePicker = new TimePickerDialog(UpdateLifestylePreferencesActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                        String selectedMinuteString = selectedMinute > 9 ? String.valueOf(selectedMinute) : "0" + selectedMinute;
                        String am_pm = "PM";
                        String selectedHourString = "";
                        if(selectedHour == 0 || selectedHour == 12){
                            selectedHourString = "12";
                        }
                        if(selectedHour > 12) {
                            am_pm = "PM";
                            int actualHours = selectedHour - 12;
                            selectedHourString = actualHours > 9 ? String.valueOf(actualHours) : "0" + actualHours;
                        }else if(selectedHour < 12){
                            am_pm = "AM";
                            if(selectedHour != 0)
                                selectedHourString = selectedHour > 9 ? String.valueOf(selectedHour) : "0" + selectedHour;
                        }
                        bedTime = selectedHourString + ":" + selectedMinuteString + " " + am_pm;
                        inputBedTime.setText(bedTime);
                    }
                }, initialHour, initialMinute, false);//Yes 24 hour time
                mTimePicker.setTitle("Select Bed Time");
                mTimePicker.show();
            }
        });

        inputWakeupTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                int initialHour = 0;
                int initialMinute = 0;

                // Set Initial Time
                if(wakeupTime.length()!=0) {

                    String wakeupTimeArr[] = wakeupTime.substring(0,5).split(":");
                    String am_pm = wakeupTime.substring(6);
                    if(wakeupTimeArr.length == 2){
                        initialHour = Integer.parseInt(wakeupTimeArr[0]);
                        initialMinute = Integer.parseInt(wakeupTimeArr[1]);
                    }

                    if(am_pm.equals("PM")){
                        if(initialHour != 12)
                            initialHour += 12;
                    }else{
                        if(initialHour == 12)
                            initialHour = 0;
                    }
                }

                TimePickerDialog mTimePicker = new TimePickerDialog(UpdateLifestylePreferencesActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                        String selectedMinuteString = selectedMinute > 9 ? String.valueOf(selectedMinute) : "0" + selectedMinute;
                        String am_pm = "PM";
                        String selectedHourString = "";
                        if(selectedHour == 0 || selectedHour == 12){
                            selectedHourString = "12";
                        }
                        if(selectedHour > 12) {
                            am_pm = "PM";
                            int actualHours = selectedHour - 12;
                            selectedHourString = actualHours > 9 ? String.valueOf(actualHours) : "0" + actualHours;
                        }else if(selectedHour < 12){
                            am_pm = "AM";
                            if(selectedHour != 0)
                                selectedHourString = selectedHour > 9 ? String.valueOf(selectedHour) : "0" + selectedHour;
                        }
                        wakeupTime = selectedHourString + ":" + selectedMinuteString + " " + am_pm;;
                        inputWakeupTime.setText(wakeupTime);
                    }
                }, initialHour, initialMinute, false);//Yes 24 hour time
                mTimePicker.setTitle("Select Wakeup Time");
                mTimePicker.show();
            }
        });
    }

    private void saveLifestylePreference(FirebaseUser user) {
        if(Utils.isNetworkConnected(getApplicationContext())) {

            lifestylePreference = new LifestylePreference();

            if (bedTime.length() == 0) {
                Toast.makeText(getApplicationContext(), "Enter Bed Time!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (wakeupTime.length() == 0) {
                Toast.makeText(getApplicationContext(), "Enter Wakeup Time!", Toast.LENGTH_SHORT).show();
                return;
            }

            String foodPref = getFoodPrefSelected(radioFoodPref.getCheckedRadioButtonId());
            String smokePref = getSmokePrefSelected(radioSmokePref.getCheckedRadioButtonId());
            String alcoholPref = getAlcoholPrefSelected(radioAlcoholPref.getCheckedRadioButtonId());
            String petFriendlyPref = getPetFriendlyPrefSelected(radioPetFriendlyPref.getCheckedRadioButtonId());
            String sharedCookPref = getSharedCookingPrefSelected(radioSharedCookingPref.getCheckedRadioButtonId());
            int cleanlinessScale = seekCleanlinessScale.getProgress();
            int visitorScale = seekVisitorScale.getProgress();
            int loudnessScale = seekLoudnessScale.getProgress();

            if (foodPref.equals("None")) {
                Toast.makeText(getApplicationContext(), "Please Select Preferred Food Type!",Toast.LENGTH_SHORT).show();
                return;
            }

            if (smokePref.equals("None")) {
                Toast.makeText(getApplicationContext(), "Please Select Smoking Preference!",Toast.LENGTH_SHORT).show();
                return;
            }

            if (alcoholPref.equals("None")) {
                Toast.makeText(getApplicationContext(), "Please Select Alcohol Preference!",Toast.LENGTH_SHORT).show();
                return;
            }

            if (petFriendlyPref.equals("None")) {
                Toast.makeText(getApplicationContext(), "Please Select Pet Preference!",Toast.LENGTH_SHORT).show();
                return;
            }

            if (sharedCookPref.equals("None")) {
                Toast.makeText(getApplicationContext(), "Please Select Shared Cooking Preference!",Toast.LENGTH_SHORT).show();
                return;
            }

            progressBar.setVisibility(View.VISIBLE);

            lifestylePreference.setFoodPref(foodPref);
            lifestylePreference.setSmokePref(smokePref);
            lifestylePreference.setAlcoholPref(alcoholPref);
            lifestylePreference.setPetFriendlyPref(petFriendlyPref);
            lifestylePreference.setSharedCookingPref(sharedCookPref);
            lifestylePreference.setCleanlinessScale(cleanlinessScale);
            lifestylePreference.setLoudnessScale(visitorScale);
            lifestylePreference.setVisitorScale(loudnessScale);
            lifestylePreference.setBedTime(bedTime);
            lifestylePreference.setWakeupTime(wakeupTime);


            saveUserProfileOnServer(lifestylePreference);

        }
        else {

            Toast.makeText(UpdateLifestylePreferencesActivity.this, R.string.error_no_connection, Toast.LENGTH_SHORT).show();

        }
    }

    private void saveUserProfileOnServer(final LifestylePreference lifestylePreference) {

        final String userKey = auth.getCurrentUser().getUid();

        userDatabase.child(userKey).child("lifestylePreferences").setValue(lifestylePreference, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                progressBar.setVisibility(View.GONE);

                //If databaseError -> NULL, transaction was successful, redirect to home activity
                if (databaseError==null) {

                    saveUserProfileOnDevice(lifestylePreference);

                    startActivity(new Intent(UpdateLifestylePreferencesActivity.this, HomeActivity.class));
                    finish();

                }
                else {

                    Toast.makeText(UpdateLifestylePreferencesActivity.this, getString(R.string.error_saving_data) + databaseError.getMessage(),
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void saveUserProfileOnDevice(LifestylePreference lifestylePreference) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(LifestylePreference.class.getSimpleName(), new Gson().toJson(lifestylePreference));
        editor.apply();
    }

    private String getFoodPrefSelected(int checkedRadioButtonId) {
        switch (checkedRadioButtonId) {
            case -1:
                return "None";
            case R.id.rbProfilePreferencesFoodVeg:
                return "Veg";
            case R.id.rbProfilePreferencesFoodNonVeg:
                return "NonVeg";
            default:
                return "Veg";
        }
    }

    private int getFoodPrefID(String foodPref) {
        switch (foodPref) {
            case "None":
                return -1;
            case "Veg":
                return R.id.rbProfilePreferencesFoodVeg;
            case "NonVeg":
                return R.id.rbProfilePreferencesFoodNonVeg;
            default:
                return R.id.rbProfilePreferencesFoodVeg;
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

    private String getSharedCookingPrefSelected(int checkedRadioButtonId) {
        switch (checkedRadioButtonId) {
            case -1:
                return "None";
            case R.id.rbProfilePreferencesCookingYes:
                return "Yes";
            case R.id.rbProfilePreferencesCookingNo:
                return "No";
            default:
                return "No";
        }
    }

    private int getSharedCookingPrefID(String sharedCookingPref) {
        switch (sharedCookingPref) {
            case "None":
                return -1;
            case "Yes":
                return R.id.rbProfilePreferencesCookingYes;
            case "No":
                return R.id.rbProfilePreferencesCookingNo;
            default:
                return R.id.rbProfilePreferencesCookingYes;
        }
    }

    private void readUserDataFromFirebase() {
        String key = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseUtils.getRefToSpecificUser(key).child("lifestylePreferences").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(com.google.firebase.database.DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue() == null) {
                    Log.d(TAG, "Housing data snapshot is null, No data to load");
                }
                else {
                    Log.d(TAG,"Housing data snapshot is not null, Loading Data" + dataSnapshot.getValue());
                    lifestylePreference = dataSnapshot.getValue(LifestylePreference.class);
                    loadData();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void loadData() {
        String foodPref = lifestylePreference.getFoodPref();
        String smokePref = lifestylePreference.getSmokePref();
        String alcoholPref = lifestylePreference.getAlcoholPref();
        String petFriendlyPref = lifestylePreference.getPetFriendlyPref();
        String sharedCookPref = lifestylePreference.getSharedCookingPref();
        int cleanlinessScale = lifestylePreference.getCleanlinessScale();
        int visitorScale = lifestylePreference.getLoudnessScale();
        int loudnessScale = lifestylePreference.getVisitorScale();
        bedTime = lifestylePreference.getBedTime();
        wakeupTime = lifestylePreference.getWakeupTime();

        radioFoodPref.check(getFoodPrefID(foodPref));
        radioSmokePref.check(getSmokePrefID(smokePref));
        radioAlcoholPref.check(getAlcoholPrefID(alcoholPref));
        radioPetFriendlyPref.check(getPetFriendlyPrefID(petFriendlyPref));
        radioSharedCookingPref.check(getSharedCookingPrefID(sharedCookPref));
        seekCleanlinessScale.setProgress(cleanlinessScale);
        seekVisitorScale.setProgress(visitorScale);
        seekLoudnessScale.setProgress(loudnessScale);
        inputBedTime.setText(bedTime);
        inputWakeupTime.setText(wakeupTime);

        progressBar.setVisibility(View.GONE);
    }

    private void initializeUIElements() {
        radioFoodPref = this.findViewById(R.id.rgProfilePreferencesFood);
        radioSmokePref = this.findViewById(R.id.rgProfilePreferencesSmoke);
        radioAlcoholPref = this.findViewById(R.id.rgProfilePreferencesAlcohol);
        radioPetFriendlyPref = this.findViewById(R.id.rgProfilePreferencesPet);
        radioSharedCookingPref = this.findViewById(R.id.rgProfilePreferencesCooking);
        seekCleanlinessScale = this.findViewById(R.id.sbProfilePreferencesCleanliness);
        seekVisitorScale = this.findViewById(R.id.sbProfilePreferencesLoudness);
        seekLoudnessScale = this.findViewById(R.id.sbProfilePreferencesVisitor);
        inputBedTime = this.findViewById(R.id.bedTime);
        inputWakeupTime = this.findViewById(R.id.wakeupTime);

        buttonSave = this.findViewById(R.id.saveLifestyleButton);
        progressBar = this.findViewById(R.id.progressBar);

        inputBedTime.setEnabled(false);
        inputWakeupTime.setEnabled(false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        progressBar.setVisibility(View.GONE);
    }
}
