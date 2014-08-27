package com.jmnow.wibeantakethree.brewingprograms;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.IdRes;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.jmnow.wibeantakethree.brewingprograms.data.BrewingProgram;
import com.jmnow.wibeantakethree.brewingprograms.data.BrewingProgramContentProvider;
import com.jmnow.wibeantakethree.brewingprograms.data.BrewingProgramHelper;
import com.jmnow.wibeantakethree.brewingprograms.wibean.WiBeanSparkState;
import com.squareup.okhttp.OkHttpClient;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.Semaphore;

/**
 * An activity representing a list of BrewingPrograms. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link BrewingProgramDetailFragment} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 * <p/>
 * The activity makes heavy use of fragments. The list of items is a
 * {@link BrewingProgramListFragment} and the item details
 * (if present) is a {@link BrewingProgramDetailFragment}.
 * <p/>
 * This activity also implements the required
 * {@link BrewingProgramListFragment.Callbacks} interface
 * to listen for item selections.
 */
public class BrewingProgramListActivity extends Activity
        implements BrewingProgramListFragment.Callbacks,
        NavigationDrawerFragment.NavigationDrawerCallbacks,
        TakeControlFragment.TakeControlFragmentListener,
        AlarmFragment.WiBeanAlarmFragmentInteractionListener,
        BrewingProgramDetailFragment.BrewingProgramDetailCallbacks,
        SmartConfigFragment.SmartConfigFragmentListener {

    /**
     * FRAGMENT IDENTIFIERS
     */
    private static final String TAG_TAKECONTROL = "fragment_takeControl";
    private static final String TAG_BREWINGPROGRAMLIST = "fragment_brewingProgramList";
    private static final String TAG_ALARM = "fragment_alarm";
    private static final String TAG_BREWINGPROGRAMDETAIL = "fragment_brewingProgramDetail";
    private static final String TAG_SMARTCONFIG = "fragment_smartConfig";
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
    // Handler allows us to run actions on the GUI thread, and post delayed events
    private Handler mHandler = new Handler();
    // used to store pointer to a progress dialog
    private ProgressDialog mProgress;
    // handles the state and communication with the WiBean
    private WiBeanSparkState mWibean = new WiBeanSparkState();
    // this variable is used to allow fragments to point at a control which we will fill when
    // barcode scans come back
    private
    @IdRes
    int mRequestedViewId = -1;
    // keep track of whether we have valid credentials
    private boolean mCredentialsValid = false;
    // keep track of the context
    private Context mContext;
    // mutex for keeping track of temperature HTTP calls
    private Semaphore mTempLoopMutex = new Semaphore(1);
    // keep track of connection attempts so we don't try forever
    private boolean tryConnectionOnFailure = true;
    //*******************
    //* LIFECYCLE METHODS
    //******************
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
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

        mHttpClient = new OkHttpClient();

        // TODO: If exposing deep links into your app, handle intents here.
        Intent intent = getIntent();
        if (!parseIntent(intent)) {
            mNavigationDrawerFragment.selectItem(2);
        }
    }

    // returns true if action taken
    private boolean parseIntent(Intent intent) {
        String action = intent.getAction();
        Uri data = intent.getData();
        if (action.equals("android.intent.action.VIEW")) {
            // parse appropriately different versions
            if (data.getScheme().equalsIgnoreCase("http")) {
                if (data.getHost().equalsIgnoreCase("www.wibean.com")) {
                    if (data.getPath().startsWith("/brewingProgram/v1")) {
                        // parse the program.
                        System.out.println(data.getQuery());
                        BrewingProgram newProg = BrewingProgram.fromUri(data);
                        if (insertOrUpdateBrewingProgram(newProg)) {
                            // navigate to the program list so the backstack works
                            mNavigationDrawerFragment.selectItem(0);
                            // load the item
                            onItemSelected(newProg.getId());
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    public void onSectionAttached(int number) {
        /**
         * Don't handle this here for now
         */
        /*
        switch (number) {
            case 0:
                mTitle = getString(R.string.title_takeControl);
                break;
            case 1:
                mTitle = getString(R.string.title_brewingprogram_list);
                mTitle = getString(R.string.title_brewingprogram_list);
                break;
            case 2:
                mTitle = getString(R.string.title_wakeAlarm);
                break;
        }
        */
    }

    public void restoreActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        boolean success = super.onCreateOptionsMenu(menu);
        // always load the global menu
        getMenuInflater().inflate(R.menu.global, menu);
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            restoreActionBar();
        }

        try {
            MenuItem item = menu.findItem(R.id.menu_toggle_heating);
            if (mWibean.isHeating()) {
                item.setTitle(R.string.action_toggle_heating_off);
            } else {
                item.setTitle(R.string.action_toggle_heating_on);
            }
        } catch (Exception e) {
            System.out.println(e.toString());
        }


        return success;
    }

    /**
     * For the Detail fragment, when the user pressed back, we should see if need to save changes
     * they have made.
     */
    @Override
    public void onBackPressed() {
        final BrewingProgramDetailFragment fragment = (BrewingProgramDetailFragment) getFragmentManager().findFragmentByTag(TAG_BREWINGPROGRAMDETAIL);
        if (fragment != null) {
            fragment.saveIfNecessary();
        }
        super.onBackPressed();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
        if (result != null) {
            String contents = result.getContents();
            if (contents != null) {
                // load the string
                EditText et = (EditText) findViewById(mRequestedViewId);
                if (et != null) {
                    et.setText(contents);
                }
            }
            // else do nothing and fail
        }
        // reset requested control
        mRequestedViewId = -1;
    }

    public boolean refreshPrefs() {
        SharedPreferences prefs = getPreferences(Context.MODE_PRIVATE);
        String deviceId = prefs.getString(WiBeanSparkState.PREF_KEY_DEVICE_ID, "");
        String accessToken = prefs.getString(WiBeanSparkState.PREF_KEY_ACCESS_TOKEN, "");
        if (deviceId.isEmpty() || accessToken.isEmpty()) {
            return false;
        }
        boolean success = true;
        success &= mWibean.setSparkDeviceId(deviceId);
        success &= mWibean.setSparkAccessToken(accessToken);
        return success;
    }

    //*************
    //* INTERFACES
    //*************

    /**
     * INTERFACE FOR testCredentials fragment
     */

    /**
     * Allows fragment to request alert dialogues which the host activity can appropriately
     * display.
     *
     * @param title   Title of the Alert Dialog.
     * @param message Message displayed in the Alert Dialog.
     */
    public void alertUser(String title, String message) {
        // alert the user and stop
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message)
                .setPositiveButton("OK", null);
        if (!title.isEmpty()) {
            builder.setTitle(title);
        }
        AlertDialog dialog = builder.create();
        dialog.show();
    }


    public void setTargetControl(@IdRes int controlId) {
        mRequestedViewId = controlId;
    }

    public void toggleHeating() {
        if (mWibean.isHeating()) {
            try {
                AsyncTask<Void, Integer, Boolean> task = new MakeHibernateTask().execute();
            } catch (Exception e) {
                System.out.println("toggleHeating was interrupted: " + e.getLocalizedMessage());
            }
        } else {
            try {
                AsyncTask<Integer, Integer, Boolean> task = new MakeHeatTask().execute();
            } catch (Exception e) {
                System.out.println("MakeHeat was interrupted: " + e.getLocalizedMessage());
            }
        }
    }

    /**
     * Allows user to use the progress bar for the whole activity.
     * Designed to be called in the GUI thread, most likely via an AsyncTask pre/post trigger
     */
    public void makeBusy(final CharSequence title, final CharSequence message) {
        mProgress = new ProgressDialog(BrewingProgramListActivity.this);
        mProgress.setProgressStyle(mProgress.STYLE_SPINNER);
        mProgress.setTitle(title);
        mProgress.setMessage(message);
        mProgress.setIndeterminate(true);
        mProgress.setCancelable(false);
        mProgress.show();
    }

    public void makeBusyWithProgress(final CharSequence title, final CharSequence message) {
        mProgress = new ProgressDialog(BrewingProgramListActivity.this, ProgressDialog.STYLE_HORIZONTAL);
        mProgress.setProgressStyle(mProgress.STYLE_HORIZONTAL);
        mProgress.setTitle(title);
        mProgress.setMessage(message);
        mProgress.setProgress(0);
        mProgress.setMax(100);
        mProgress.setCancelable(false);
        mProgress.show();
    }

    public void updateBusyProgress(int value) {
        if (mProgress != null) {
            mProgress.setProgress(value);
        }
    }

    public void makeNotBusy() {
        if (mProgress != null) {
            mProgress.dismiss();
        }
    }

    public void temperaturePollLoop() {
        // only allow one poll at a time
        if (mTempLoopMutex.tryAcquire()) {
            // transitioning from unconnected > connected, notify user
            if (!mCredentialsValid && tryConnectionOnFailure) {
                makeBusy(getString(R.string.action_connecting_title), getString(R.string.action_pleaseWait));
            }
            // make the request
            AsyncTask<Void, Integer, Boolean> task = new QueryStatusTask().execute();
        }
        // query every 4 seconds when cold
        int queryInterval = 4000;
        // 2 seconds when heating
        if (mWibean.isHeating()) {
            queryInterval = 2000;
        }
        // and 1 second when  brewing
        if (mWibean.isBrewing()) {
            queryInterval = 1000;
        }
        mHandler.postDelayed(new Runnable() {
                                 @Override
                                 public void run() {
                                     temperaturePollLoop();
                                 }
        }, queryInterval);
    }

    public boolean insertOrUpdateBrewingProgram(BrewingProgram theProgram) {
        // Defines an object to contain the updated values
        ContentValues updateValues = new ContentValues();
        // Sets the updated value and updates the selected words.
        updateValues.put(BrewingProgramHelper.COLUMN_NAME, theProgram.getName());
        updateValues.put(BrewingProgramHelper.COLUMN_DESCRIPTION, theProgram.getDescription());
        // we can't use the SQLite datetime() function via the ContentValues object, so construct
        // the string ourselves
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date();
        updateValues.put(BrewingProgramHelper.COLUMN_MODIFIED_AT, dateFormat.format(date));

        Integer[] onTimes = theProgram.getOnTimes();
        Integer[] offTimes = theProgram.getOffTimes();
        updateValues.put(BrewingProgramHelper.COLUMN_ON_ONE, onTimes[0]);
        updateValues.put(BrewingProgramHelper.COLUMN_OFF_ONE, offTimes[0]);
        updateValues.put(BrewingProgramHelper.COLUMN_ON_TWO, onTimes[1]);
        updateValues.put(BrewingProgramHelper.COLUMN_OFF_TWO, offTimes[1]);
        updateValues.put(BrewingProgramHelper.COLUMN_ON_THREE, onTimes[2]);
        updateValues.put(BrewingProgramHelper.COLUMN_OFF_THREE, offTimes[2]);
        updateValues.put(BrewingProgramHelper.COLUMN_ON_FOUR, onTimes[3]);
        updateValues.put(BrewingProgramHelper.COLUMN_OFF_FOUR, offTimes[3]);
        updateValues.put(BrewingProgramHelper.COLUMN_ON_FIVE, onTimes[4]);
        updateValues.put(BrewingProgramHelper.COLUMN_OFF_FIVE, offTimes[4]);
        updateValues.put(BrewingProgramHelper.COLUMN_SHORT_URL, theProgram.getShortUrl());

        // catch results
        int rowsUpdated = 0;
        Uri newRow;
        if (theProgram.getId().isEmpty()) {
            // insert
            newRow = this.getContentResolver().insert(
                    BrewingProgramContentProvider.CONTENT_URI,  // the user dictionary content URI
                    updateValues); // the columns to update
            String resultAsString = newRow.toString();
            if (!resultAsString.contains("-1")) {
                // success!
                theProgram.setId(resultAsString.substring(resultAsString.lastIndexOf("/") + 1));
                return true;
            } else {
                return false;
            }
        } else {
            // update
            rowsUpdated = this.getContentResolver().update(
                    ContentUris.withAppendedId(BrewingProgramContentProvider.CONTENT_URI, Long.parseLong(theProgram.getId())),  // the user dictionary content URI
                    updateValues,                       // the columns to update
                    null, // the column to select on
                    null// the value to compare to
            );
            return (rowsUpdated > 0);
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
        Fragment fragment = null;
        // figure out which was to place
        String tag = "";
        switch (position) {
            default:
            case 0:
                fragment = new BrewingProgramListFragment();
                tag = TAG_BREWINGPROGRAMLIST;
                break;
            case 1:
                fragment = AlarmFragment.newInstance(mWibean.getAlarm());
                tag = TAG_ALARM;
                break;
            case 2:
                fragment = TakeControlFragment.newInstance(mCredentialsValid);
                tag = TAG_TAKECONTROL;
                break;
            case 3:
                fragment = SmartConfigFragment.newInstance();
                tag = TAG_SMARTCONFIG;
                break;

        }
        if (fragment != null) {
            FragmentManager fm = getFragmentManager();
            // clean  up the back stack
            fm.popBackStack("menu_item_create_new", FragmentManager.POP_BACK_STACK_INCLUSIVE);
            // do the swap
            fm.beginTransaction()
                    .replace(R.id.list_content_container, fragment, tag)
                    .commit();
        }
    }

    public boolean isHeating() {
        return mWibean.isHeating();
    }

    public void onHeatToggleSelected() {
        toggleHeating();
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
                .addToBackStack("brew_program_detail_show")
                .commit();
    }


    /**
     * INTERFACE FOR AlarmFragment
     * tries communication when user sets alarm
     */
    public boolean sendAlarmRequest(WiBeanSparkState.WiBeanAlarmPackV1 requestedAlarm) {
        Boolean success = false;
        try {
            AsyncTask<WiBeanSparkState.WiBeanAlarmPackV1, Integer, Boolean> task = new ToggleAlarmTask().execute(requestedAlarm);
        } catch (Exception e) {
            System.out.println("sendAlarmRequest was interrupted: " + e.getLocalizedMessage());
            success = false;
        }
        return success;
    }

    /**
     * INTERFACE FOR BrewingProgramDetailFragment
     * when the user wants to brew!
     */
    public void brewProgram(BrewingProgram theProgram) {

        Boolean success = false;
        try {
            AsyncTask<BrewingProgram, Integer, Boolean> task = new BrewProgramTask().execute(theProgram);
        } catch (Exception e) {
            System.out.println("brewProgram was interrupted: " + e.getLocalizedMessage());
            success = false;
        }
    }

    public boolean saveOrCreateItem(BrewingProgram aProgram) {
        return insertOrUpdateBrewingProgram(aProgram);
    }

    /**
     * ASYNC TASKS USED TO DO NETWORK AND GUI CALLS APPROPRIATELY
     */

    private class MakeHeatTask extends AsyncTask<Integer, Integer, Boolean> {
        protected Boolean doInBackground(Integer... temps) {
            if (temps.length > 0) {
                return mWibean.makeHeat(temps[0]);
            } else {
                return mWibean.makeHeat();
            }
        }
        protected void onPreExecute() {
            makeBusy("Please wait", getString(R.string.action_activatingHeat));
            refreshPrefs();
        }
        protected void onPostExecute(Boolean result) {
            if (!result) {
                alertUser(getString(R.string.dialog_ip_error_title), getString(R.string.dialog_ip_error_message));
            } else {
                // heat toggle text update is already handled by NavDrawerFragment, don't do it here
                // update status text
                ((TextView) (findViewById(R.id.tv_inControlLabel))).setText(R.string.heading_control_status_heating);
            }
            // force a rebuild of the button text
            invalidateOptionsMenu();
            makeNotBusy();
        }
    }

    private class MakeHibernateTask extends AsyncTask<Void, Integer, Boolean> {
        protected Boolean doInBackground(Void... voids) {
            if (mWibean.isHeating()) {
                return mWibean.makeHibernate();
            } else {
                mWibean.makeHibernate(); // always send the command for safety
                return true; //already not in-control, so report success for the user
            }
        }
        protected void onPreExecute() {
            makeBusy("Please wait", getString(R.string.action_disablingHeat));
            refreshPrefs();
        }
        protected void onPostExecute(Boolean result) {
            if (!result) {
                alertUser(getString(R.string.dialog_ip_error_title), getString(R.string.dialog_ip_error_message));
            } else {
                // heat toggle text update is already handled by NavDrawerFragment, don't do it here
                // update status text
                ((TextView) (findViewById(R.id.tv_inControlLabel))).setText(R.string.heading_control_status_cooling);
            }
            // force a rebuild of the button text
            invalidateOptionsMenu();
            makeNotBusy();
        }
    }

    private class BrewProgramTask extends AsyncTask<BrewingProgram, Integer, Boolean> {
        protected void onPreExecute() {
            makeBusyWithProgress("Brewing!", "Generating coffee...");
            refreshPrefs();
        }
        protected Boolean doInBackground(BrewingProgram... programs) {
            boolean success = true;
            for (int k = 0; k < programs.length; ++k) {
                success &= mWibean.runBrewProgram(programs[k]);
                // now spin a progress bar for the length of the program
                // spin at ~10fps
                final int sleepTimeInMs = 1000 / 10;
                final long totalDuration = programs[k].getTotalDurationInMilliseconds();
                Calendar c = Calendar.getInstance();
                final long startTime = c.getTimeInMillis();
                final long endTime = startTime + totalDuration;
                // only spin if the brew actually started well
                while (success && (c.getTimeInMillis() < endTime)) {
                    try {
                        Thread.sleep(sleepTimeInMs);
                    } catch (Exception e) {
                    }
                    ;
                    publishProgress((int) ((c.getTimeInMillis() - startTime) * 100 / totalDuration));
                    // update the calendar object with the new time
                    c = Calendar.getInstance();
                }
                publishProgress(100);
            }
            return success;
        }

        protected void onProgressUpdate(Integer... progress) {
            updateBusyProgress(progress[0]);
        }

        protected void onPostExecute(Boolean result) {
            if (!result) {
                alertUser(getString(R.string.notify_brewingFailure_title), getString(R.string.notify_brewingFailure_message));
            } else {
                Toast.makeText(BrewingProgramListActivity.this, mContext.getString(R.string.notify_brewSuccess), Toast.LENGTH_LONG).show();
            }
            makeNotBusy();
        }
    }

    private class QueryStatusTask extends AsyncTask<Void, Integer, Boolean> {
        protected void onPreExecute() {
            refreshPrefs();
        }

        protected Boolean doInBackground(Void... voids) {
            return mWibean.queryStatus();
        }

        protected void onPostExecute(Boolean result) {
            try {
                if (!result) {
                    mCredentialsValid = false;
                    if (tryConnectionOnFailure) {
                        alertUser("", "Credentials Invalid");
                    }
                    // update Control status text
                    TextView v = (TextView) findViewById(R.id.tv_inControlLabel);
                    v.setText(R.string.heading_control_status_bad);
                } else {
                    // if success and we just connected
                    if ((mHandler != null) && !mCredentialsValid) {
                        // notify user
                        mHandler.post(() -> {
                            Toast.makeText(BrewingProgramListActivity.this, mContext.getString(R.string.notify_wibeanConnectionSuccess), Toast.LENGTH_SHORT).show();
                        });
                        // and flip to the brewing programs
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                mNavigationDrawerFragment.selectItem(0);
                            }
                        });
                    }
                    mCredentialsValid = true;
                    // update Control status text
                    TextView v = (TextView) findViewById(R.id.tv_inControlLabel);
                    if (mWibean.isBrewing()) {
                        v.setText(R.string.heading_control_status_brewing);
                    } else if (mWibean.isHeating()) {
                        v.setText(R.string.heading_control_status_heating);
                    } else {
                        v.setText(R.string.heading_control_status_cooling);
                    }
                    // and update the heat/sleep button by forcing a rebuild of the menus
                    invalidateOptionsMenu();
                    // update head temp display
                    v = (TextView) findViewById(R.id.tv_headTemperature);
                    v.setText(new StringBuilder().append(mWibean.getHeadTemperature()).toString());
                    // if we have an Alarm fragment, update the remote UI state
                    AlarmFragment f = (AlarmFragment) getFragmentManager().findFragmentByTag(TAG_ALARM);
                    if (f != null) {
                        f.updateRemoteAlarm(mWibean.getAlarm());
                    }
                }
                // if we have a TakeControl fragment, update the UI for proper consistency
                TakeControlFragment f = (TakeControlFragment) getFragmentManager().findFragmentByTag(TAG_TAKECONTROL);
                if (f != null) {
                    if (mCredentialsValid) {
                        f.setCredentialsValid();
                    } else {
                        f.setCredentialsInvalid();
                    }
                }
            } catch (Exception e) {
                // if it fails, no worries
            }
            // we just finished a connection attempt, if we were supposed to toggle this, do it.
            tryConnectionOnFailure = false;
            mTempLoopMutex.release();
            makeNotBusy();
        }
    }

    private class ToggleAlarmTask extends AsyncTask<WiBeanSparkState.WiBeanAlarmPackV1, Integer, Boolean> {
        protected Boolean doInBackground(WiBeanSparkState.WiBeanAlarmPackV1... alarmPacks) {
            boolean success = true;
            if (alarmPacks.length > 0) {
                success &= mWibean.setAlarm(alarmPacks[0]);
            }
            return success;
        }

        protected void onPreExecute() {
            makeBusy(getString(R.string.notify_alarmUpdate_title), getString(R.string.action_pleaseWait));
            refreshPrefs();
        }

        protected void onPostExecute(Boolean result) {
            if (!result) {
                alertUser(getString(R.string.dialog_ip_error_title), getString(R.string.dialog_ip_error_message));
            }
            makeNotBusy();
        }
    }
}
