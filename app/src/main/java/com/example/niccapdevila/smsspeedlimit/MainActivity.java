package com.example.niccapdevila.smsspeedlimit;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.telephony.SmsManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class MainActivity extends ActionBarActivity implements ActionBar.TabListener {


    static SMSDatabaseHelper mSMSDatabaseHelper;


    public static final String UPDATE_OVER15ARRAYLIST = "com.example.niccapdevila.smsspeedlimit.UPDATE_OVER15ARRAYLIST";
    public static final String UPDATE_UNDER15ARRAYLIST = "com.example.niccapdevila.smsspeedlimit.UPDATE_OVER15ARRAYLIST";


    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;


//Starts the service that listens for Outgoing SMS messages
    public void listenForSMS(){

        startService(new Intent(this, OutgoingSMSReceiver.class));

    }





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set up the action bar.
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());


        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        // When swiping between different sections, select the corresponding
        // tab. We can also use ActionBar.Tab#select() to do this if we have
        // a reference to the Tab.
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }
        });

        // For each of the sections in the app, add a tab to the action bar.
        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
            // Create a tab with text corresponding to the page title defined by
            // the adapter. Also specify this Activity object, which implements
            // the TabListener interface, as the callback (listener) for when
            // this tab is selected.
            actionBar.addTab(
                    actionBar.newTab()
                            .setText(mSectionsPagerAdapter.getPageTitle(i))
                            .setTabListener(this));
        }






        listenForSMS();



    }


    @Override
    protected void onResume() {
        super.onResume();
/*        mSMSInfoList = mSMSDatabaseHelper.getAllSMSInfos();

        mSMSInfoArrayAdapter.clear();
        mSMSInfoArrayAdapter.addAll(mSMSInfoList);

        mSMSInfoArrayAdapter.notifyDataSetChanged();*/
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        // When the given tab is selected, switch to the corresponding page in
        // the ViewPager.
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            if(position==0)
                return Under15Fragment.newInstance(position);
            if(position==1) {
                return Over15Fragment.newInstance(position);
            }


            return null;
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            switch (position) {
                case 0:
                    return getString(R.string.tab_title_under15).toUpperCase(l);
                case 1:
                    return getString(R.string.tab_title_over15).toUpperCase(l);

            }
            return null;
        }
    }

    /**
     * Under 15 mph fragment
     */
    public static class Under15Fragment extends Fragment {

        Button m_kClearButton;

        ListView mListViewSMSInfoUnder15;
        SMSInfoArrayAdapter mSMSInfoArrayAdapter;
        List<SMSInfo> mSMSUnder15InfoList;
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static Under15Fragment newInstance(int sectionNumber) {
            Under15Fragment fragment = new Under15Fragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public Under15Fragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.under15_fragment, container, false);

            mSMSDatabaseHelper = new SMSDatabaseHelper(getActivity());
            mSMSUnder15InfoList =  new ArrayList<SMSInfo>();
            mSMSInfoArrayAdapter = new SMSInfoArrayAdapter(getActivity(), mSMSUnder15InfoList);

            for(SMSInfo smsInfo : mSMSDatabaseHelper.getAllSMSInfos()){
                if(smsInfo.getSpeed()!=null) {
                    if (Float.parseFloat(smsInfo.getSpeed()) < 15f)
                        mSMSInfoArrayAdapter.add(smsInfo);
                }
            }


            mSMSInfoArrayAdapter.notifyDataSetChanged();

            mListViewSMSInfoUnder15 = (ListView) rootView.findViewById(R.id.listViewSMSInfoUnder15);
            mListViewSMSInfoUnder15.setAdapter(mSMSInfoArrayAdapter);

            m_kClearButton = (Button) rootView.findViewById(R.id.button_send_text);
            m_kClearButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    mSMSDatabaseHelper.deleteSMSs(mSMSUnder15InfoList);
                    mSMSInfoArrayAdapter.clear();
                    mSMSInfoArrayAdapter.notifyDataSetChanged();
                }
            });

            //Register a broadcast listener for UI update
            LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(getActivity());
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(UPDATE_UNDER15ARRAYLIST);
            broadcastManager.registerReceiver(bReceiver, intentFilter);

            return rootView;
        }


        private BroadcastReceiver bReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(UPDATE_UNDER15ARRAYLIST)) {

                    mSMSInfoArrayAdapter.clear();

                    for(SMSInfo smsInfo : mSMSDatabaseHelper.getAllSMSInfos()){
                        if(smsInfo.getSpeed()!=null) {
                            if (Float.parseFloat(smsInfo.getSpeed()) < 15f)
                                mSMSInfoArrayAdapter.add(smsInfo);
                        }
                    }
                    mSMSInfoArrayAdapter.notifyDataSetChanged();
                }
            }
        };
    }
