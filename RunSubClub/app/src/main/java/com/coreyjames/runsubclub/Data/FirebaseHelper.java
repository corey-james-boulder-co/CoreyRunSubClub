package com.coreyjames.runsubclub.Data;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * Created by csteimel on 2/19/17.
 */

public class FirebaseHelper {

    public DatabaseReference dbRef() {
        return FirebaseDatabase.getInstance().getReference();
    }

    public void readUser(String uid, FirebaseDataHandler handler) {

        dbRef().child("users").child(uid).addListenerForSingleValueEvent(newListener(handler));

    }

    private ValueEventListener newListener(FirebaseDataHandler handler) {

        ValueEventListener listener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                handler.callback(dataSnapshot);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        return listener;
    }


}
