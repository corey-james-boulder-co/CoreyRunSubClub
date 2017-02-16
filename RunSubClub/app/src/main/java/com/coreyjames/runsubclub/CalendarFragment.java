package com.coreyjames.runsubclub;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
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

//import com.firebase.ui.auth.AuthUI;
//import com.google.firebase.auth.FirebaseAuth;
//import com.google.firebase.auth.FirebaseUser;
//import com.google.firebase.database.ChildEventListener;
//import com.google.firebase.database.DataSnapshot;
//import com.google.firebase.database.DatabaseError;
//import com.google.firebase.database.DatabaseReference;
//import com.google.firebase.database.FirebaseDatabase;
//import com.google.firebase.database.ValueEventListener;

public class CalendarFragment extends Fragment {


    public CalendarFragment() {
    }


    public static final String ANONYMOUS = "anonymous";

    public static final int RC_SIGN_IN = 1;

    private GridView mMessageListView;
    private StaticFirebaseDataAdapter mMessageAdapter;
    private UserFirebaseDataAdapter mMessageAdapterUser;
    private ProgressBar mProgressBar;

    private String mUsername;

    // Firebase instance variables
    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseDatabase mFirebaseDatabaseGeneral;
    private DatabaseReference mMessagesDatabaseReference;
    private DatabaseReference mMessagesDatabaseReferenceGeneral;
    private ChildEventListener mChildEventListener;
    private ChildEventListener mChildEventListenerUser;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private DatabaseReference mUserDatabaseReference;
    private DatabaseReference mUserIdDatabaseReference;

    private Fragment fragment;
    private FragmentManager fragmentManager;

    public String userId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View myCalendarView = inflater.inflate(R.layout.fragment_calendar, container, false);

        initializeFirebaseComponents();

        initializeFirebaseDB();

        initializePublicViews();

        initializeListViewAdapter();

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
            }
        };

        initializeProgressBar();

        fragmentManager = getFragmentManager();

        // Setup the item click listener
        mMessageListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {

                position = position + 1;

                String neat;

                if (position < 10) {
                    neat = "trainingDay0" + Integer.toString(position);
                } else {
                    neat = "trainingDay" + Integer.toString(position);
                }

                Bundle bundle = new Bundle();
                String dbRef = neat;

                bundle.putString("runKey", dbRef);
                fragment = new RunFragment();
                fragment.setArguments(bundle);
                final FragmentTransaction transaction = fragmentManager.beginTransaction();
                transaction.replace(R.id.main_container, fragment, "RunView").addToBackStack( "tag" ).commit();

            }
        });

        return myCalendarView;
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

                public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                }

                public void onChildRemoved(DataSnapshot dataSnapshot) {
                }

                public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                }

                public void onCancelled(DatabaseError databaseError) {
                }
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

    public void initializeFirebaseDB() {
        mMessagesDatabaseReference = mFirebaseDatabase.getReference().child("users")
                .child(initializeUserID()).child("activePlan");
    }

    public void initializeFirebaseGeneral() {
        mMessagesDatabaseReferenceGeneral = mFirebaseDatabaseGeneral.getReference().child("users");
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

    public String initializeUserID() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        System.out.println("YO YO YO " + user.getUid());
        if (user == null) {
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
        return userId = user.getUid();

    }




}
