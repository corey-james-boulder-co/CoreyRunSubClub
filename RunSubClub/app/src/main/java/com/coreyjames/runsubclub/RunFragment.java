package com.coreyjames.runsubclub;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerSupportFragment;
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

import static com.coreyjames.runsubclub.R.drawable.christmas_star_big;
import static com.coreyjames.runsubclub.R.drawable.star_big;

public class RunFragment extends Fragment {

    public RunFragment() {

    }

    private static final String API_KEY = "AIzaSyBCs4CMAzvVMiwVoGnKXW8kHq4rXhn6Vcw";
    public String VIDEO_ID;

    public String dbRefTrainingDay;

    public static final String ANONYMOUS = "anonymous";

    public static final int RC_SIGN_IN = 1;

    private WorkoutInstructionAdapter mWorkoutInstructionAdapter;

    private String mUsername;

    // Firebase instance variables
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mWorkoutInstructionDatabaseReference;
    private DatabaseReference mFirstVideoDataBaseReference;
    private ChildEventListener mChildEventListener;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private DatabaseReference mSessionTypeReference;
    private DatabaseReference mDurationReference;
    private DatabaseReference mDescriptionReference;
    private DatabaseReference mCompletionStatusReference;
    private DatabaseReference mCompletionStatusReferenceUser;
    private DatabaseReference mCompletionStatusIntReference;
    private DatabaseReference mCompletionStatusIntReferenceUser;



    /** TextView field to read workout Run WO Number */
    private TextView mWorkoutNumberTextView;

    /** ImageView field to read Run WO Session Type Icon*/
    private ImageView mSessionTypeImageView;

    private ImageView mStockPhoto;

    /** ImageView field to read Run WO Session Completion Status Icon*/
    private ImageView mCompletionStatusImageView;

    /** TextView field to read workout Session Type */
    private TextView mSessionTypeTextView;

    /** TextView field to read workout Duration */
    private TextView mWorkoutDateTextView;

    /** TextView field to read workout Duration */
    private TextView mDurationTextView;

    /** TextView field to read workout Description */
    private TextView mDescriptionTextView;

    private ListView mWorkoutListView;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View myRunWoView = inflater.inflate(R.layout.fragment_run, container, false);

        BottomNavigationView bottomNavigationView = (BottomNavigationView)
                getActivity().findViewById(R.id.bottom_navigation);

        MenuItem item = bottomNavigationView.getMenu().getItem(1);

        item.setChecked(true);

        // Set app bar title
        getActivity().setTitle(getString(R.string.run_wo_activity_title_run_wo));

        // Find all relevant views that we will need to read user input from
//        mWorkoutNumberTextView = (TextView) myRunWoView.findViewById(R.id.run_wo_day_number);
        mSessionTypeImageView = (ImageView) myRunWoView.findViewById(R.id.run_wo_session_type_icon);
        mCompletionStatusImageView = (ImageView) myRunWoView.findViewById(R.id.run_wo_completion_status_icon);
        mSessionTypeTextView = (TextView) myRunWoView.findViewById(R.id.run_wo_session_type_text);
        mDurationTextView = (TextView) myRunWoView.findViewById(R.id.run_wo_duration_text);
        mDescriptionTextView = (TextView) myRunWoView.findViewById(R.id.run_wo_description_text);
        mWorkoutListView = (ListView) myRunWoView.findViewById(R.id.workoutListView);
        mStockPhoto = (ImageView) myRunWoView.findViewById(R.id.stock_photo);

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

        initializeFirebaseComponents();

        dbRefTrainingDay = getArguments().getString("runKey");

        initializeDatabase(dbRefTrainingDay);

        doYouTubeOrDoImage(dbRefTrainingDay);

        initializeListViewAdapter();

        initializeCompletionStatusDatabaseForUser(dbRefTrainingDay);

        setSessionTypeTextView();
        setDurationTextView();
        setDescriptionTextView();
        setSessionTypeImageView();
        setCompletionStatusImageView();

        tapStar();

