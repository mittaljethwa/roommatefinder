package com.mittaljethwa.android.roommatefinder;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.location.places.PlaceBufferResponse;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final int FROM_HOME = 100 ;
    private static final String TAG = "HomeActivity";
    private FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener authListener;
    private ProgressBar progressBar;
    private TextView textAccountName;
    private TextView textAccountEmail;
    private RecyclerView recyclerRoommates;
    private TextView noRoommatesTextView;
    private String gender = null;

    private SharedPreferences sharedPreferences;
    private UserProfile userProfile;
    private List<RoommateDetails> roomDetailsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.title_activity_home));
        setSupportActionBar(toolbar);

        String currentUserKey = FirebaseAuth.getInstance().getCurrentUser().getUid();

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        sharedPreferences = getSharedPreferences(currentUserKey,MODE_PRIVATE);

        recyclerRoommates = this.findViewById(R.id.recyclerRoommates);
        noRoommatesTextView = this.findViewById(R.id.textNoRoommates);

        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
                startActivityForResult(new Intent(HomeActivity.this,FilterActivity.class),FROM_HOME);
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        loadUserProfile();

        if (userProfile != null) {

            View headerView = navigationView.getHeaderView(0);
            textAccountName = headerView.findViewById(R.id.accountName);
            textAccountEmail = headerView.findViewById(R.id.accountEmail);

            textAccountName.setText(userProfile.getFirstname() + " " + userProfile.getLastname());
            textAccountEmail.setText(userProfile.getEmail());
            gender = userProfile.getGender();
        }

        if (sharedPreferences.getString("Filters","") == "")
            //No filters are set
            loadInitialResults();
        else {
            //Use user applied filters
            Log.d("User filters: ","filters present");
            applyCustomFilters();
        }


        //get firebase auth instance
        auth = FirebaseAuth.getInstance();


        authListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user == null) {
                    // user auth state is changed - user is null
                    // launch login activity
                    startActivity(new Intent(HomeActivity.this, LoginActivity.class));
                    finish();
                }
            }
        };
    }

    private void applyCustomFilters() {

        DatabaseReference userReference = FirebaseUtils.getRefToUsersNode();
        Query query;

        final Filters customFilters = getCustomFilters();

        String initialFilter = "gender";
        query = userReference.orderByChild(initialFilter).equalTo(customFilters.getGender());

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                roomDetailsList =  new ArrayList<>();
                String currentUserKey = FirebaseAuth.getInstance().getCurrentUser().getUid();
                if (dataSnapshot.exists()) {
                    for (DataSnapshot user : dataSnapshot.getChildren()) {

                        //Get all users matching the query, except self.
                        if(!user.getKey().equals(currentUserKey)) {

                            RoommateDetails roommateDetails = user.getValue(RoommateDetails.class);
                            if(filtersApplicable(roommateDetails,customFilters)) {
                                roomDetailsList.add(roommateDetails);
                            }
                        }
                    }

                    updateRecyclerUI();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private boolean filtersApplicable(RoommateDetails roommateDetails, Filters customFilters) {
        boolean isAMatch = true;
        if (customFilters.getProfileCateogry().equals("Any")) {
            isAMatch = true;
        }
        else if (!customFilters.getProfileCateogry().equals(roommateDetails.getProfileCategory())) {
            return false;
        }

        if(customFilters.getSmokePref().equals("")) {
            isAMatch = true;
        }
        else if(!customFilters.getSmokePref().equals(roommateDetails.getLifestylePreferences().get("smokePref"))) {
            return false;
        }

        if(customFilters.getAlcoholPref().equals("")) {
            isAMatch = true;
        }
        else if(!customFilters.getAlcoholPref().equals(roommateDetails.getLifestylePreferences().get("smokePref"))) {
            return false;
        }

        return true;
    }

    private void loadInitialResults() {

        DatabaseReference userReference = FirebaseUtils.getRefToUsersNode();
        Query query;

        if (gender==null) {
            query = userReference.orderByChild("lastname");
            Log.d("Gender","ABSENT");
        }
        else {
            query = userReference.orderByChild("gender").equalTo(gender);
            Log.d("Gender","PRESENT");
        }


        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                roomDetailsList =  new ArrayList<>();
                String currentUserKey = FirebaseAuth.getInstance().getCurrentUser().getUid();
                Log.d("Current USER KEY ",currentUserKey );
                if (dataSnapshot.exists()) {
                    for (DataSnapshot user : dataSnapshot.getChildren()) {

                        //Get all users matching the query, except self.
                        if(!user.getKey().equals(currentUserKey)) {

                            Log.d("USER KEY ",user.getKey());
                            RoommateDetails roommateDetails = user.getValue(RoommateDetails.class);
                            roomDetailsList.add(roommateDetails);
                        }
                    }

                    updateRecyclerUI();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    void updateRecyclerUI() {
        if (roomDetailsList.size() == 0) {
            noRoommatesTextView.setVisibility(View.VISIBLE);
            recyclerRoommates.setVisibility(View.GONE);
        }
        else {
            noRoommatesTextView.setVisibility(View.GONE);
            recyclerRoommates.setVisibility(View.VISIBLE);
            RoommatesRecyclerAdapter adapter = new RoommatesRecyclerAdapter(roomDetailsList, new RoommatesRecyclerAdapter.ItemClickedListener() {
                @Override
                public void onItemActionButtonClicked(RoommateDetails roommateDetails) {
                    //enrollToTheCourse(classDetails.getWaitlist() > 0, classDetails);
                    Log.d("Item Clicked",roommateDetails.getFirstname());
                }
            });
            RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
            recyclerRoommates.setLayoutManager(mLayoutManager);
            recyclerRoommates.setItemAnimator(new DefaultItemAnimator());
            recyclerRoommates.setAdapter(adapter);
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        if (id == R.id.action_signOut) {
            //Sign out current user and Redirect to Login Page
            signOut();
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_update_profile) {
//            startActivityForResult(new Intent(HomeActivity.this, UpdatePersonalProfileActivity.class));
            startActivityForResult(new Intent(HomeActivity.this, UpdatePersonalProfileActivity.class),FROM_HOME);
        }
        else if(id == R.id.nav_lifestyle) {
            startActivityForResult(new Intent(HomeActivity.this, UpdateLifestylePreferencesActivity.class),FROM_HOME);
        }
        else if(id == R.id.nav_housing) {
            startActivityForResult(new Intent(HomeActivity.this, UpdateHousingPreferencesActivity.class),FROM_HOME);
        }
        else if(id == R.id.nav_logoff) {
            signOut();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    void loadUserProfile() {
        final String userKey = FirebaseAuth.getInstance().getCurrentUser().getUid();
        getUserProfileFromFirebase(userKey);
        String userProfileData = sharedPreferences.getString(userKey, "");
        userProfile = new Gson().fromJson(userProfileData, UserProfile.class);
        Log.d(TAG,"User data: " + userProfile);
    }
    //sign out method
    public void signOut() {
        auth.signOut();
    }

    @Override
    protected void onResume() {
        super.onResume();
        progressBar.setVisibility(View.GONE);
    }

    @Override
    public void onStart() {
        super.onStart();
        auth.addAuthStateListener(authListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (authListener != null) {
            auth.removeAuthStateListener(authListener);
        }
    }

    public void getCurrentUserGender() {

        String userKey = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseUtils.getRefToSpecificUser(userKey).child("gender").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue() == null) {
                    Log.d(TAG, "Gender data snapshot is null, No data to load");
                }
                else {
                    Log.d(TAG,"Gender data snapshot is not null, Loading Data " + dataSnapshot.getValue());
                    gender = dataSnapshot.getValue().toString();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void getUserProfileFromFirebase(final String userKey) {
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
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public Filters getCustomFilters() {
        Filters customFilters;

        String searchFilters = sharedPreferences.getString("Filters", "");
        customFilters = new Gson().fromJson(searchFilters, Filters.class);
        Log.d("Loading Filters in home",searchFilters);

        return customFilters;
    }
}
