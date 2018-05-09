package com.mittaljethwa.android.roommatefinder;

import java.util.Date;

/**
 * Created by Mittal on 5/7/2018.
 */

public class UserProfile {

    private String firstname;
    private String lastname;
    private String email;
    private String gender;
    private String birthdate;
    private String profileCategory;
    private Boolean isActivelySearching;
    private String bio;

    public UserProfile() {

    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
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

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getBirthdate() {
        return birthdate;
    }

    public void setBirthdate(String birthdate) {
        this.birthdate = birthdate;
    }

    public String getProfileCategory() {
        return profileCategory;
    }

    public void setProfileCategory(String profileCategory) {
        this.profileCategory = profileCategory;
    }

    public Boolean getActivelySearching() {
        return isActivelySearching;
    }

    public void setActivelySearching(Boolean activelySearching) {
        isActivelySearching = activelySearching;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }


}
