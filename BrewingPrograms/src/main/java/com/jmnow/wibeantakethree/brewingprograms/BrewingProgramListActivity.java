package com.jmnow.wibeantakethree.brewingprograms;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.os.Handler;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;

import com.squareup.okhttp.OkHttpClient;

/**
 * An activity representing a list of BrewingPrograms. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link BrewingProgramDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 * <p>
 * The activity makes heavy use of fragments. The list of items is a
 * {@link BrewingProgramListFragment} and the item details
 * (if present) is a {@link BrewingProgramDetailFragment}.
 * <p>
 * This activity also implements the required
 * {@link BrewingProgramListFragment.Callbacks} interface
 * to listen for item selections.
 */
public class BrewingProgramListActivity extends Activity
        implements BrewingProgramListFragment.Callbacks,
        NavigationDrawerFragment.NavigationDrawerCallbacks,
        TakeControlFragment.TakeControlFragmentListener,
        AlarmFragment.WiBeanAlarmFragmentInteractionListener
{

     /**
      * FRAGMENT IDENTIFIERS
      */
    private static final String TAG_TAKECONTROL = "fragment_takeControl";
    private static final String TAG_BREWINGPROGRAMLIST = "fragment_brewingProgramList";
    private static final String TAG_ALARM = "fragment_alarm";
    private static final String TAG_BREWINGPROGRAMDETAIL = "fragment_brewingProgramDetail";


     /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;
    // httpClient, make one to save resources
    OkHttpClient mHttpClient;
    // title shows above
    private CharSequence mTitle;
    // Handler allows us to run actions on the GUI thread
    private Handler mHandler = new Handler();
    // used to store pointer to a progress dialog
    private ProgressDialog mProgess;
    // handles the state and communication with the WiBean
    private WiBeanYunState mWibean = new WiBeanYunState();

    //*******************
    //* LIFECYCLE METHODS
    //******************
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_brewingprogram_list);

        if (findViewById(R.id.brewingprogram_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-large and
            // res/values-sw600dp). If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;
            // In two-pane mode, list items should be given the
            // 'activated' state when touched.
            ((BrewingProgramListFragment) getFragmentManager()
                    .findFragmentById(R.id.brewingprogram_list))
                    .setActivateOnItemClick(true);
        }

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getFragmentManager().findFragmentById(R.id.navigation_drawer);
        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

        mTitle = getTitle();
        mHttpClient = new OkHttpClient();

        // TODO: If exposing deep links into your app, handle intents here.
    }


    public void onSectionAttached(int number) {
        switch (number) {
            case 0:
                mTitle = getString(R.string.title_takeControl);
                break;
            case 1:
                mTitle = getString(R.string.title_brewingprogram_list);
                break;
            case 2:
                mTitle = getString(R.string.title_wakeAlarm);
                break;
        }
    }
    public void restoreActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.main, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    public boolean refreshIp() {
        SharedPreferences prefs = getPreferences(Context.MODE_PRIVATE);
        String ipAddress = prefs.getString(WiBeanYunState.UNIT_IP_PREF_KEY,"");
        if( ipAddress.isEmpty() ) {
            return false;
        }
        return mWibean.setIpAddress(ipAddress);
    }


    //*************
    //* INTERFACES
    //*************

    /**
     * INTERFACE FOR takeControl fragment
     */

    /**
     * Allows fragment to request alert dialogues which the host activity can appropriately
     * display.
     * @param title Title of the Alert Dialog.
     * @param message Message displayed in the Alert Dialog.
     */
    public void alertUser(String title, String message) {
        // alert the user and stop
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message)
                .setTitle(title)
                .setPositiveButton("OK",null);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public boolean takeControl() {
        AsyncTask<Void,Integer,Boolean> task = new TakeControlTask().execute();
        Boolean success = false;
        try {
           success = task.get();
        }
        catch( Exception e ) {
            System.out.println("takeControl was interrupted: " + e.getLocalizedMessage());
            success = false;
        }
        if( success ) {
            TakeControlFragment f = (TakeControlFragment)getFragmentManager().findFragmentByTag(TAG_TAKECONTROL);
            if( f != null ) {
                f.setInControl();
            }
        }
        return success.booleanValue();
    }
    public boolean returnControl() {
        AsyncTask<Void,Integer,Boolean> task = new TakeControlTask().execute();
        Boolean success = false;
        try {
            success = task.get();
        }
        catch( Exception e ) {
            System.out.println("returnControl was interrupted: " + e.getLocalizedMessage());
            success = false;
        }
        if( success ) {
            TakeControlFragment f = (TakeControlFragment)getFragmentManager().findFragmentByTag(TAG_TAKECONTROL);
            if( f != null ) {
                f.setNoControl();
            }
        }
        return success.booleanValue();
    }
    /**
     * Allows user to use the progress bar for the whole activity.
     * Setup so that callers can use any thread
     */
    public void makeBusy(final CharSequence title, final CharSequence message) {
        if( !mHandler.post(new Runnable() {
            public void run() {
                mProgess = new ProgressDialog(BrewingProgramListActivity.this);
                mProgess.setTitle(title);
                mProgess.setMessage(message);
                mProgess.setIndeterminate(true);
                mProgess.setCancelable(false);
                mProgess.show();
            }
        }) ) {
            System.out.println("ERROR MAKING BUSY!");
        }
    }
    public void makeNotBusy() {
        mHandler.post(new Runnable() {
            public void run() {
                if( mProgess != null) {
                    mProgess.dismiss();
                }
            }
        });
    }

    /**
     * INTERFACE FOR NavigationDrawer fragment
     * add support for the navigation drawer
     */
    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getFragmentManager();
        if( position == 0) {
            fragmentManager.beginTransaction()
                    .replace(R.id.list_content_container, TakeControlFragment.newInstance(false), TAG_TAKECONTROL)
                    .commit();
        }
        else if( position == 1) {
            fragmentManager.beginTransaction()
                    .replace(R.id.list_content_container, new BrewingProgramListFragment(), TAG_BREWINGPROGRAMLIST)
                    .commit();
        }
        else {
            fragmentManager.beginTransaction()
                    .replace(R.id.list_content_container, AlarmFragment.newInstance(70, 540, 20), TAG_ALARM)
                    .commit();
        }
    }


    /**
     * INTERFACE FOR BrewingProgramListFragment
     * takes action when the user clicks an item
     */
    public void onItemSelected(String id) {
        FragmentManager fragmentManager = getFragmentManager();
        BrewingProgramDetailFragment frag = new BrewingProgramDetailFragment();
        Bundle args = new Bundle();
        args.putString(BrewingProgramDetailFragment.ARG_ITEM_ID, id);
        frag.setArguments(args);
        fragmentManager.beginTransaction()
                .replace(R.id.list_content_container, frag, TAG_BREWINGPROGRAMDETAIL)
                .commit();
    }


    /**
     * INTERFACE FOR AlarmFragment
     * tries communication when user sets alarm
     */
    public boolean sendAlarmRequest(WiBeanYunState.WiBeanAlarmPack requestedAlarm){
        return true;
    }
    public WiBeanYunState.WiBeanAlarmPack requestAlarmState() {
        return new WiBeanYunState.WiBeanAlarmPack();
    }


    /**
     * ASYNC TASKS USED TO DO NETWORK AND GUI CALLS APPROPRIATELY
     */

    private class TakeControlTask extends AsyncTask<Void, Integer, Boolean> {
        protected Boolean doInBackground(Void... voids) {
            return mWibean.takeControl();
        }
        protected void onPreExecute() {
            makeBusy("Please wait", "Taking control...");
            refreshIp();
        }
        protected void onPostExecute(Boolean result) {
            makeNotBusy();
            if( !result ) {
                alertUser(getString(R.string.dialog_ip_error_title), getString(R.string.dialog_ip_error_message));
            }
        }
    }
    private class ReturnControlTask extends AsyncTask<Void, Integer, Boolean> {
        protected Boolean doInBackground(Void... voids) {
            return mWibean.returnControl();
        }
        protected void onPreExecute() {
            makeBusy("Please wait", "Taking control...");
            refreshIp();
        }
        protected void onPostExecute(Boolean result) {
            makeNotBusy();
            if( !result ) {
                alertUser(getString(R.string.dialog_ip_error_title), getString(R.string.dialog_ip_error_message));
            }
        }
    }
}
