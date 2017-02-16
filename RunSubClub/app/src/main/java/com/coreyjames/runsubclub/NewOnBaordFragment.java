package com.coreyjames.runsubclub;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ProgressBar;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;



/**
 * Created by csteimel on 2/11/17.
 */

public class NewOnBaordFragment extends Fragment {


    public NewOnBaordFragment() {
    }


    public static final String ANONYMOUS = "anonymous";

    public static final int RC_SIGN_IN = 1;

    private GridView mMessageListView;
    private StaticFirebaseDataAdapter mMessageAdapter;
    private ProgressBar mProgressBar;

    private String mUsername;

    // Firebase instance variables
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mMessagesDatabaseReference;
    private ChildEventListener mChildEventListener;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private DatabaseReference mUserIdDatabaseReference;

    private Fragment fragment;
    private FragmentManager fragmentManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View myNewOnBoardFragment = inflater.inflate(R.layout.fragment_on_board, container, false);

        initializeFirebaseComponents();

        initializeFirebaseDB("basicPlan");

        initializePublicViews();

        initializeListViewAdapter();

        fragmentManager = getFragmentManager();

        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    onSignedInInitialize(user.getDisplayName());

                } else {
                    // User is signed out
                    onSignedOutCleanup();
                    startActivityForResult(
                            AuthUI.getInstance()
                                    .createSignInIntentBuilder()
                                    .setIsSmartLockEnabled(false)
                                    .setProviders(
                                            AuthUI.EMAIL_PROVIDER,
                                            AuthUI.GOOGLE_PROVIDER)
                                    .build(),
                            RC_SIGN_IN);
                }



                fragment = new OnBoardFragment();

                final FragmentTransaction transaction = fragmentManager.beginTransaction();
                transaction.replace(R.id.main_container, fragment).commit();


            }
        };

        initializeProgressBar();








        return myNewOnBoardFragment;
    }

    @Override
    public void onResume() {
        super.onResume();
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mAuthStateListener != null) {
            mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
        }
        mMessageAdapter.clear();
        detachDatabaseReadListener();
    }

    public void onSignedInInitialize(String username) {
        mUsername = username;
        attachDatabaseReadListener();
        checkUserExistsUserFirebaseDatabase();
    }

    public void checkUserExistsUserFirebaseDatabase() {

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        mUserIdDatabaseReference = FirebaseDatabase.getInstance().getReference()
                .child("users").child(user.getUid());

        mUserIdDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                Boolean exists = dataSnapshot.exists();

                if (exists == true) {

                } else {
                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                    DatabaseReference mRefPlanData = database.getReference()
                            .child("trainingPlanLibrary").child("basicPlan");

                    mRefPlanData.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            // the changed value is going to be stored into the dataSnapshot
                            // to retrieve value from dataSnapshot we write

                            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                            String userId = user.getUid();
                            FirebaseDatabase database = FirebaseDatabase.getInstance();

                            DatabaseReference mRefActivePlan = database.getReference().child("users")
                                    .child(userId).child("activePlan");

                            Object value = dataSnapshot.getValue();
                            mRefActivePlan.setValue(value);

                            DatabaseReference mRefCurrentPlanName = database.getReference().child("users")
                                    .child(userId).child("currentPlanTitle");

                            mRefCurrentPlanName.setValue("basicPlan");

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void onSignedOutCleanup() {
        mUsername = ANONYMOUS;
        mMessageAdapter.clear();
        detachDatabaseReadListener();
    }

    public void attachDatabaseReadListener() {
        if (mChildEventListener == null) {
            mChildEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    StaticFirebaseDataHelper staticFirebaseData = dataSnapshot.getValue(StaticFirebaseDataHelper.class);
                    mMessageAdapter.add(staticFirebaseData);

                }

                public void onChildChanged(DataSnapshot dataSnapshot, String s) {}
                public void onChildRemoved(DataSnapshot dataSnapshot) {}
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {}
                public void onCancelled(DatabaseError databaseError) {}
            };
            mMessagesDatabaseReference.addChildEventListener(mChildEventListener);
        }
    }

    public void detachDatabaseReadListener() {
        if (mChildEventListener != null) {
            mMessagesDatabaseReference.removeEventListener(mChildEventListener);
            mChildEventListener = null;
        }
    }

    public void initializeFirebaseComponents() {
        // Initialize Firebase components
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mFirebaseAuth = FirebaseAuth.getInstance();
    }

    public void initializeFirebaseDB(String db) {
        mMessagesDatabaseReference = mFirebaseDatabase.getReference().child(db);
    }

    public void initializePublicViews() {
        // Initialize references to views
        mProgressBar = (ProgressBar) getActivity().findViewById(R.id.progressBar);
        mMessageListView = (GridView) getActivity().findViewById(R.id.messageListView);
    }

    public void initializeListViewAdapter() {
        // Initialize message ListView and its adapter
        List<StaticFirebaseDataHelper> staticFirebaseData = new ArrayList<>();
        mMessageAdapter = new StaticFirebaseDataAdapter(getContext(), R.layout.item_message, staticFirebaseData);
        mMessageListView.setAdapter(mMessageAdapter);
    }

    public void initializeProgressBar() {
        // Initialize progress bar
        mProgressBar.setVisibility(ProgressBar.INVISIBLE);
    }


}
