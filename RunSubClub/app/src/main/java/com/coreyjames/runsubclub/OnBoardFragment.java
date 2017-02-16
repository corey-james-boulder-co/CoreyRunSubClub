package com.coreyjames.runsubclub;

/**
 * Created by csteimel on 2/16/17.
 */

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import static android.view.View.GONE;

/**
 * Created by csteimel on 1/6/17.
 */

public class OnBoardFragment extends Fragment {

    public OnBoardFragment(){}

    private FragmentManager fragmentManager;
    private Fragment fragment;

    private Button mSendIt;
    private View bottomNav;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View myOnBoardView = inflater.inflate(R.layout.fragment_on_board, container, false);


        mSendIt = (Button) myOnBoardView.findViewById(R.id.cool_send_it_button);
        bottomNav = getActivity().findViewById(R.id.bottom_navigation);

        bottomNav.setVisibility(GONE);

        tapStar();

        return myOnBoardView;


    }

    public void tapStar() {
        mSendIt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), MainActivity.class);
                startActivity(intent);
            }

        });
    }

}
