package com.mittaljethwa.android.roommatefinder;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.util.Date;

public class UpdatePersonalProfileActivity extends AppCompatActivity {

    private static final String TAG = "UpdateActivity";
    private EditText inputFirstname;
    private EditText inputLastname;
    private EditText inputBio;
    private RadioGroup radioGender;
    private RadioGroup radioProfileCategory;
    private EditText inputBirthday;
    private Button buttonSave;
    private ToggleButton toggleActivelySearching;

    private String userKey;
    private ProgressBar progressBar;
    private SharedPreferences sharedPreferences;
    private UserProfile userProfile;

    FirebaseAuth auth;
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference databaseReference = FirebaseUtils.getRootRef();
    private DatabaseReference userDatabase = FirebaseUtils.getRefToUsersNode();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_personal_profile);

        auth = FirebaseAuth.getInstance();
        sharedPreferences = getPreferences(Context.MODE_PRIVATE);

        //get current user
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        initializeUIElements();
        readData();

        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                saveUserProfile(user);
            }
        });
    }

    private void initializeUIElements() {
        inputFirstname = this.findViewById(R.id.firstName);
        inputLastname = this.findViewById(R.id.lastName);
        inputBio = this.findViewById(R.id.profileBio);
        radioGender = this.findViewById(R.id.gender);
        radioProfileCategory = this.findViewById(R.id.rgProfileCategory);
        inputBirthday = this.findViewById(R.id.birthday);
        buttonSave = this.findViewById(R.id.saveProfileButton);
        toggleActivelySearching = this.findViewById(R.id.toggleButtonLooking);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
    }

    private void readData() {
        String key = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseUtils.getRefToSpecificUser(key).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue() == null) {
                    Log.d(TAG, "User data snapshot is null, No data to load");
                }
                else {
                    Log.d(TAG,"User data snapshot is not null, Loading Data" + dataSnapshot.getValue());
                    userProfile = dataSnapshot.getValue(UserProfile.class);
                    loadData();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void loadData() {
        String firstName = userProfile.getFirstname();
        String lastName = userProfile.getLastname();
        String bio = userProfile.getBio();
        String gender = userProfile.getGender();
        String profileCategory = userProfile.getProfileCategory();
        String birthdate = userProfile.getBirthdate();
        Boolean isActivelySearching = userProfile.getActivelySearching();

        inputFirstname.setText(firstName);
        inputLastname.setText(lastName);
        inputBio.setText(bio);
        radioGender.check(getGenderID(gender));
        radioProfileCategory.check(getProfileCategoryID(profileCategory));
        inputBirthday.setText(birthdate);
        toggleActivelySearching.setChecked(isActivelySearching);

    }

    private void saveUserProfile(FirebaseUser user) {

        if(Utils.isNetworkConnected(getApplicationContext())) {

            userProfile = new UserProfile();
            String firstName = inputFirstname.getText().toString().trim();
            String lastName = inputLastname.getText().toString().trim();
            String bio = inputBio.getText().toString().trim();
            String gender = getGenderSelected(radioGender.getCheckedRadioButtonId());
            String profileCategory = getProfileCategorySelected(radioProfileCategory.getCheckedRadioButtonId());
            String birthdate = inputBirthday.getText().toString().trim();
            Boolean isActivelySearching = toggleActivelySearching.isChecked();

            Log.d(TAG, "Gender Selected ID: " + radioGender.getCheckedRadioButtonId());
            Log.d(TAG, "Profile Selected ID : " + radioProfileCategory.getCheckedRadioButtonId());

            if (TextUtils.isEmpty(firstName)) {
                Toast.makeText(getApplicationContext(), "Enter First Name!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (TextUtils.isEmpty(lastName)) {
                Toast.makeText(getApplicationContext(), "Enter Last Name!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (TextUtils.isEmpty(bio)) {
                Toast.makeText(getApplicationContext(), "Enter Bio!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (TextUtils.isEmpty(birthdate)) {
                Toast.makeText(getApplicationContext(), "Enter your Birthdate!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (bio.length() > 240) {
                Toast.makeText(getApplicationContext(), "Limit Bio to 240 characters!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (gender.equals("None")) {
                Toast.makeText(getApplicationContext(), "Please select your gender!",Toast.LENGTH_SHORT).show();
                return;
            }

            if (profileCategory.equals("None")) {
                Toast.makeText(getApplicationContext(), "Please select your profile category!",Toast.LENGTH_SHORT).show();
                return;
            }

            userProfile.setFirstname(firstName);
            userProfile.setLastname(lastName);
            userProfile.setBio(bio);
            userProfile.setBirthdate(birthdate);
            userProfile.setGender(gender);
            userProfile.setProfileCategory(profileCategory);
            userProfile.setActivelySearching(isActivelySearching);
            userProfile.setEmail(user.getEmail());

            progressBar.setVisibility(View.VISIBLE);

            saveUserProfileOnServer(userProfile);

        }
        else {

            Toast.makeText(UpdatePersonalProfileActivity.this,R.string.error_no_connection,Toast.LENGTH_SHORT).show();

        }
    }

    private void saveUserProfileOnDevice(UserProfile userProfile) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(UserProfile.class.getSimpleName(), new Gson().toJson(userProfile));
            editor.apply();
    }

    private void saveUserProfileOnServer(final UserProfile userProfile) {

//        //Creating a new user object on server and get key
        final String userKey = auth.getCurrentUser().getUid();

        // pushing user to 'Users' node using the userKey
        userDatabase.child(userKey).setValue(userProfile, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                //If databaseError -> NULL, transaction was successful, redirect to home activity
                if (databaseError==null) {

                    saveUserProfileOnDevice(userProfile);

                    startActivity(new Intent(UpdatePersonalProfileActivity.this, HomeActivity.class));
                    finish();

                }
                else {

                    progressBar.setVisibility(View.GONE);

                    Toast.makeText(UpdatePersonalProfileActivity.this, getString(R.string.error_saving_data) + databaseError.getMessage(),
                            Toast.LENGTH_SHORT).show();
                }
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
}
