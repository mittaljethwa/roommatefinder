package com.mittaljethwa.android.roommatefinder;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by Mittal on 5/8/2018.
 */

public class FirebaseUtils {
        private static FirebaseDatabase database = FirebaseDatabase.getInstance();

        public static DatabaseReference getRootRef(){
            return database.getReference();
        }


        //User details
        public static DatabaseReference getRefToUsersNode(){
            DatabaseReference ref = getRootRef().child("Users");
            return ref;
        }

        public static DatabaseReference getRefToSpecificUser(String key){
            DatabaseReference ref = getRefToUsersNode();
            return ref.child(key);
        }

        public static DatabaseReference getRefToUserName(String key){
            DatabaseReference ref = getRefToSpecificUser(key);
            return ref.child("firstname") ;
        }

}
