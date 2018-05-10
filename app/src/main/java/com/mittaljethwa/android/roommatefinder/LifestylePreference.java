package com.mittaljethwa.android.roommatefinder;

/**
 * Created by Mittal on 5/8/2018.
 */

public class LifestylePreference {

    private String userKey;
    private String foodPref;
    private String alcoholPref;
    private String smokePref;
    private String petFriendlyPref;
    private String sharedCookingPref;
    private int cleanlinessScale;
    private int loudnessScale;
    private int visitorScale;
    private String bedTime;
    private String wakeupTime;

    public LifestylePreference() {}

    public String getUserKey() {
        return userKey;
    }

    public void setUserKey(String userKey) {
        this.userKey = userKey;
    }

    public String getFoodPref() {
        return foodPref;
    }

    public void setFoodPref(String foodPref) {
        this.foodPref = foodPref;
    }

    public String getSmokePref() {
        return smokePref;
    }

    public void setSmokePref(String smokePref) {
        this.smokePref = smokePref;
    }

    public String getAlcoholPref() {
        return alcoholPref;
    }

    public void setAlcoholPref(String alcoholPref) {
        this.alcoholPref = alcoholPref;
    }

    public String getPetFriendlyPref() {
        return petFriendlyPref;
    }

    public void setPetFriendlyPref(String petFriendlyPref) {
        this.petFriendlyPref = petFriendlyPref;
    }

    public String getSharedCookingPref() {
        return sharedCookingPref;
    }

    public void setSharedCookingPref(String sharedCookingPref) {
        this.sharedCookingPref = sharedCookingPref;
    }

    public int getCleanlinessScale() {
        return cleanlinessScale;
    }

    public void setCleanlinessScale(int cleanlinessScale) {
        this.cleanlinessScale = cleanlinessScale;
    }

    public int getLoudnessScale() {
        return loudnessScale;
    }

    public void setLoudnessScale(int loudnessScale) {
        this.loudnessScale = loudnessScale;
    }

    public int getVisitorScale() {
        return visitorScale;
    }

    public void setVisitorScale(int visitorScale) {
        this.visitorScale = visitorScale;
    }

    public String getBedTime() {
        return bedTime;
    }

    public void setBedTime(String bedTime) {
        this.bedTime = bedTime;
    }

    public String getWakeupTime() {
        return wakeupTime;
    }

    public void setWakeupTime(String wakeupTime) {
        this.wakeupTime = wakeupTime;
    }

}
