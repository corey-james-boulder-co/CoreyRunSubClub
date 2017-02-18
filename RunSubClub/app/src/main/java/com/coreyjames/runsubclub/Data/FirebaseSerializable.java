package com.coreyjames.runsubclub.Data;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * Created by csteimel on 2/17/17.
 */

public abstract class FirebaseSerializable {

    FirebaseKey firebaseKey;

    public abstract Object encode();

    public String dbRefPathString() {
        return firebaseKey.pathString();
    }

    public void readFromFirebase() {

        DatabaseReference mSessionTypeReference;

        mSessionTypeReference = FirebaseDatabase.getInstance().getReference()
                .child("users").child(dbRefPathString());

        mSessionTypeReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                // the changed value is going to be stored into the dataSnapshot
                // to retrieve value from dataSnapshot we write

                String value = (String) dataSnapshot.getValue();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

//    public

}
