package com.coreyjames.runsubclub;

import android.os.Bundle;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.WindowManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    private Fragment fragment;
    private FragmentManager fragmentManager;
    private DatabaseReference mCompletionStatusReference;
    private DatabaseReference mCompletionStatusReferenceUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // remove app bar
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        // hide the whole damn action bar
        getSupportActionBar().hide();
        setContentView(R.layout.activity_main);




        fragmentManager = getSupportFragmentManager();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            fragment = new NewOnBaordFragment();
            final FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.replace(R.id.main_container, fragment).commit();
        } else {
            fragment = new CalendarFragment();
            final FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.replace(R.id.main_container, fragment).commit();
        }



        BottomNavigationView bottomNavigationView = (BottomNavigationView)
                findViewById(R.id.bottom_navigation);

//        View view = bottomNavigationView.findViewById(R.id.action_calendar);
//        view.performClick();

        bottomNavigationView.setOnNavigationItemSelectedListener(


                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.action_calendar:

                                fragment = new CalendarFragment();

                                break;
                            case R.id.action_run:
                                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                                mCompletionStatusReferenceUser = FirebaseDatabase.getInstance().getReference("users")
                                        .child(user.getUid()).child("activePlan");

                                mCompletionStatusReferenceUser.orderByChild("completionStatus").limitToFirst(1).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {

                                        for (DataSnapshot messageSnapshot : dataSnapshot.getChildren()) {

                                            Bundle bundle = new Bundle();
                                            String dbRef = messageSnapshot.getKey();
                                            bundle.putString("runKey", dbRef);
                                            fragment = new RunFragment();
                                            fragment.setArguments(bundle);
                                            final FragmentTransaction transaction = fragmentManager.beginTransaction();
                                            transaction.replace(R.id.main_container, fragment, "RunView").addToBackStack( "tag" ).commit();
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });

                                break;

                            case R.id.action_strength:

                                fragment = new StrengthFragment();
                                break;
                            case R.id.action_plans:
                                fragment = new PlansFragment();
                                break;
                        }
                        final FragmentTransaction transaction = fragmentManager.beginTransaction();
                        transaction.replace(R.id.main_container, fragment).addToBackStack( "tag" ).commit();
                        return true;
                    }
                });




    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}

