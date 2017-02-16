package com.coreyjames.runsubclub;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import java.util.List;

/**
 * Created by csteimel on 2/6/17.
 */

public class UserFirebaseDataAdapter extends ArrayAdapter<UserFirebaseDataHelper> {
    public UserFirebaseDataAdapter(Context context, int resource, List<UserFirebaseDataHelper> objects) {
        super(context, resource, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = ((Activity) getContext()).getLayoutInflater().inflate(R.layout.item_message, parent, false);
        }

        ImageView sessionTypeImageView = (ImageView) convertView.findViewById(R.id.run_wo_session_type);

        UserFirebaseDataHelper userData = getItem(position);

        String completionStatusText = userData.getCompletionStatusText();

        Log.wtf("completion Status", completionStatusText);

        if (completionStatusText == "complete") {
            sessionTypeImageView.setImageResource(R.drawable.christmas_star_big);
        }

        return convertView;
    }

}
