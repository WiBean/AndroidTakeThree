package com.jmnow.wibeantakethree.brewingprograms;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.widget.TextView;

import com.jmnow.wibeantakethree.brewingprograms.data.BrewingProgram;
import com.jmnow.wibeantakethree.brewingprograms.wibean.WiBeanYunState;
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
        AlarmFragment.WiBeanAlarmFragmentInteractionListener,
        BrewingProgramDetailFragment.BrewingProgramDetailCallbacks {

     /**
      * FRAGMENT IDENTIFIERS
      */
    private static final String TAG_TAKECONTROL = "fragment_takeControl";
    private static final String TAG_BREWINGPROGRAMLIST = "fragment_brewingProgramList";
    private static final String TAG_ALARM = "fragment_alarm";
    private static final String TAG_BREWINGPROGRAMDETAIL = "fragment_brewingProgramDetail";
    // httpClient, make one to save resources
    OkHttpClient mHttpClient;
     /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;
    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;
    // title shows above
    private CharSequence mTitle;
    // Handler allows us to run actions on the GUI thread, and post delayed events
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
        // if success, enable temperature polling
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                temperaturePollLoop();
            }
        });
        return success.booleanValue();
    }
    public boolean returnControl() {
        AsyncTask<Void, Integer, Boolean> task = new ReturnControlTask().execute();
        Boolean success = false;
        try {
            success = task.get();
        }
        catch( Exception e ) {
            System.out.println("returnControl was interrupted: " + e.getLocalizedMessage());
            success = false;
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

    public void temperaturePollLoop() {
        if (mWibean.inControl()) {
            AsyncTask<Void, Integer, String> task = new QueryTemperatureTask().execute();
            mHandler.postDelayed(new Runnable() {
                                     @Override
                                     public void run() {
                                         temperaturePollLoop();
                                     }
                                 },
                    500
            );
        }
    }
    // ********************
    // INTERFACES
    // ********************

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

    public boolean onResetSelected() {
        return returnControl();
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
                .addToBackStack(null)
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
     * INTERFACE FOR BrewingProgramDetailFragment
     * when the user wants to brew!
     */
    public boolean brewProgram(BrewingProgram theProgram) {
        AsyncTask<BrewingProgram, Integer, Boolean> task = new BrewProgramTask().execute(theProgram);
        Boolean success = false;
        try {
            success = task.get();
        } catch (Exception e) {
            System.out.println("brewProgram was interrupted: " + e.getLocalizedMessage());
            success = false;
        }
        return success.booleanValue();
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
            if( !result ) {
                alertUser(getString(R.string.dialog_ip_error_title), getString(R.string.dialog_ip_error_message));
            } else {
                TakeControlFragment f = (TakeControlFragment) getFragmentManager().findFragmentByTag(TAG_TAKECONTROL);
                if (f != null) {
                    f.setInControl();
                }
                ((TextView) (findViewById(R.id.tv_inControlLabel))).setText(R.string.heading_in_control_true);
            }
            makeNotBusy();
        }
    }
    private class ReturnControlTask extends AsyncTask<Void, Integer, Boolean> {
        protected Boolean doInBackground(Void... voids) {
            return mWibean.returnControl();
        }
        protected void onPreExecute() {
            makeBusy("Please wait", "Returning control...");
            refreshIp();
        }

        protected void onPostExecute(Boolean result) {
            if (!result) {
                alertUser(getString(R.string.dialog_ip_error_title), getString(R.string.dialog_ip_error_message));
            } else {
                TakeControlFragment f = (TakeControlFragment) getFragmentManager().findFragmentByTag(TAG_TAKECONTROL);
                if (f != null) {
                    f.setNoControl();
                }
            }
            makeNotBusy();
        }
    }

    private class QueryTemperatureTask extends AsyncTask<Void, Integer, String> {
        protected String doInBackground(Void... voids) {
            StringBuilder builder = new StringBuilder();
            mWibean.getTemperature(builder);
            return builder.toString();
        }

        protected void onPreExecute() {
            refreshIp();
        }

        protected void onPostExecute(String result) {
            if (result.isEmpty()) {
                alertUser(getString(R.string.dialog_ip_error_title), getString(R.string.dialog_ip_error_message));
            } else {
                TextView v = (TextView) findViewById(R.id.tv_headTemperature);
                v.setText(result);
            }
            makeNotBusy();
        }
    }

    private class BrewProgramTask extends AsyncTask<BrewingProgram, Integer, Boolean> {
        protected Boolean doInBackground(BrewingProgram... programs) {
            boolean success = true;
            for (int k = 0; k < programs.length; ++k) {
                success &= mWibean.runBrewProgram(programs[k]);
            }
            return success;
        }

        protected void onPreExecute() {
            makeBusy("Brewing!", "Generating coffee...");
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
