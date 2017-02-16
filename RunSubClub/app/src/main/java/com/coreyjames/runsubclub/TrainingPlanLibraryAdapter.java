package com.coreyjames.runsubclub;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by csteimel on 2/7/17.
 */

public class TrainingPlanLibraryAdapter extends ArrayAdapter<TrainingPlanLibraryHelper> {
    public TrainingPlanLibraryAdapter(Context context, int resource, List<TrainingPlanLibraryHelper> objects) {
        super(context, resource, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = ((Activity) getContext()).getLayoutInflater().inflate(
                    R.layout.item_training_plan_library, parent, false);
        }

        TextView trainingPlanListView = (TextView) convertView.findViewById(R.id.trainingPlanLibraryListViewText);

        TrainingPlanLibraryHelper message = getItem(position);

        trainingPlanListView.setText(message.getTrainingPlanTitle());

        return convertView;
    }

}