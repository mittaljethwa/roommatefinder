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

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        sharedPreferences = getSharedPreferences(MainActivity.class.getSimpleName(),MODE_PRIVATE);

        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }

        loadInitialResults();

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

    private void loadInitialResults() {

        String userGender = getCurrentUserGender();


        DatabaseReference userReference = FirebaseUtils.getRefToUsersNode();
        Query query;

        if (userGender==null) {
            query = userReference.orderByChild("lastname");
        }
        else {
            query = userReference.orderByChild("gender").equalTo(userGender);
        }

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot user : dataSnapshot.getChildren()) {
                        RoommateDetails roommateDetails = user.getValue(RoommateDetails.class);
                        Log.d(user.child("firstname").getValue().toString(), user.getValue().toString() );
                        if( roommateDetails.getHousingPreferences() != null)
                            Log.d(user.child("firstname").getValue().toString(), roommateDetails.getHousingPreferences().get("placeID").toString());
                        if( roommateDetails.getLifestylePreferences() != null)
                            Log.d(user.child("firstname").getValue().toString(), roommateDetails.getLifestylePreferences().get("petFriendlyPref").toString());
                        //roomDetailsList.add(roommateDetails);
                    }

//                    updateRecyclerUI();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

//    void updateRecyclerUI() {
//        if (roomDetailsList.size() == 0) {
//            noClassesTextView.setVisibility(View.VISIBLE);
//            recyclerCourses.setVisibility(View.GONE);
//        }
//        else {
//            noClassesTextView.setVisibility(View.GONE);
//            recyclerCourses.setVisibility(View.VISIBLE);
//            ClassesRecyclerAdapter adapter = new ClassesRecyclerAdapter(classDetailsList, false, getResources().getColor(R.color.green), new ClassesRecyclerAdapter.ItemClickedListener() {
//                @Override
//                public void onItemActionButtonClicked(ClassDetails classDetails) {
//                    enrollToTheCourse(classDetails.getWaitlist() > 0, classDetails);
//                }
//            });
//            RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
//            recyclerCourses.setLayoutManager(mLayoutManager);
//            recyclerCourses.setItemAnimator(new DefaultItemAnimator());
//            recyclerCourses.setAdapter(adapter);
//        }
//    }

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
        String userProfileData = sharedPreferences.getString(UserProfile.class.getSimpleName(), "");
        Log.d(TAG,"User data: " + userProfileData);
        userProfile = new Gson().fromJson(userProfileData, UserProfile.class);
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

    public String getCurrentUserGender() {

        String userKey = FirebaseAuth.getInstance().getCurrentUser().getUid();
        final String[] gender = new String[1];
        FirebaseUtils.getRefToSpecificUser(userKey).child("gender").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue() == null) {
                    Log.d(TAG, "Gender data snapshot is null, No data to load");
                }
                else {
                    Log.d(TAG,"Gender data snapshot is not null, Loading Data " + dataSnapshot.getValue());
                    gender[0] = dataSnapshot.getValue().toString();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        return gender[0];
    }
}
