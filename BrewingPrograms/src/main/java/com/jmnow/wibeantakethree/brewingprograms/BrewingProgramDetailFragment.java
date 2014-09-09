package com.jmnow.wibeantakethree.brewingprograms;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ShareActionProvider;
import android.widget.TextView;
import android.widget.Toast;

import com.jmnow.wibeantakethree.brewingprograms.data.BrewingProgram;
import com.jmnow.wibeantakethree.brewingprograms.data.BrewingProgramContentProvider;

/**
 * A fragment representing a single BrewingProgram detail screen.
 * This fragment is either contained in a {@link BrewingProgramListActivity}
 * in two-pane mode (on tablets) or a {@link BrewingProgramDetailActivity}
 * on handsets.
 */
public class BrewingProgramDetailFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<Cursor>,
        View.OnClickListener {
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_ITEM_ID = "item_id";
    /**
     * The LOADER instance used here must be identified, whatever you want
     */
    private static final int PROGRAMS_LOADER = 0;
    // Event listener
    BrewingProgramDetailCallbacks mListener;
    private TextWatcher mDifferenceToggle = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            updateUiOnChange();
        }
    };
    // ensure the controls have at least a zero when they lose focus
    private View.OnFocusChangeListener mFocusLostChecker = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if (!hasFocus) {
                EditText et = (EditText) v;
                if (et.length() == 0) {
                    et.setText("0");
                }
            }
        }
    };
    // Do we have unsaved changes?
    private boolean mUnsavedChanges = false;
    //for the share button
    private ShareActionProvider mShareActionProvider;
    /**
     * The content this fragment is presenting.
     */
    private BrewingProgram mItem = new BrewingProgram("-1", "", "",
            new Integer[]{0, 0, 0, 0, 0}, new Integer[]{0, 0, 0, 0, 0});

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public BrewingProgramDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        if (getArguments().containsKey(ARG_ITEM_ID)) {
            // Load the dummy content specified by the fragment
            // arguments. In a real-world scenario, use a Loader
            // to load content from a content provider.
            mItem.setId(getArguments().getString(ARG_ITEM_ID));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View rootView = inflater.inflate(R.layout.fragment_brewingprogram_detail, container, false);
        if (mItem.getId().isEmpty()) {
            //we are creating a fresh one
            ((Button) rootView.findViewById(R.id.btn_programDiscardChanges)).setText(R.string.action_createProgram);
        } else {

        }
        // hookup the button here in the Fragment
        // (onClicks generated from buttons in Fragments get sent to their Activity
        // removing modularity)
        ((Button) rootView.findViewById(R.id.btn_brew)).setOnClickListener(this);
        ((Button) rootView.findViewById(R.id.btn_programDiscardChanges)).setOnClickListener(this);

        // setup difference checker to enable/disable save button
        ((EditText) rootView.findViewById(R.id.et_program_name)).addTextChangedListener(mDifferenceToggle);
        ((EditText) rootView.findViewById(R.id.et_program_description)).addTextChangedListener(mDifferenceToggle);
        ((EditText) rootView.findViewById(R.id.et_onForOne)).addTextChangedListener(mDifferenceToggle);
        ((EditText) rootView.findViewById(R.id.et_offForOne)).addTextChangedListener(mDifferenceToggle);
        ((EditText) rootView.findViewById(R.id.et_onForTwo)).addTextChangedListener(mDifferenceToggle);
        ((EditText) rootView.findViewById(R.id.et_offForTwo)).addTextChangedListener(mDifferenceToggle);
        ((EditText) rootView.findViewById(R.id.et_onForThree)).addTextChangedListener(mDifferenceToggle);
        ((EditText) rootView.findViewById(R.id.et_offForThree)).addTextChangedListener(mDifferenceToggle);
        ((EditText) rootView.findViewById(R.id.et_onForFour)).addTextChangedListener(mDifferenceToggle);
        ((EditText) rootView.findViewById(R.id.et_offForFour)).addTextChangedListener(mDifferenceToggle);
        ((EditText) rootView.findViewById(R.id.et_onForFive)).addTextChangedListener(mDifferenceToggle);
        ((EditText) rootView.findViewById(R.id.et_offForFive)).addTextChangedListener(mDifferenceToggle);

        // setup on focus lost checker
        rootView.findViewById(R.id.et_onForOne).setOnFocusChangeListener(mFocusLostChecker);
        rootView.findViewById(R.id.et_offForOne).setOnFocusChangeListener(mFocusLostChecker);
        rootView.findViewById(R.id.et_onForTwo).setOnFocusChangeListener(mFocusLostChecker);
        rootView.findViewById(R.id.et_offForTwo).setOnFocusChangeListener(mFocusLostChecker);
        rootView.findViewById(R.id.et_onForThree).setOnFocusChangeListener(mFocusLostChecker);
        rootView.findViewById(R.id.et_offForThree).setOnFocusChangeListener(mFocusLostChecker);
        rootView.findViewById(R.id.et_onForFour).setOnFocusChangeListener(mFocusLostChecker);
        rootView.findViewById(R.id.et_offForFour).setOnFocusChangeListener(mFocusLostChecker);
        rootView.findViewById(R.id.et_onForFive).setOnFocusChangeListener(mFocusLostChecker);
        rootView.findViewById(R.id.et_offForFive).setOnFocusChangeListener(mFocusLostChecker);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        /*
         * Initializes the CursorLoader. The PROGRAMS_LOADER value is eventually passed
         * to onCreateLoader().
         */

        if (!mItem.getId().isEmpty()) {
            mListener.makeBusy("Loading", "Loading program from database...");
            getLoaderManager().initLoader(PROGRAMS_LOADER, null, this);
        } else {
            // item is blank, init with default values
            mItem.setName("Untitled Program");
            mItem.setDescription("Best coffee ever.");
            Integer onTimes[] = new Integer[]{50, 50, 0, 0, 0};
            Integer offTimes[] = new Integer[]{20, 0, 0, 0, 0};
            mItem.setOnTimes(onTimes);
            mItem.setOffTimes(offTimes);
            updateUiFromItem();
            ((Button) getView().findViewById(R.id.btn_brew)).setEnabled(true);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.brewing_program, menu);
        ActionBar actionBar = getActivity().getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle("Brew");
        // Locate MenuItem with ShareActionProvider
        MenuItem item = menu.findItem(R.id.menu_item_share);
        // Fetch and store ShareActionProvider
        mShareActionProvider = (ShareActionProvider) item.getActionProvider();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (BrewingProgramDetailCallbacks) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement BrewingProgramDetailCallbacks");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    // Call to update the share intent
    private void setShareIntent() {
        if (mShareActionProvider != null) {
            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.putExtra(Intent.EXTRA_TEXT, mItem.getName() + " " + mItem.getShortUrl());
            shareIntent.setType("text/plain");
            mShareActionProvider.setShareIntent(shareIntent);
        }
    }

    private void updateUiOnChange() {
        mUnsavedChanges = isItemDifferentThanUi();
        Button b = (Button) getView().findViewById(R.id.btn_programDiscardChanges);
        if (b != null) {
            b.setEnabled(mUnsavedChanges);
        }

    }

    private void updateUiFromItem() {
        // do stuff;
        EditText et;
        TextView tv;
        et = (EditText) getView().findViewById(R.id.et_program_name);
        et.setText(mItem.getName());
        et = (EditText) getView().findViewById(R.id.et_program_description);
        et.setText(mItem.getDescription());

        Integer[] onTimes = mItem.getOnTimes();
        Integer[] offTimes = mItem.getOffTimes();
        et = (EditText) getView().findViewById(R.id.et_onForOne);
        et.setText(onTimes[0].toString());
        et = (EditText) getView().findViewById(R.id.et_offForOne);
        et.setText(offTimes[0].toString());
        et = (EditText) getView().findViewById(R.id.et_onForTwo);
        et.setText(onTimes[1].toString());
        et = (EditText) getView().findViewById(R.id.et_offForTwo);
        et.setText(offTimes[1].toString());
        et = (EditText) getView().findViewById(R.id.et_onForThree);
        et.setText(onTimes[2].toString());
        et = (EditText) getView().findViewById(R.id.et_offForThree);
        et.setText(offTimes[2].toString());
        et = (EditText) getView().findViewById(R.id.et_onForFour);
        et.setText(onTimes[3].toString());
        et = (EditText) getView().findViewById(R.id.et_offForFour);
        et.setText(offTimes[3].toString());
        et = (EditText) getView().findViewById(R.id.et_onForFive);
        et.setText(onTimes[4].toString());
        et = (EditText) getView().findViewById(R.id.et_offForFive);
        et.setText(offTimes[4].toString());

        tv = (TextView) getView().findViewById(R.id.tv_createdAt);
        tv.setText(mItem.getCreatedAt());
        tv = (TextView) getView().findViewById(R.id.tv_modifiedAt);
        tv.setText(mItem.getModifiedAt());

        mListener.makeNotBusy();
        if (mItem.getShortUrl().isEmpty()) {
            try {
                AsyncTask<BrewingProgram, Integer, Boolean> task = new ShortenUrlTask().execute(mItem);
            } catch (Exception e) {
                System.out.println("ShortenUrlTask was interrupted: " + e.getLocalizedMessage());
            }
        } else {
            setShareIntent();
        }

    }

    private void updateItemFromUi() {
        // do stuff;
        EditText v;
        v = (EditText) getView().findViewById(R.id.et_program_name);
        mItem.setName(v.getText());
        v = (EditText) getView().findViewById(R.id.et_program_description);
        mItem.setDescription(v.getText());

        Integer[] onTimes = mItem.getOnTimes();
        Integer[] offTimes = mItem.getOffTimes();
        v = (EditText) getView().findViewById(R.id.et_onForOne);
        ifEmptySetZero(v);
        onTimes[0] = Integer.valueOf(v.getText().toString());
        v = (EditText) getView().findViewById(R.id.et_offForOne);
        ifEmptySetZero(v);
        offTimes[0] = Integer.valueOf(v.getText().toString());
        v = (EditText) getView().findViewById(R.id.et_onForTwo);
        ifEmptySetZero(v);
        onTimes[1] = Integer.valueOf(v.getText().toString());
        v = (EditText) getView().findViewById(R.id.et_offForTwo);
        ifEmptySetZero(v);
        offTimes[1] = Integer.valueOf(v.getText().toString());
        v = (EditText) getView().findViewById(R.id.et_onForThree);
        ifEmptySetZero(v);
        onTimes[2] = Integer.valueOf(v.getText().toString());
        v = (EditText) getView().findViewById(R.id.et_offForThree);
        ifEmptySetZero(v);
        offTimes[2] = Integer.valueOf(v.getText().toString());
        v = (EditText) getView().findViewById(R.id.et_onForFour);
        ifEmptySetZero(v);
        onTimes[3] = Integer.valueOf(v.getText().toString());
        v = (EditText) getView().findViewById(R.id.et_offForFour);
        ifEmptySetZero(v);
        offTimes[3] = Integer.valueOf(v.getText().toString());
        v = (EditText) getView().findViewById(R.id.et_onForFive);
        ifEmptySetZero(v);
        onTimes[4] = Integer.valueOf(v.getText().toString());
        v = (EditText) getView().findViewById(R.id.et_offForFive);
        ifEmptySetZero(v);
        offTimes[4] = Integer.valueOf(v.getText().toString());

        mItem.setOnTimes(onTimes);
        mItem.setOffTimes(offTimes);
    }

    private boolean isItemDifferentThanUi() {
        // do stuff;
        EditText v;
        v = (EditText) getView().findViewById(R.id.et_program_name);
        if (!v.getText().toString().contentEquals(mItem.getName())) {
            return true;
        }
        v = (EditText) getView().findViewById(R.id.et_program_description);
        if (!v.getText().toString().contentEquals(mItem.getDescription())) {
            return true;
        }
        Integer[] onTimes = mItem.getOnTimes();
        Integer[] offTimes = mItem.getOffTimes();
        v = (EditText) getView().findViewById(R.id.et_onForOne);
        if (v.getText().toString().isEmpty() || !Integer.valueOf(v.getText().toString()).equals(onTimes[0])) {
            return true;
        }
        v = (EditText) getView().findViewById(R.id.et_offForOne);
        if (v.getText().toString().isEmpty() || !Integer.valueOf(v.getText().toString()).equals(offTimes[0])) {
            return true;
        }
        v = (EditText) getView().findViewById(R.id.et_onForTwo);
        if (v.getText().toString().isEmpty() || !Integer.valueOf(v.getText().toString()).equals(onTimes[1])) {
            return true;
        }
        v = (EditText) getView().findViewById(R.id.et_offForTwo);
        if (v.getText().toString().isEmpty() || !Integer.valueOf(v.getText().toString()).equals(offTimes[1])) {
            return true;
        }
        v = (EditText) getView().findViewById(R.id.et_onForThree);
        if (v.getText().toString().isEmpty() || !Integer.valueOf(v.getText().toString()).equals(onTimes[2])) {
            return true;
        }
        v = (EditText) getView().findViewById(R.id.et_offForThree);
        if (v.getText().toString().isEmpty() || !Integer.valueOf(v.getText().toString()).equals(offTimes[2])) {
            return true;
        }
        v = (EditText) getView().findViewById(R.id.et_onForFour);
        if (v.getText().toString().isEmpty() || !Integer.valueOf(v.getText().toString()).equals(onTimes[3])) {
            return true;
        }
        v = (EditText) getView().findViewById(R.id.et_offForFour);
        if (v.getText().toString().isEmpty() || !Integer.valueOf(v.getText().toString()).equals(offTimes[3])) {
            return true;
        }
        v = (EditText) getView().findViewById(R.id.et_onForFive);
        if (v.getText().toString().isEmpty() || !Integer.valueOf(v.getText().toString()).equals(onTimes[4])) {
            return true;
        }
        v = (EditText) getView().findViewById(R.id.et_offForFive);
        if (v.getText().toString().isEmpty() || !Integer.valueOf(v.getText().toString()).equals(offTimes[4])) {
            return true;
        }
        return false;
    }

    private void ifEmptySetZero(EditText v) {
        if (v.getText().toString().isEmpty()) {
            v.setText("0");
        }
    }

    private void on_brewButtonClicked(View v) {
        if (mListener != null) {
            updateItemFromUi();
            mListener.brewProgram(mItem);
        }
    }

    private void on_discardChangesClicked(View v) {
        // discard
        updateUiFromItem();
    }

    private void saveOrUpdateRecord(View v) {
        if (mListener.saveOrCreateItem(mItem)) {
            Toast.makeText(getActivity(), getString(R.string.notify_changesSavedSuccess), Toast.LENGTH_SHORT);
        } else {
            Toast.makeText(getActivity(), getString(R.string.notify_changesSavedFailure), Toast.LENGTH_SHORT);
        }
        updateUiOnChange();
    }


    /**
     * ***********
     * INTERFACES
     * ************
     */

    // for View.onClickListener interface
    // handle onClick in the fragment, yay Android Fragments
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_brew:
                on_brewButtonClicked(v);
                break;
            case R.id.btn_programDiscardChanges:
                on_discardChangesClicked(v);
                break;
        }
    }

    public void saveIfNecessary() {
        if (mUnsavedChanges) {
            updateItemFromUi();
            try {
                AsyncTask<BrewingProgram, Integer, Boolean> task = new ShortenUrlTask().execute(mItem);
                task.get(); //block on task to keep this alive
            } catch (Exception e) {
                System.out.println("ShortenUrlTask was interrupted: " + e.getLocalizedMessage());
            }
            // the save commands in the Async task will fail here if called from the activity itself
            // (as happens when the back button is intercepted and this is called)
            mListener.saveOrCreateItem(mItem);
            mListener.makeNotBusy(); // kill the busy spinner
        }
    }

    /*
    * Callback that's invoked when the system has initialized the Loader and
    * is ready to start the query. This usually happens when initLoader() is
    * called. The loaderID argument contains the ID value passed to the
    * initLoader() call.
    */
    @Override
    public Loader<Cursor> onCreateLoader(int loaderID, Bundle bundle) {
        return new CursorLoader(
                getActivity(),   // Parent activity context
                ContentUris.withAppendedId(BrewingProgramContentProvider.CONTENT_URI, Long.parseLong(mItem.getId())),// Table to query
                null,           // null, return every column
                null,            // No selection clause because the URI handles it!!!
                null,            // No selection arguments
                null             // Default sort order
        );
    }

    /*
     * Defines the callback that CursorLoader calls
     * when it's finished its query
     */
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        /**
         * Move the results into the view
         */
        if (cursor.getCount() <= 0) {
            return;
        }
        cursor.moveToFirst();
        try {
            int k = 0;
            mItem.setName(cursor.getString(k++));
            mItem.setDescription(cursor.getString(k++));
            Integer[] onTimes = new Integer[5];
            Integer[] offTimes = new Integer[5];
            onTimes[0] = cursor.getInt((k++));
            offTimes[0] = cursor.getInt((k++));
            onTimes[1] = cursor.getInt((k++));
            offTimes[1] = cursor.getInt((k++));
            onTimes[2] = cursor.getInt((k++));
            offTimes[2] = cursor.getInt((k++));
            onTimes[3] = cursor.getInt((k++));
            offTimes[3] = cursor.getInt((k++));
            onTimes[4] = cursor.getInt((k++));
            offTimes[4] = cursor.getInt((k++));
            mItem.setOnTimes(onTimes);
            mItem.setOffTimes(offTimes);
            // skip one for the original author
            k++;
            mItem.setShortUrl(cursor.getString(k++));
            mItem.setCreatedAt(cursor.getString(k++));
            mItem.setModifiedAt(cursor.getString(k++));
        } catch (Exception e) {
            System.out.println("CURSOR ERROR: " + e.getLocalizedMessage());
        }
        ((Button) getView().findViewById(R.id.btn_brew)).setEnabled(true);
        updateUiFromItem();
    }

    /*
    * Invoked when the CursorLoader is being reset. For example, this is
    * called if the data in the provider changes and the Cursor becomes stale.
    */
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // nothing to do here
    }

    public interface BrewingProgramDetailCallbacks {
        void brewProgram(BrewingProgram theProgram);

        void makeBusy(final CharSequence title, final CharSequence message);

        void makeNotBusy();

        boolean saveOrCreateItem(BrewingProgram aProgram);
    }

    private class ShortenUrlTask extends AsyncTask<BrewingProgram, Integer, Boolean> {
        protected Boolean doInBackground(BrewingProgram... programs) {
            Boolean success = true;
            for (int k = 0; k < programs.length; ++k) {
                success &= programs[k].shortenUrl();
            }
            return success;
        }

        protected void onPreExecute() {
            mListener.makeBusy("Please wait", "Shortening URL...");
        }

        protected void onPostExecute(Boolean result) {
            setShareIntent();
            // since we have a new URL, save if we can
            if (mListener != null) {
                mListener.saveOrCreateItem(mItem);
                mListener.makeNotBusy();
            }
        }
    }
}
