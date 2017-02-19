package com.coreyjames.runsubclub;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.coreyjames.runsubclub.util.IabBroadcastReceiver.IabBroadcastListener;
import com.coreyjames.runsubclub.util.IabHelper;
import com.coreyjames.runsubclub.util.IabHelper.IabAsyncInProgressException;
import com.coreyjames.runsubclub.util.IabResult;
import com.coreyjames.runsubclub.util.Inventory;
import com.coreyjames.runsubclub.util.Purchase;
import com.coreyjames.runsubclub.util.IabBroadcastReceiver;

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


public class PlansFragment extends Fragment implements IabBroadcastListener,
        OnClickListener {

    // The helper object
    IabHelper mHelper;

    // Provides purchase notification while this app is running
    IabBroadcastReceiver mBroadcastReceiver;

    public static final String ANONYMOUS = "anonymous";
    public static final int RC_SIGN_IN = 1;
    private ListView mTrainingPlanLibraryListView;
    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseAuth mFirebaseAuth;
    private DatabaseReference mTrainingPlanLibraryDatabaseReference;
    private ChildEventListener mChildEventListener;
    private TrainingPlanLibraryAdapter mTrainingPlanLibraryAdapter;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private String mUsername;
    private DatabaseReference mUserIdDatabaseReference;



    // Does the user have the premium upgrade?
    boolean mIsPremium = false;

    // Does the user have an active subscription to the infinite gas plan?
    boolean mSubscribedToInfiniteGas = false;

    // Will the subscription auto-renew?
    boolean mAutoRenewEnabled = false;

    // Tracks the currently owned infinite gas SKU, and the options in the Manage dialog
    String mInfiniteGasSku = "";
    String mFirstChoiceSku = "";
    String mSecondChoiceSku = "";

    // Used to select between purchasing gas on a monthly or yearly basis
    String mSelectedSubscriptionPeriod = "";

    // SKUs for our products: the premium upgrade (non-consumable) and gas (consumable)
    static final String SKU_PREMIUM = "premium";
    static final String SKU_NEW_PLAN = "newplan";

    // SKU for our subscription (infinite gas)
    static final String SKU_INFINITE_GAS_MONTHLY = "infinite_gas_monthly";
    static final String SKU_INFINITE_GAS_YEARLY = "infinite_gas_yearly";

    // (arbitrary) request code for the purchase flow
    static final int RC_REQUEST = 10001;

//    // Graphics for the gas gauge
//    static int[] TANK_RES_IDS = { R.drawable.gas0, R.drawable.gas1, R.drawable.gas2,
//            R.drawable.gas3, R.drawable.gas4 };

    // How many units (1/4 tank is our unit) fill in the tank.
    static final int TANK_MAX = 4;

    // Current amount of gas in tank, in units
    int mTank;

    public PlansFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        /* base64EncodedPublicKey should be YOUR APPLICATION'S PUBLIC KEY
         * (that you got from the Google Play developer console). This is not your
         * developer public key, it's the *app-specific* public key.
         *
         * Instead of just storing the entire literal string here embedded in the
         * program,  construct the key at runtime from pieces or
         * use bit manipulation (for example, XOR with some other string) to hide
         * the actual key.  The key itself is not secret information, but we don't
         * want to make it easy for an attacker to replace the public key with one
         * of their own and then fake messages from the server.
         */
        String base64EncodedPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAmmDGuV0J6n7KjC1Y02wuFk/x6ZrqBWECd8Mphx/AiEGBDUl5dpcgK/jz97Vq9srWf2vflwqu6G5g+11d+aJyH/oZVenOK4o7aaQUCFkRq08kno4buzgU1bHAP7/ciqyvDuw+COlAiMrwxhNr9DH5A/b5kbtn+Y/qBgUXdS2iIuSTYxUeFrlVPMq1XpCOr3D69g30rioLMhKJHSpKGi+EeGvYefEgP4NimEAwLOYk6a16v8lbbnvX6KCtorbhMBqI+1I7MMXLeclDY9pp6DFCuaJSLL59o+qNZL/wV3UvP3Jt1eCbIQvYVe5b47gb2UiSx3feWaSPigPvg5GHbErtEwIDAQAB";

        // Create the helper, passing it our context and the public key to verify signatures with
        Log.d("YO!", "Creating IAB helper.");
        mHelper = new IabHelper(getActivity(), base64EncodedPublicKey);

        // enable debug logging (for a production application, you should set this to false).
        mHelper.enableDebugLogging(true);

        // Start setup. This is asynchronous and the specified listener
        // will be called once setup completes.
        Log.d("Yo!", "Starting setup.");
        mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            public void onIabSetupFinished(IabResult result) {
                Log.d("Yo!", "Setup finished.");

                if (!result.isSuccess()) {
                    // Oh noes, there was a problem.
                    complain("Problem setting up in-app billing: " + result);
                    return;
                }

                // Have we been disposed of in the meantime? If so, quit.
                if (mHelper == null) return;

                // Important: Dynamically register for broadcast messages about updated purchases.
                // We register the receiver here instead of as a <receiver> in the Manifest
                // because we always call getPurchases() at startup, so therefore we can ignore
                // any broadcasts sent while the app isn't running.
                // Note: registering this listener in an Activity is a bad idea, but is done here
                // because this is a SAMPLE. Regardless, the receiver must be registered after
                // IabHelper is setup, but before first call to getPurchases().
                mBroadcastReceiver = new IabBroadcastReceiver(PlansFragment.this);
                IntentFilter broadcastFilter = new IntentFilter(IabBroadcastReceiver.ACTION);
                getActivity().registerReceiver(mBroadcastReceiver, broadcastFilter);

                // IAB is fully set up. Now, let's get an inventory of stuff we own.
                Log.d("Yo!", "Setup successful. Querying inventory.");
                try {
                    mHelper.queryInventoryAsync(mGotInventoryListener);
                } catch (IabAsyncInProgressException e) {
                    complain("Error querying inventory. Another async operation in progress.");
                }
            }
        });


        View mTrainigPlanLibraryView = inflater.inflate(R.layout.fragment_plans, container, false);

        BottomNavigationView bottomNavigationView = (BottomNavigationView)
                getActivity().findViewById(R.id.bottom_navigation);

        MenuItem item = bottomNavigationView.getMenu().getItem(3);

        item.setChecked(true);

        mTrainingPlanLibraryListView = (ListView) mTrainigPlanLibraryView.findViewById(R.id.trainingPlanLibraryListView);

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

        initializeDatabase();

        initializeListViewAdapter();

        onClickCopyCurrentPlan();

        return mTrainigPlanLibraryView;
    }

    public void initializeFirebaseComponents() {
        // Initialize Firebase components
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mFirebaseAuth = FirebaseAuth.getInstance();
    }

    public void initializeDatabase() {
        mTrainingPlanLibraryDatabaseReference = mFirebaseDatabase.getReference()
                .child("trainingPlanLibrary").child("metaData");
    }

    public void initializeListViewAdapter() {
        // Initialize message ListView and its adapter
        List<TrainingPlanLibraryHelper> trainingPlans = new ArrayList<>();
        mTrainingPlanLibraryAdapter = new TrainingPlanLibraryAdapter
                (getContext(), R.layout.item_training_plan_library, trainingPlans);
        mTrainingPlanLibraryListView.setAdapter(mTrainingPlanLibraryAdapter);
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
        mTrainingPlanLibraryAdapter.clear();
        detachDatabaseReadListener();
    }

    public void attachDatabaseReadListener() {
        if (mChildEventListener == null) {
            mChildEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    TrainingPlanLibraryHelper friendlyMessage = dataSnapshot.getValue(TrainingPlanLibraryHelper.class);
                    mTrainingPlanLibraryAdapter.add(friendlyMessage);
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
            mTrainingPlanLibraryDatabaseReference.addChildEventListener(mChildEventListener);
        }
    }

    public void detachDatabaseReadListener() {
        if (mChildEventListener != null) {
            mTrainingPlanLibraryDatabaseReference.removeEventListener(mChildEventListener);
            mChildEventListener = null;
        }
    }

    public void onClickCopyCurrentPlan() {

        /*
        FIRST COPY THE ACTIVE PLAN TO A NODE UNDER THE USER .. TO SAVE A COPY
         */
        mTrainingPlanLibraryListView.setOnItemClickListener
                (new AdapterView.OnItemClickListener() {

                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {

                        Boolean didBuy = null;
                        if (position == 1) {
                            didBuy = buyNewPlanGooglePlay();
                        }

                        final FirebaseDatabase database = FirebaseDatabase.getInstance();
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        final String userId = user.getUid();

                        /*
                        FIRST KNOW WHAT THE CURRENT PLAN IS
                         */
                        DatabaseReference mRefCurrentPlanName = database.getReference().child("users")
                                .child(userId).child("currentPlanTitle");

                        mRefCurrentPlanName.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                // the changed value is going to be stored into the dataSnapshot
                                // to retrieve value from dataSnapshot we write

                                final String currentPlanTitle = (String) dataSnapshot.getValue();

                                /*
                        Second COPY ACTIVE PLAN TO A A NODE UNDER USERS
                         */


                                DatabaseReference mRefCurrentActiveData = database.getReference().child("users")
                                        .child(userId).child("activePlan");

                                mRefCurrentActiveData.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {

                                        // the changed value is going to be stored into the dataSnapshot
                                        // to retrieve value from dataSnapshot we write

                                        DatabaseReference mRefOldPlan = database.getReference().child("users")
                                                .child(userId).child(currentPlanTitle);

                                        Object value = dataSnapshot.getValue();
                                        mRefOldPlan.setValue(value);

                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

                        /*
                        SECOND ADD AN ALERT THAT SAYS HEY ARE YOU SURE YOU WANNA LOAD THIS PLAN
                        ON CLICK IF YES LOAD SELECTED PLAN FROM STATIC DATA TO ACTIVE PLAN UNDER USER NODE
                        THEN ADD ANOTHER ALERT THAT SAYS THE PLAN WAS ADDED SUCCESSFULLY AND ON OK CLICK
                        UPDATE THE CURRENT PLAN TITLE
                        ON CLICK NO JUST DISMISS THE MESSAGES
                         */

                        if (position == 1 && didBuy != true) {
                            return;
                        } else if (position == 0) {
                            confirmLoadPlanDiaglog(position);
                        } else if (position == 1 && didBuy == true){
                            confirmLoadPlanDiaglog(position);
                        } else {
                            return;
                        }


                    }
                });

    }

    public void confirmPlanLoad(int position) {

        final String trainingPlanName;
        switch (position) {
            case 0:
                trainingPlanName = "basicPlan";
                break;
            case 1:
                trainingPlanName = "newPlan";
                break;
            default:
                trainingPlanName = "basicPlan";
                break;
        }

        final FirebaseDatabase database = FirebaseDatabase.getInstance();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String userId = user.getUid();

        // but first delete the node

        DatabaseReference mRefDeleteNode = database.getReference().child("users").child(userId)
                .child("activePlan");
        mRefDeleteNode.removeValue();
                         /*
                        Just push the whole java object to the active plan .. its gonna be fine
                         */

        DatabaseReference mRefPlanData = database.getReference()
                .child("trainingPlanLibrary").child(trainingPlanName);

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

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }


    /**
     * Prompt the user to confirm that they want to load the plan
     */
    private void confirmLoadPlanDiaglog(final int position) {

            // Create an AlertDialog.Builder and set the message, and click listeners
            // for the postivie and negative buttons on the dialog.
            AlertDialog.Builder builder = new AlertDialog.Builder(this.getContext());
            builder.setMessage(R.string.confirm_load_plan);
            builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {

                    // User clicked the "OK" button, so load plan.
                    confirmPlanLoad(position);
                    updateCurrentPlanTitleDialog(position);
                }
            });
            builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // User clicked the "Cancel" button, so dismiss the dialog
                    // and continue editing the pet.
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                }
            });

            // Create and show the AlertDialog
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
    }

    private void updateCurrentPlanTitleDialog(final int position) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this.getContext());
        builder.setMessage(R.string.plan_loaded_confirmation);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "OK" button, so load plan.
                updateCurrentPlanTitle(position);
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();

    }

    public void updateCurrentPlanTitle(final int position) {

        String trainingPlanName;
        switch (position) {
            case 0:
                trainingPlanName = "basicPlan";
                break;
            case 1:
                trainingPlanName = "newPlan";
                break;
            default:
                trainingPlanName = "basicPlan";
                break;
        }

        FirebaseDatabase database = FirebaseDatabase.getInstance();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String userId = user.getUid();

        DatabaseReference mRefCurrentPlanName = database.getReference().child("users")
                .child(userId).child("currentPlanTitle");

        mRefCurrentPlanName.setValue(trainingPlanName);

    }

    void complain(String message) {
        Log.e("Yo!", "**** TrivialDrive Error: " + message);
        alert("Error: " + message);
    }

    void alert(String message) {
        AlertDialog.Builder bld = new AlertDialog.Builder(getContext());
        bld.setMessage(message);
        bld.setNeutralButton("OK", null);
        Log.d("Yo!", "Showing alert dialog: " + message);
        bld.create().show();
    }

    // Listener that's called when we finish querying the items and subscriptions we own
    IabHelper.QueryInventoryFinishedListener mGotInventoryListener = new IabHelper.QueryInventoryFinishedListener() {
        public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
            Log.d("YO!", "Query inventory finished.");

            // Have we been disposed of in the meantime? If so, quit.
            if (mHelper == null) return;

            // Is it a failure?
            if (result.isFailure()) {
                complain("Failed to query inventory: " + result);
                return;
            }

            Log.d("Yo!", "Query inventory was successful.");

            /*
             * Check for items we own. Notice that for each purchase, we check
             * the developer payload to see if it's correct! See
             * verifyDeveloperPayload().
             */

            // Do we have the premium upgrade?
            Purchase premiumPurchase = inventory.getPurchase(SKU_PREMIUM);
            mIsPremium = (premiumPurchase != null && verifyDeveloperPayload(premiumPurchase));
            Log.d("Yo!", "User is " + (mIsPremium ? "PREMIUM" : "NOT PREMIUM"));

            // First find out which subscription is auto renewing
            Purchase gasMonthly = inventory.getPurchase(SKU_INFINITE_GAS_MONTHLY);
            Purchase gasYearly = inventory.getPurchase(SKU_INFINITE_GAS_YEARLY);
            if (gasMonthly != null && gasMonthly.isAutoRenewing()) {
                mInfiniteGasSku = SKU_INFINITE_GAS_MONTHLY;
                mAutoRenewEnabled = true;
            } else if (gasYearly != null && gasYearly.isAutoRenewing()) {
                mInfiniteGasSku = SKU_INFINITE_GAS_YEARLY;
                mAutoRenewEnabled = true;
            } else {
                mInfiniteGasSku = "";
                mAutoRenewEnabled = false;
            }

            // The user is subscribed if either subscription exists, even if neither is auto
            // renewing
            mSubscribedToInfiniteGas = (gasMonthly != null && verifyDeveloperPayload(gasMonthly))
                    || (gasYearly != null && verifyDeveloperPayload(gasYearly));
            Log.d("Yo!", "User " + (mSubscribedToInfiniteGas ? "HAS" : "DOES NOT HAVE")
                    + " infinite gas subscription.");
            if (mSubscribedToInfiniteGas) mTank = TANK_MAX;

            // Check for gas delivery -- if we own gas, we should fill up the tank immediately
            Purchase gasPurchase = inventory.getPurchase(SKU_NEW_PLAN);
            if (gasPurchase != null && verifyDeveloperPayload(gasPurchase)) {
                Log.d("Yo!", "We have gas. Consuming it.");
                try {
                    mHelper.consumeAsync(inventory.getPurchase(SKU_NEW_PLAN), mConsumeFinishedListener);
                } catch (IabAsyncInProgressException e) {
                    complain("Error consuming gas. Another async operation in progress.");
                }
                return;
            }

            Log.d("Yo!", "Initial inventory query finished; enabling main UI.");
        }
    };

    /** Verifies the developer payload of a purchase. */
    boolean verifyDeveloperPayload(Purchase p) {
        String payload = p.getDeveloperPayload();

        /*
         * TODO: verify that the developer payload of the purchase is correct. It will be
         * the same one that you sent when initiating the purchase.
         *
         * WARNING: Locally generating a random string when starting a purchase and
         * verifying it here might seem like a good approach, but this will fail in the
         * case where the user purchases an item on one device and then uses your app on
         * a different device, because on the other device you will not have access to the
         * random string you originally generated.
         *
         * So a good developer payload has these characteristics:
         *
         * 1. If two different users purchase an item, the payload is different between them,
         *    so that one user's purchase can't be replayed to another user.
         *
         * 2. The payload must be such that you can verify it even when the app wasn't the
         *    one who initiated the purchase flow (so that items purchased by the user on
         *    one device work on other devices owned by the user).
         *
         * Using your own server to store and verify developer payloads across app
         * installations is recommended.
         */

        return true;
    }

    // Called when consumption is complete
    IabHelper.OnConsumeFinishedListener mConsumeFinishedListener = new IabHelper.OnConsumeFinishedListener() {
        public void onConsumeFinished(Purchase purchase, IabResult result) {
            Log.d("Yo!", "Consumption finished. Purchase: " + purchase + ", result: " + result);

            // if we were disposed of in the meantime, quit.
            if (mHelper == null) return;

            // We know this is the "gas" sku because it's the only one we consume,
            // so we don't check which sku was consumed. If you have more than one
            // sku, you probably should check...
            if (result.isSuccess()) {
                // successfully consumed, so we apply the effects of the item in our
                // game world's logic, which in our case means filling the gas tank a bit
                Log.d("Yo!", "Consumption successful. Provisioning.");
                mTank = mTank == TANK_MAX ? TANK_MAX : mTank + 1;
                alert("You filled 1/4 tank. Your tank is now " + String.valueOf(mTank) + "/4 full!");
            }
            else {
                complain("Error while consuming: " + result);
            }

            Log.d("Yo!", "End consumption flow.");
        }
    };


    @Override
    public void onClick(DialogInterface dialog, int which) {

    }

    @Override
    public void receivedBroadcast() {

    }

    public Boolean buyNewPlanGooglePlay() {
        // launch the gas purchase UI flow.
        // We will be notified of completion via mPurchaseFinishedListener
        setWaitScreen(true);
        Log.d("Yo!", "Launching purchase flow for gas.");

        /* TODO: for security, generate your payload here for verification. See the comments on
         *        verifyDeveloperPayload() for more info. Since this is a SAMPLE, we just use
         *        an empty string, but on a production app you should carefully generate this. */
        String payload = "";

        try {
            mHelper.launchPurchaseFlow(getActivity(), SKU_NEW_PLAN, RC_REQUEST,
                    mPurchaseFinishedListener, payload);
            return true;
        } catch (IabAsyncInProgressException e) {
            complain("Error launching purchase flow. Another async operation in progress.");
            setWaitScreen(false);
            return false;
        }
    }

    // Enables or disables the "please wait" screen.
    void setWaitScreen(boolean set) {
        getActivity().findViewById(R.id.trainingPlanLibraryListView).setVisibility(set ? View.GONE : View.VISIBLE);
        getActivity().findViewById(R.id.screen_wait).setVisibility(set ? View.VISIBLE : View.GONE);
    }

    // Callback for when a purchase is finished
    IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
        public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
            Log.d("Yo!", "Purchase finished: " + result + ", purchase: " + purchase);

            // if we were disposed of in the meantime, quit.
            if (mHelper == null) return;

            if (result.isFailure()) {
                complain("Error purchasing: " + result);
                setWaitScreen(false);
                return;
            }
            if (!verifyDeveloperPayload(purchase)) {
                complain("Error purchasing. Authenticity verification failed.");
                setWaitScreen(false);
                return;
            }

            Log.d("Yo!", "Purchase successful.");

            if (purchase.getSku().equals(SKU_NEW_PLAN)) {
                // bought 1/4 tank of gas. So consume it.
                Log.d("Yo!", "Purchase is gas. Starting gas consumption.");
                try {
                    mHelper.consumeAsync(purchase, mConsumeFinishedListener);
                } catch (IabAsyncInProgressException e) {
                    complain("Error consuming gas. Another async operation in progress.");
                    setWaitScreen(false);
                    return;
                }
            }
            else if (purchase.getSku().equals(SKU_PREMIUM)) {
                // bought the premium upgrade!
                Log.d("Yo!", "Purchase is premium upgrade. Congratulating user.");
                alert("Thank you for upgrading to premium!");
                mIsPremium = true;
//                updateUi();
                setWaitScreen(false);
            }
            else if (purchase.getSku().equals(SKU_INFINITE_GAS_MONTHLY)
                    || purchase.getSku().equals(SKU_INFINITE_GAS_YEARLY)) {
                // bought the infinite gas subscription
                Log.d("Yo!", "Infinite gas subscription purchased.");
                alert("Thank you for subscribing to infinite gas!");
                mSubscribedToInfiniteGas = true;
                mAutoRenewEnabled = purchase.isAutoRenewing();
                mInfiniteGasSku = purchase.getSku();
                mTank = TANK_MAX;
//                updateUi();
                setWaitScreen(false);
            }
        }
    };
}
