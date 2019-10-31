package com.tp.myapp1;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.tp.myapp1.fragments.MainFragment;
import com.tp.myapp1.fragments.SecondFragment;
import com.tp.myapp1.fragments.ThirdFragment;
import com.tp.myapp1.fragments.WelcomeFragment;


public class MainActivity extends AppCompatActivity {
    private static final String LOG_TAG = "HomeTabActivity";


    private View m_hometabLayout;
    private View m_secondFrameLayout;
    private Fragment m_mainFragment;
    private Fragment m_secondFragment;
    private Handler m_handler = new Handler();

    private boolean m_isTablet;
    private boolean m_isPortrait;



    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        m_isTablet = getResources().getBoolean(R.bool.isTablet);
        m_isPortrait = getResources().getBoolean(R.bool.isPortrait);

        if (updateLayout(savedInstanceState))
            return;
    }

    // When an android device changes orientation usually the activity is destroyed and recreated with a new
    // orientation layout. This method, along with a setting in the the manifest for this activity
    // tells the OS to let us handle it instead.
    //
    // This increases performance and gives us greater control over activity creation and destruction for simple
    // activities.
    //
    // Must place this into the AndroidManifest.xml file for this activity in order for this to work properly
    //   android:configChanges="keyboardHidden|orientation"
    //   optionally
    //   android:screenOrientation="landscape"
    @Override
    public void onConfigurationChanged(Configuration newConfig)
    {
        super.onConfigurationChanged(newConfig);

        m_isTablet = getResources().getBoolean(R.bool.isTablet);
        m_isPortrait = getResources().getBoolean(R.bool.isPortrait);

        //showRightFragment();
    }

    private boolean updateLayout(Bundle savedInstanceState) {
        setContentView(R.layout.activity_main);

        m_hometabLayout = findViewById(R.id.home_fragment);
        m_secondFrameLayout = findViewById(R.id.second_fragment);

        //showRightFragment();

        // However, if we're being restored from a previous state,
        // then we don't need to do anything and should return or else
        // we could end up with overlapping fragments.
        if (savedInstanceState != null) {
            // Need to hide/show fragments according to current State

            return true;
        }

        FragmentManager supportFragmentManager = this.getFragmentManager();
        FragmentTransaction fragmentTransaction = supportFragmentManager.beginTransaction();

        // Check that the activity is using the layout version with
        // the fragment_container FrameLayout
        if (m_hometabLayout != null) {
            if( m_mainFragment == null) {
                m_mainFragment = new MainFragment();

                // Add the fragment to the 'fragment_container' FrameLayout
                fragmentTransaction.add(R.id.home_fragment, m_mainFragment);
            }
        }

        if (m_secondFrameLayout != null) {
            // Add the fragment to the 'fragment_container' FrameLayout

            if( m_isTablet) {
                try {
                    m_secondFragment = WelcomeFragment.class.newInstance();
                } catch (Exception e) {
                    Log.e(LOG_TAG, "Exception while instanciating Fragment: "+e.getMessage());
                }

                fragmentTransaction.add(R.id.second_fragment, m_secondFragment);
            }
        }

        fragmentTransaction.commitAllowingStateLoss();

        return false;
    }

    private void popFragmentStack() {
        Log.i(LOG_TAG, ">popFragmentStack");

        if( m_secondFragment != null ) {

            m_handler.post(new Runnable() {
                public void run() {
                    FragmentManager fragmentManager = getFragmentManager();

                    int backStackEntryCount = fragmentManager.getBackStackEntryCount();
                    Log.v(LOG_TAG, "backStackEntryCount="+backStackEntryCount);
                    if (backStackEntryCount > 0) {
                        fragmentManager.popBackStackImmediate();
                        fragmentManager.beginTransaction().commit();

                        //showRightFragment();
                    } else {
                        Log.v(LOG_TAG, " use Back Task");
                        //HomeTabActivity.super.onBackPressed();
                        moveTaskToBack(true);
                    }
                }
            });
        }
    }

    public void showFragment(final Class fragmentClass, final Bundle bundle) {
        Log.i(LOG_TAG, ">showFragment");
        if( fragmentClass == null )
            return;

        m_handler.post(new Runnable() {
            public void run() {
                switchFragmentMode(fragmentClass, bundle);
            }
        });
    }

    public void closeAndShowFragment(final Class fragmentClass, final Bundle bundle) {
        Log.i(LOG_TAG, ">showFragment");
        if( fragmentClass == null )
            return;

        m_handler.post(new Runnable() {
            public void run() {
                onBackPressed();
                switchFragmentMode(fragmentClass, bundle);
            }
        });
    }

    public void switchFragmentMode() {
        if( !(m_isTablet && !m_isPortrait))
            return;

        if( m_secondFragment instanceof WelcomeFragment) {
            try {
                m_secondFragment = SecondFragment.class.newInstance();
            } catch (Exception e) {
                Log.e(LOG_TAG, "Exception while instanciating Fragment: "+e.getMessage());
            }
        } else if( m_secondFragment instanceof SecondFragment) {
            try {
                m_secondFragment = ThirdFragment.class.newInstance();
            } catch (Exception e) {
                Log.e(LOG_TAG, "Exception while instanciating Fragment: "+e.getMessage());
            }
        } else {
            try {
                m_secondFragment = WelcomeFragment.class.newInstance();
            } catch (Exception e) {
                Log.e(LOG_TAG, "Exception while instanciating Fragment: "+e.getMessage());
            }
        }

        FragmentManager fragmentManager = this.getFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();

        // Replace whatever is in the fragment_container view with this fragment,
        // and add the transaction to the back stack so the user can navigate back
        transaction.replace(R.id.second_fragment, m_secondFragment);
        transaction.addToBackStack("frag");

        // Commit the transaction
        transaction.commit();
        fragmentManager.executePendingTransactions();
    }

    public void switchFragmentMode(final Class fragmentClass, final Bundle bundle) {
        Log.i(LOG_TAG, ">switchFragmentMode");

        FragmentManager fragmentManager = this.getFragmentManager();
        int backStackEntryCount = fragmentManager.getBackStackEntryCount();
        if (backStackEntryCount > 0) {
            Fragment currentFrag = fragmentManager.findFragmentById(R.id.second_fragment);

            if (currentFrag.getClass().equals(fragmentClass)) {
                popFragmentStack();
            }
        }

        m_handler.post(new Runnable() {
            public void run() {

                Fragment newFragment = null;
                try {
                    newFragment = (Fragment) fragmentClass.newInstance();
                    if (bundle != null)
                        newFragment.setArguments(bundle);
                } catch (Exception e) {
                    Log.e(LOG_TAG, "Exception : " + e.getMessage());
                }


                if (newFragment != null) {
                    m_secondFragment = newFragment;
                    FragmentManager fragmentManager = getFragmentManager();
                    FragmentTransaction transaction = fragmentManager.beginTransaction();

                    // Replace whatever is in the fragment_container view with this fragment,
                    // and add the transaction to the back stack so the user can navigate back
                    transaction.replace(R.id.second_fragment, m_secondFragment);
                    transaction.addToBackStack("frag");

                    // Commit the transaction
                    transaction.commit();
                    fragmentManager.executePendingTransactions();

                    //showRightFragment();
                }
            }
        });
    }

}
