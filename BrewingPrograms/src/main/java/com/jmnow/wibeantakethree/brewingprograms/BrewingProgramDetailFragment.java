package com.jmnow.wibeantakethree.brewingprograms;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
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

import com.jmnow.wibeantakethree.brewingprograms.data.BrewingProgram;
import com.jmnow.wibeantakethree.brewingprograms.data.BrewingProgramContentProvider;
import com.jmnow.wibeantakethree.brewingprograms.data.BrewingProgramHelper;

import java.text.SimpleDateFormat;
import java.util.Date;

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
    public TextWatcher mDifferenceToggle = new TextWatcher() {
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
    // Event listener
    BrewingProgramDetailCallbacks mListener;
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
        if (!mItem.getId().isEmpty()) {
            ((Button) rootView.findViewById(R.id.btn_deleteProgram)).setEnabled(true);
        } else {
            //we are creating a fresh one
            ((Button) rootView.findViewById(R.id.btn_saveProgram)).setText(R.string.action_createProgram);
        }
        // hookup the button here in the Fragment
        // (onClicks generated from buttons in Fragments get sent to their Activity
        // removing modularity)
        ((Button) rootView.findViewById(R.id.btn_brew)).setOnClickListener(this);
        ((Button) rootView.findViewById(R.id.btn_saveProgram)).setOnClickListener(this);
        ((Button) rootView.findViewById(R.id.btn_programDiscardChanges)).setOnClickListener(this);
        ((Button) rootView.findViewById(R.id.btn_deleteProgram)).setOnClickListener(this);

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
            getLoaderManager().initLoader(PROGRAMS_LOADER, null, this);
        } else {
            updateUiFromItem();
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
        // now that we have the share button, hook it up
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_TEXT, "My Awesome Brew Program #" + getArguments().getString(ARG_ITEM_ID, "UNDEF"));
        shareIntent.setType("text/plain");
        setShareIntent(shareIntent);
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
    private void setShareIntent(Intent shareIntent) {
        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(shareIntent);
        }
    }

    private void updateUiOnChange() {
        boolean different = isItemDifferentThanUi();
        if (mItem.getId().isEmpty()) {
            ((Button) getView().findViewById(R.id.btn_saveProgram)).setEnabled(different);
            ((Button) getView().findViewById(R.id.btn_programDiscardChanges)).setEnabled(false);
        } else {
            ((Button) getView().findViewById(R.id.btn_saveProgram)).setEnabled(different);
            ((Button) getView().findViewById(R.id.btn_programDiscardChanges)).setEnabled(different);
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
        ifEmptySetZero(v);
        if (!Integer.valueOf(v.getText().toString()).equals(onTimes[0])) {
            return true;
        }
        v = (EditText) getView().findViewById(R.id.et_offForOne);
        ifEmptySetZero(v);
        if (!Integer.valueOf(v.getText().toString()).equals(offTimes[0])) {
            return true;
        }
        v = (EditText) getView().findViewById(R.id.et_onForTwo);
        ifEmptySetZero(v);
        if (!Integer.valueOf(v.getText().toString()).equals(onTimes[1])) {
            return true;
        }
        v = (EditText) getView().findViewById(R.id.et_offForTwo);
        ifEmptySetZero(v);
        if (!Integer.valueOf(v.getText().toString()).equals(offTimes[1])) {
            return true;
        }
        v = (EditText) getView().findViewById(R.id.et_onForThree);
        ifEmptySetZero(v);
        if (!Integer.valueOf(v.getText().toString()).equals(onTimes[2])) {
            return true;
        }
        v = (EditText) getView().findViewById(R.id.et_offForThree);
        ifEmptySetZero(v);
        if (!Integer.valueOf(v.getText().toString()).equals(offTimes[2])) {
            return true;
        }
        v = (EditText) getView().findViewById(R.id.et_onForFour);
        ifEmptySetZero(v);
        if (!Integer.valueOf(v.getText().toString()).equals(onTimes[3])) {
            return true;
        }
        v = (EditText) getView().findViewById(R.id.et_offForFour);
        ifEmptySetZero(v);
        if (!Integer.valueOf(v.getText().toString()).equals(offTimes[3])) {
            return true;
        }
        v = (EditText) getView().findViewById(R.id.et_onForFive);
        ifEmptySetZero(v);
        if (!Integer.valueOf(v.getText().toString()).equals(onTimes[4])) {
            return true;
        }
        v = (EditText) getView().findViewById(R.id.et_offForFive);
        ifEmptySetZero(v);
        if (!Integer.valueOf(v.getText().toString()).equals(offTimes[4])) {
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
        updateUiFromItem();
    }

    private void saveOrUpdateRecord(View v) {
        // move the UI into our object first
        updateItemFromUi();
        // Defines an object to contain the updated values
        ContentValues updateValues = new ContentValues();
        // Sets the updated value and updates the selected words.
        updateValues.put(BrewingProgramHelper.COLUMN_NAME, mItem.getName());
        updateValues.put(BrewingProgramHelper.COLUMN_DESCRIPTION, mItem.getDescription());

        // we can't use the SQLite datetime() function via the ContentValues object, so construct
        // the string ourselves
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date();
        updateValues.put(BrewingProgramHelper.COLUMN_MODIFIED_AT, dateFormat.format(date));

        Integer[] onTimes = mItem.getOnTimes();
        Integer[] offTimes = mItem.getOffTimes();
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

        // catch results
        int rowsUpdated = 0;
        Uri newRow;
        if (mItem.getId().isEmpty()) {
            // insert
            newRow = getActivity().getContentResolver().insert(
                    BrewingProgramContentProvider.CONTENT_URI,  // the user dictionary content URI
                    updateValues); // the columns to update
            // on successful create, change the Create button to a save button and update
            // the local item
            if (!newRow.toString().toString().isEmpty()) {
                //we are creating a fresh one
                ((Button) getView().findViewById(R.id.btn_saveProgram)).setText(R.string.action_saveProgram);
                mItem.setId(newRow.toString());
            }
        } else {
            // update
            rowsUpdated = getActivity().getContentResolver().update(
                    ContentUris.withAppendedId(BrewingProgramContentProvider.CONTENT_URI, Long.parseLong(mItem.getId())),  // the user dictionary content URI
                    updateValues,                       // the columns to update
                    null, // the column to select on
                    null// the value to compare to
            );
        }
        // enable the delete button
        if (!mItem.getId().isEmpty()) {
            ((Button) getView().findViewById(R.id.btn_deleteProgram)).setEnabled(true);
        }
        updateUiOnChange();
    }

    private void on_deleteProgram() {
        new AlertDialog.Builder(getActivity())
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Delete this Program")
                .setMessage("Are you sure?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteProgram();
                    }

                })
                .setNegativeButton("No", null)
                .show();
    }

    private void deleteProgram() {
        if (!mItem.getId().isEmpty()) {
            getActivity().getContentResolver().delete(
                    ContentUris.withAppendedId(BrewingProgramContentProvider.CONTENT_URI, Long.parseLong(mItem.getId())),
                    null, //selector id is contained with the URI
                    null);
            getActivity().onBackPressed();
        }
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
            case R.id.btn_saveProgram:
                saveOrUpdateRecord(v);
                break;
            case R.id.btn_deleteProgram:
                on_deleteProgram();
                break;
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
    }
}
