package com.mittaljethwa.android.roommatefinder;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    final private String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();

    }

    @Override
    protected void onStart() {
        super.onStart();

        //Check if the user is signed in and updateUI accordingly
        FirebaseUser currentUser = mAuth.getCurrentUser();
        UpdateUI(currentUser);

    }

    private void UpdateUI(FirebaseUser currentUser) {
        if (currentUser == null) {
            //User is not signed in, display register page
            Intent registerIntent = new Intent(this, RegisterActivity.class);
            startActivity(registerIntent);
        } else {
            //User is signed in, check if personal info is filled and redirect accordingly

            navigateToActivity();
            Intent homeIntent = new Intent(this, HomeActivity.class);
            startActivity(homeIntent);
        }
        finish();
    }

    private void navigateToActivity() {
        String key = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseUtils.getRefToUserName(key).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue() == null)
                {
                    Log.d(TAG,"First name data snapshot is null, asking user to update profile");
                    Intent updateIntent = new Intent(MainActivity.this, UpdatePersonalProfileActivity.class);
                    startActivity(updateIntent);                            }
                else {
                    Log.d(TAG,"First name data snapshot is not null, navigating to Home Screen");
                    Intent homeIntent = new Intent(MainActivity.this, HomeActivity.class);
                    startActivity(homeIntent);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
