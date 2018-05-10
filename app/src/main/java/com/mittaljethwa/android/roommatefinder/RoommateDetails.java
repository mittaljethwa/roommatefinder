package com.mittaljethwa.android.roommatefinder;

import java.util.HashMap;

/**
 * Created by Mittal on 5/9/2018.
 */

public class RoommateDetails {

    Boolean activelySearching=true;
    String birthdate;
    String lastname;
    String email;
    HashMap<String,Object> housing;//housing={placeID=ChIJzQ7MT3bQ24ARlDAdXPQe5fw, maxBudget=300, searchRadius=2, roomType=Shared, minBudget=200},
    String gender;
    String firstname;
    String bio;
    String profileCategory;
    HashMap<String,Object> lifestyle;//lifestyle={wakeupTime=07:00, visitorScale=4, sharedCookingPref=No, foodPref=Veg, loudnessScale=2, bedTime=23:00


    public Boolean getActivelySearching() {
        return activelySearching;
    }

    public void setActivelySearching(Boolean activelySearching) {
        this.activelySearching = activelySearching;
    }

    public String getBirthdate() {
        return birthdate;
    }

    public void setBirthdate(String birthdate) {
        this.birthdate = birthdate;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public HashMap<String, Object> getHousingPreferences() {
        return housing;
    }

    public void setHousingPreferences(HashMap<String, Object> housing) {
        this.housing = housing;
    }

    public HashMap<String, Object> getLifestylePreferences() {
        return lifestyle;
    }

    public void setLifestylePreferences(HashMap<String, Object> lifestyle) {
        this.lifestyle = lifestyle;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getProfileCategory() {
        return profileCategory;
    }

    public void setProfileCategory(String profileCategory) {
        this.profileCategory = profileCategory;
    }
}