        return myRunWoView;
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
        detachDatabaseReadListener();
    }

    public void onSignedInInitialize(String username) {
        mUsername = username;
        attachDatabaseReadListener();
    }

    public void onSignedOutCleanup() {
        mUsername = ANONYMOUS;
        mWorkoutInstructionAdapter.clear();
        detachDatabaseReadListener();
    }

    public void attachDatabaseReadListener() {
        if (mChildEventListener == null) {
            mChildEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    StaticFirebaseDataHelper friendlyMessage = dataSnapshot.getValue(StaticFirebaseDataHelper.class);
                    mWorkoutInstructionAdapter.add(friendlyMessage);
                }

                public void onChildChanged(DataSnapshot dataSnapshot, String s) {}
                public void onChildRemoved(DataSnapshot dataSnapshot) {}
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {}
                public void onCancelled(DatabaseError databaseError) {}
            };
            mWorkoutInstructionDatabaseReference.addChildEventListener(mChildEventListener);
        }
    }

    public void detachDatabaseReadListener() {
        if (mChildEventListener != null) {
            mWorkoutInstructionDatabaseReference.removeEventListener(mChildEventListener);
            mChildEventListener = null;
        }
    }

    public void initializeFirebaseComponents() {
        // Initialize Firebase components
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mFirebaseAuth = FirebaseAuth.getInstance();
    }

    public void initializeDatabase(String childOne) {
        // Initialize Database
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        mSessionTypeReference = FirebaseDatabase.getInstance().getReference()
                .child("users").child(user.getUid()).child("activePlan").child(childOne).child("sessionType");
        mDurationReference = FirebaseDatabase.getInstance().getReference()
                .child("users").child(user.getUid()).child("activePlan").child(childOne).child("duration");
        mDescriptionReference = FirebaseDatabase.getInstance().getReference()
                .child("users").child(user.getUid()).child("activePlan").child(childOne).child("description");
        mWorkoutInstructionDatabaseReference = FirebaseDatabase.getInstance().getReference()
                .child("users").child(user.getUid()).child("activePlan").child(childOne).child("workoutInstructionData");
        mFirstVideoDataBaseReference = FirebaseDatabase.getInstance().getReference()
                .child("users").child(user.getUid()).child("activePlan").child(childOne).child("video");

    }

    public void initializeCompletionStatusDatabaseForUser(String childOne) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        mCompletionStatusReferenceUser = FirebaseDatabase.getInstance().getReference()
                .child("users").child(user.getUid()).child("activePlan").child(childOne).child("completionStatusText");
        mCompletionStatusIntReferenceUser = FirebaseDatabase.getInstance().getReference()
                .child("users").child(user.getUid()).child("activePlan").child(childOne).child("completionStatus");
    }

    public void setSessionTypeTextView() {
        // In the database whenever any data is changed then the below code snipped is going to be executed.
        mSessionTypeReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                // the changed value is going to be stored into the dataSnapshot
                // to retrieve value from dataSnapshot we write

                String value = (String) dataSnapshot.getValue();
                mSessionTypeTextView.setText(value);
                Log.wtf("THE VAULE IS ... ", value);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void setDurationTextView() {
        // In the database whenever any data is changed then the below code snipped is going to be executed.
        mDurationReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                // the changed value is going to be stored into the dataSnapshot
                // to retrieve value from dataSnapshot we write

                String value = (String) dataSnapshot.getValue();
                mDurationTextView.setText(value);
                Log.wtf("THE VAULE IS ... ", value);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void setDescriptionTextView() {
        // In the database whenever any data is changed then the below code snipped is going to be executed.
        mDescriptionReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                // the changed value is going to be stored into the dataSnapshot
                // to retrieve value from dataSnapshot we write

                String value = (String) dataSnapshot.getValue();
                mDescriptionTextView.setText(value);
                Log.wtf("THE VAULE IS ... ", value);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void setSessionTypeImageView() {
        // In the database whenever any data is changed then the below code snipped is going to be executed.
        mSessionTypeReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                // the changed value is going to be stored into the dataSnapshot
                // to retrieve value from dataSnapshot we write

                String value = (String) dataSnapshot.getValue();
                switch (value) {
                    case "aerobic":
                        mSessionTypeImageView.setImageResource(R.drawable.heart_with_pulse_big);
                        break;
                    case "speed":
                        mSessionTypeImageView.setImageResource(R.drawable.running_rabbit_big);
                        break;
                    case "endurance":
                        mSessionTypeImageView.setImageResource(R.drawable.sewing_tape_measure_big);
                        break;
                    case "drills":
                        mSessionTypeImageView.setImageResource(R.drawable.maintenance_big);
                        break;
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void setCompletionStatusImageView() {
        // In the database whenever any data is changed then the below code snipped is going to be executed.
        mCompletionStatusReferenceUser.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                // the changed value is going to be stored into the dataSnapshot
                // to retrieve value from dataSnapshot we write

                String value = (String) dataSnapshot.getValue();
                switch (value) {
                    case "incomplete":
                        mCompletionStatusImageView.setImageResource(star_big);
                        break;
                    case "complete":
                        mCompletionStatusImageView.setImageResource(christmas_star_big);
                        break;
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void tapStar() {
        mCompletionStatusImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mCompletionStatusImageView.getDrawable().getConstantState() == getResources().getDrawable(christmas_star_big).getConstantState()) {
                    mCompletionStatusImageView.setImageResource(star_big);
                    mCompletionStatusReferenceUser.setValue("incomplete");
                    mCompletionStatusIntReferenceUser.setValue(0);
                } else {
                    mCompletionStatusImageView.setImageResource(christmas_star_big);
                    mCompletionStatusReferenceUser.setValue("complete");
                    mCompletionStatusIntReferenceUser.setValue(1);
                }
            }
        });
    }

    public void doYouTubeOrDoImage (final String childOne) {

        //First Let's Check if there are videos .. If there are videos then set up YouTube .. If not just show Image
        mFirstVideoDataBaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String value = (String) dataSnapshot.getValue();
                if (value != null) {

                    // set up a bunch of youtube crap here

                    //YOU TUBE SHIT
                    final YouTubePlayerSupportFragment youTubePlayerFragment = YouTubePlayerSupportFragment.newInstance();

                    FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
                    transaction.add(R.id.youtube_layout, youTubePlayerFragment).addToBackStack( "tag" ).commit();

                    youTubePlayerFragment.initialize(API_KEY, new YouTubePlayer.OnInitializedListener() {

                        @Override
                        public void onInitializationSuccess(YouTubePlayer.Provider provider, final YouTubePlayer player, boolean wasRestored) {

                            if (!wasRestored) {
                                player.setPlayerStyle(YouTubePlayer.PlayerStyle.DEFAULT
                                );

                                mFirstVideoDataBaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {

                                        // the changed value is going to be stored into the dataSnapshot
                                        // to retrieve value from dataSnapshot we write

                                        String value = (String) dataSnapshot.getValue();

                                        VIDEO_ID = value;
                                        player.cueVideo(VIDEO_ID);

                                        mWorkoutListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                            @Override
                                            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {

                                                position = position + 1;
                                                final String positionText;
                                                if (position < 10) {
                                                    positionText = "0" + Integer.toString(position);
                                                } else {
                                                    positionText = Integer.toString(position);
                                                }
                                                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                                                String userId = user.getUid();
                                                DatabaseReference mYoutube = FirebaseDatabase.getInstance().getReference()
                                                        .child("users").child(userId)
                                                        .child("activePlan").child(childOne)
                                                        .child("workoutInstructionData").child(positionText)
                                                        .child("workoutInstructionVideo");
                                                mYoutube.addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                                        String value = (String) dataSnapshot.getValue();
                                                        System.out.println("thi is the url " + value + " and this is the position " + positionText);
                                                        if (value != null) {
                                                            VIDEO_ID = value;
                                                            player.loadVideo(VIDEO_ID);
                                                        } else {
                                                            player.cueVideo(VIDEO_ID);
                                                        }
                                                    }
                                                    @Override
                                                    public void onCancelled(DatabaseError databaseError) {}
                                                });
                                            }
                                        });
                                    }
                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {}
                                });
                            }
                        }

                        @Override
                        public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult error) {
                            // YouTube error
                            String errorMessage = error.toString();
                            Toast.makeText(getActivity(), errorMessage, Toast.LENGTH_LONG).show();
                            Log.d("errorMessage:", errorMessage);
                        }
                    });



                } else {

                    // set up the image view here
                    mStockPhoto.setImageResource(christmas_star_big);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }

    public void initializeListViewAdapter() {
        // Initialize message ListView and its adapter
        List<StaticFirebaseDataHelper> friendlyMessages = new ArrayList<>();
        mWorkoutInstructionAdapter = new WorkoutInstructionAdapter
                (getContext(), R.layout.item_workout_instruction, friendlyMessages);
        mWorkoutListView.setAdapter(mWorkoutInstructionAdapter);
    }






//    youTubePlayerFragment.initialize(API_KEY, new OnInitializedListener() {
//
//
//    @Override
//    public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean b) {
//
//        youTubePlayer.setPlaybackEventListener(this);
//        youTubePlayer.setPlayerStateChangeListener(this);
//
//    }
//
//    @Override
//    public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {
//
//    }
//
//    @Override
//    public void onPlaying() {
//
//    }
//
//    @Override
//    public void onPaused() {
//
//    }
//
//    @Override
//    public void onStopped() {
//
//    }
//
//    @Override
//    public void onBuffering(boolean b) {
//
//    }
//
//    @Override
//    public void onSeekTo(int i) {
//
//    }
//
//    @Override
//    public void onLoading() {
//
//    }
//
//    @Override
//    public void onLoaded(String s) {
//
//    }
//
//    @Override
//    public void onAdStarted() {
//
//    }
//
//    @Override
//    public void onVideoStarted() {
//
//    }
//
//    @Override
//    public void onVideoEnded() {
//
//    }
//
//    @Override
//    public void onError(YouTubePlayer.ErrorReason errorReason) {
//
//    }
//
//    });



    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if(keyCode == KeyEvent.KEYCODE_BACK)
        {

            return true;
        }
        return false;
    }
}
