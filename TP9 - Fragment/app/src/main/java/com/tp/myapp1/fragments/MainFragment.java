package com.tp.myapp1.fragments;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.tp.myapp1.MainActivity;
import com.tp.myapp1.R;


public class MainFragment extends Fragment implements View.OnClickListener {

    private static final String LOG_TAG = "MainFragment";

    private Button m_nextButton = null;

    private MainActivity m_parent;

    private boolean m_isTablet;
    private boolean m_isPortrait;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onAttach(Activity activity)
    {
        Log.v(LOG_TAG, ">onAttach");
        super.onAttach(activity);

        m_parent = (MainActivity) activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View fragmentView = inflater.inflate(R.layout.main_fragment, container, false);
        setHasOptionsMenu(false);


        m_nextButton = (Button)fragmentView.findViewById(R.id.next);
        m_nextButton.setOnClickListener(this);

        return fragmentView;
    }

    @Override
    public void onResume() {
        super.onResume();

        m_isTablet = getResources().getBoolean(R.bool.isTablet);
        m_isPortrait = getResources().getBoolean(R.bool.isPortrait);

        if( m_isTablet && !m_isPortrait)
            m_nextButton.setVisibility(View.VISIBLE);
        else
            m_nextButton.setVisibility(View.GONE);
    }


    @Override
    public void onClick(View view) {
        if( view == m_nextButton) {
            m_parent.switchFragmentMode();
        }
    }

}
