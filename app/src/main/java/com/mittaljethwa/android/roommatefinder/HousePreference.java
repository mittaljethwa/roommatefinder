package com.mittaljethwa.android.roommatefinder;

/**
 * Created by Mittal on 5/8/2018.
 */

public class HousePreference {

    private String userKey;
    private String placeID;
    private float searchRadius;
    private float minBudget;
    private float maxBudget;
    private String roomType;

    public String getPlaceID() {
        return placeID;
    }

    public void setPlaceID(String placeID) {
        this.placeID = placeID;
    }

    public HousePreference() {}

    public String getUserKey() {
        return userKey;
    }

    public void setUserKey(String userKey) {
        this.userKey = userKey;
    }

    public float getSearchRadius() {
        return searchRadius;
    }

    public void setSearchRadius(float searchRadius) {
        this.searchRadius = searchRadius;
    }

    public float getMinBudget() {
        return minBudget;
    }

    public void setMinBudget(float minBudget) {
        this.minBudget = minBudget;
    }

    public float getMaxBudget() {
        return maxBudget;
    }

    public void setMaxBudget(float maxBudget) {
        this.maxBudget = maxBudget;
    }

    public String getRoomType() {
        return roomType;
    }

    public void setRoomType(String roomType) {
        this.roomType = roomType;
    }


}