/*

    */
/**
     * Over 15 mph fragment
     */

     public static class Over15Fragment extends Fragment {

        Button m_kClearButton;
        Button m_kButtonSendText;
        ListView mListViewSMSInfOver15;
        SMSDatabaseHelper mSMSDatabaseHelper;
        SMSInfoArrayAdapter mSMSInfoArrayAdapter;
        List<SMSInfo> mSMSOver15InfoList;

/**
         * The fragment argument representing the section number for this
         * fragment.
         */

        private static final String ARG_SECTION_NUMBER = "section_number";

/**
         * Returns a new instance of this fragment for the given section
         * number.
         */

        public static Over15Fragment newInstance(int sectionNumber) {
            Over15Fragment fragment = new Over15Fragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public Over15Fragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.over15_fragment, container, false);

            mSMSDatabaseHelper = new SMSDatabaseHelper(getActivity());

            mSMSOver15InfoList = new ArrayList<SMSInfo>();
            mSMSInfoArrayAdapter = new SMSInfoArrayAdapter(getActivity(), mSMSOver15InfoList);

            for(SMSInfo smsInfo : mSMSDatabaseHelper.getAllSMSInfos()){
                if(smsInfo.getSpeed()!=null) {
                    if (Float.parseFloat(smsInfo.getSpeed()) >= 15f)
                        mSMSInfoArrayAdapter.add(smsInfo);
                }
            }


            mSMSInfoArrayAdapter.notifyDataSetChanged();

            mListViewSMSInfOver15 = (ListView) rootView.findViewById(R.id.listViewSMSInfoOver15);
            mListViewSMSInfOver15.setAdapter(mSMSInfoArrayAdapter);

            m_kClearButton = (Button) rootView.findViewById(R.id.button_clear_list);
            m_kClearButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    mSMSDatabaseHelper.deleteSMSs(mSMSOver15InfoList);
                    mSMSInfoArrayAdapter.clear();
                    mSMSInfoArrayAdapter.notifyDataSetChanged();
                }
            });

            m_kButtonSendText = (Button) rootView.findViewById(R.id.button_send_text);
            m_kButtonSendText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SmsManager sms = SmsManager.getDefault();
                    sms.sendTextMessage("4115", null, "Test Message", null, null);
                }
            });
            //Register a broadcast listener for UI update
            LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(getActivity());
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(UPDATE_OVER15ARRAYLIST);
            broadcastManager.registerReceiver(bReceiver, intentFilter);

            return rootView;
        }


        private BroadcastReceiver bReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(UPDATE_OVER15ARRAYLIST)) {

                    mSMSInfoArrayAdapter.clear();

                    for(SMSInfo smsInfo : mSMSDatabaseHelper.getAllSMSInfos()){
                        if(smsInfo.getSpeed()!=null) {
                            if (Float.parseFloat(smsInfo.getSpeed()) >= 15f)
                                mSMSInfoArrayAdapter.add(smsInfo);
                        }
                    }

                    mSMSInfoArrayAdapter.notifyDataSetChanged();
                }
            }
        };
    }

}
