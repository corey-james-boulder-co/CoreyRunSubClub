package com.coreyjames.runsubclub;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import static com.coreyjames.runsubclub.R.drawable.christmas_star_big;

/**
 * Created by csteimel on 1/26/17.
 */

public class StaticFirebaseDataAdapter extends ArrayAdapter<StaticFirebaseDataHelper> {
    public StaticFirebaseDataAdapter(Context context, int resource, List<StaticFirebaseDataHelper> objects) {
        super(context, resource, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = ((Activity) getContext()).getLayoutInflater().inflate(R.layout.item_message, parent, false);
        }

        TextView sessionTypeTextView = (TextView) convertView.findViewById(R.id.sessionTypeTextView);

        final ImageView sessionTypeImageView = (ImageView) convertView.findViewById(R.id.run_wo_session_type);

        final StaticFirebaseDataHelper staticData = getItem(position);

        sessionTypeTextView.setVisibility(View.VISIBLE);
        sessionTypeTextView.setText(staticData.getSessionType());


        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        position = position + 1;

        String neat;

        if (position < 10) {
            neat = "trainingDay0"+Integer.toString(position);
        } else {
            neat = "trainingDay"+Integer.toString(position);
        }

        DatabaseReference mCompletionStatusReferenceUser = FirebaseDatabase.getInstance().getReference()
                .child("users").child(user.getUid()).child("activePlan").child(neat).child("completionStatusText");


        mCompletionStatusReferenceUser.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String value = (String) dataSnapshot.getValue();
                switch (value) {
                    case "incomplete":
                        String runWoSessionType = staticData.getSessionType();
                        switch (runWoSessionType) {
                            case "aerobic":
                                sessionTypeImageView.setImageResource(R.drawable.heart_with_pulse_big);
                                break;
                            case "speed":
                                sessionTypeImageView.setImageResource(R.drawable.running_rabbit_big);
                                break;
                            case "endurance":
                                sessionTypeImageView.setImageResource(R.drawable.sewing_tape_measure_big);
                                break;
                            case "drills":
                                sessionTypeImageView.setImageResource(R.drawable.maintenance_big);
                                break;
                        }
                        break;
                    case "complete":
                        sessionTypeImageView.setImageResource(christmas_star_big);
                        break;
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        return convertView;
    }

}

