package com.coreyjames.runsubclub;

import android.os.Bundle;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;


public class StrengthFragment extends Fragment {

    public StrengthFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View myStrengthWoView = inflater.inflate(R.layout.fragment_strength, container, false);

        BottomNavigationView bottomNavigationView = (BottomNavigationView)
                getActivity().findViewById(R.id.bottom_navigation);

        MenuItem item = bottomNavigationView.getMenu().getItem(2);

        item.setChecked(true);


        return myStrengthWoView;
    }
}

