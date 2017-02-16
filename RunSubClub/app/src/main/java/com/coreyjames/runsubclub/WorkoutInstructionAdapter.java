package com.coreyjames.runsubclub;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by csteimel on 2/3/17.
 */

public class WorkoutInstructionAdapter extends ArrayAdapter<StaticFirebaseDataHelper> {
    public WorkoutInstructionAdapter(Context context, int resource, List<StaticFirebaseDataHelper> objects) {
        super(context, resource, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = ((Activity) getContext()).getLayoutInflater().inflate(
                    R.layout.item_workout_instruction, parent, false);
        }

        TextView workoutListViewText = (TextView) convertView.findViewById(R.id.workoutListViewText);

        StaticFirebaseDataHelper message = getItem(position);

        workoutListViewText.setText(message.getWorkoutInstructionText());

        return convertView;
    }

}