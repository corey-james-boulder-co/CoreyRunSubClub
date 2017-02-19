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
                        String fragmentTag = "default";
                        switch (item.getItemId()) {
                            case R.id.action_calendar:

                                fragment = new CalendarFragment();
                                fragmentTag = "calendarFragment";

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

                                fragmentTag = "runFragment";

                                break;

                            case R.id.action_strength:

                                fragment = new StrengthFragment();
                                fragmentTag = "strengthFragment";
                                break;
                            case R.id.action_plans:
                                fragment = new PlansFragment();
                                fragmentTag = "plansFragment";
                                break;
                        }
                        final FragmentTransaction transaction = fragmentManager.beginTransaction();
                        transaction.replace(R.id.main_container, fragment).addToBackStack( fragmentTag ).commit();
                        System.out.println("Hello Rudy " + fragmentTag);
                        return true;
                    }
                });




    }

    @Override
    public void onBackPressed() {

//        String id = getApplicationContext().getTag();
//
//        List list = fragmentManager.();
//
//        getSupportFragmentManager().tag
//
//        Context ok = getApplication().getApplicationContext();
//
//        Context lilGuy = this.fragment.getT();
//
//        System.out.println("the lil guy tag is " + id);

//        try {
//            switch (pos) {
//                case 0: // Means Main Activity : Do Default action
//                    super.onBackPressed();
//                    break;
//                case 1:
//                case 2:
//                case 3://Other than main tab open previous positioned tab.
//                    viewPager.setCurrentItem(pos, true);
//                    break;
//            }
//        } catch (Exception e) {
//            super.onBackPressed();
//        }


//
//        int index = getFragmentManager().getBackStackEntryCount() - 1;
//        FragmentManager.BackStackEntry backEntry = (FragmentManager.BackStackEntry) getFragmentManager().getBackStackEntryAt(index);
//        String tag = backEntry.getName();
//        android.app.Fragment fragment = getFragmentManager().findFragmentByTag(tag);
//
//
//        System.out.println("HELLO RUDY " + fragment);
//
//        List fragList = getSupportFragmentManager().getFragments();
//
//        int listSize = fragList.size();
//
//        if (listSize > 1) {
//
//            int fragGet = listSize - 2;
//
//            Fragment fragment = (Fragment) fragList.get(fragGet);
//
//            String fragName = fragment.toString();
//
//            String[] seperated = fragName.split("Fragment");
//
//            int i = 0;
//
//            if (seperated[0] != null) {
//                switch (seperated[0]) {
//                    case "Calendar": i = 0;
//                        break;
//                    case "Run": i = 1;
//                        break;
//                    case "Strength": i = 2;
//                        break;
//                    case "Plans": i = 3;
//                        break;
//                }
//                BottomNavigationView bottomNavigationView = (BottomNavigationView)
//                        findViewById(R.id.bottom_navigation);
//
//                MenuItem item = bottomNavigationView.getMenu().getItem(i);
//
//                item.setChecked(true);
//
//                System.out.println("BILLY BILLY BILLY .. The current frag list is" + fragList + " this list contains this many: "
//                        + listSize + " frag NAME is " + fragName  + " what is i "
//                        + i + " the seperated value is " + seperated[0] +
//                        " what is the item?? " + item);
//            }
//
//        }


        super.onBackPressed();

//        getSupportFragmentManager().popBackStack();


    }
}

