package com.jmnow.wibeantakethree.brewingprograms;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import com.jmnow.wibeantakethree.brewingprograms.data.BrewingProgramContentProvider;
import com.jmnow.wibeantakethree.brewingprograms.data.BrewingProgramHelper;

/**
 * A list fragment representing a list of BrewingPrograms. This fragment
 * also supports tablet devices by allowing list items to be given an
 * 'activated' state upon selection. This helps indicate which item is
 * currently being viewed in a {@link BrewingProgramDetailFragment}.
 * <p/>
 * Activities containing this fragment MUST implement the {@link Callbacks}
 * interface.
 */
public class BrewingProgramListFragment extends ListFragment implements
        LoaderManager.LoaderCallbacks<Cursor> {

    /**
     * The LOADER instance used here must be identified, whatever you want
     */
    private static final int PROGRAMS_LOADER = 0;
    /**
     * The serialization (saved instance state) Bundle key representing the
     * activated item position. Only used on tablets.
     */
    private static final String STATE_ACTIVATED_POSITION = "activated_position";
    /**
     * A dummy implementation of the {@link Callbacks} interface that does
     * nothing. Used only when this fragment is not attached to an activity.
     */
    private static Callbacks sDummyCallbacks = new Callbacks() {
        @Override
        public void onItemSelected(String id) {
        }
    };
    /**
     * The fragment's current callback object, which is notified of list item
     * clicks.
     */
    private Callbacks mCallbacks = sDummyCallbacks;
    public String[] mFromColumns = {
            BrewingProgramHelper.COLUMN_NAME,
            BrewingProgramHelper.COLUMN_DESCRIPTION
    };
    public int[] mToFields = {
            android.R.id.text1,
            android.R.id.text2
    };
    /**
     * CursorAdapter for the ListView, along with column mappings
     */
    SimpleCursorAdapter mAdapter = null;
    /**
     * List of columns which are taken from the database to power the ListView
     * Used in connection with the CursorLoader
     */
    String[] mProjection =
            {
                    BrewingProgramHelper.COLUMN_ID_ALIASED,
                    BrewingProgramHelper.COLUMN_NAME,
                    BrewingProgramHelper.COLUMN_DESCRIPTION
            };
    // title shows above
    private CharSequence mTitle;
    /**
     * The current activated item position. Only used on tablets.
     */
    private int mActivatedPosition = ListView.INVALID_POSITION;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public BrewingProgramListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        /*
         * Defines a SimpleCursorAdapter for the ListView
         * Utilizes built in Android resources (note android.R....)
         */
        mAdapter =
                new SimpleCursorAdapter(
                        getActivity(),                // Current context
                        android.R.layout.simple_list_item_2,  // Layout for a single row
                        null,                // No Cursor yet
                        mFromColumns,        // Cursor columns to use
                        mToFields,           // Layout fields to use
                        0                    // No flags
                );
        // Sets the adapter for the view
        setListAdapter(mAdapter);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Restore the previously serialized activated item position.
        if (savedInstanceState != null
                && savedInstanceState.containsKey(STATE_ACTIVATED_POSITION)) {
            setActivatedPosition(savedInstanceState.getInt(STATE_ACTIVATED_POSITION));
        }
        /*
         * Initializes the CursorLoader. The PROGRAMS_LOADER value is eventually passed
         * to onCreateLoader().
         */
        getLoaderManager().initLoader(PROGRAMS_LOADER, null, this);

        // setup the long press for delete
        getListView().setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                final Long idToDelete = id;
                new AlertDialog.Builder(getActivity())
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setTitle("Delete this Program")
                        .setMessage("Are you sure?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                getActivity().getContentResolver().delete(
                                        ContentUris.withAppendedId(BrewingProgramContentProvider.CONTENT_URI, Long.valueOf(idToDelete)),
                                        null, //selector id is contained with the URI
                                        null);
                            }

                        })
                        .setNegativeButton("No", null)
                        .show();
                return true;
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.brewing_program_list, menu);
        ActionBar actionBar = getActivity().getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle("Programs");
        // Hookup the Create new button
        MenuItem item = menu.findItem(R.id.menu_item_create_new);
        item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                mCallbacks.onItemSelected("");
                return true;
            }
        });
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // Activities containing this fragment must implement its callbacks.
        if (!(activity instanceof Callbacks)) {
            throw new IllegalStateException("Activity must implement fragment's callbacks.");
        }
        mCallbacks = (Callbacks) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();

        // Reset the active callbacks interface to the dummy implementation.
        mCallbacks = sDummyCallbacks;
    }

    @Override
    public void onListItemClick(ListView listView, View view, int position, long id) {
        super.onListItemClick(listView, view, position, id);

        // Notify the active callbacks interface (the activity, if the
        // fragment is attached to one) that an item has been selected.
        mCallbacks.onItemSelected(Long.valueOf(id).toString());
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mActivatedPosition != ListView.INVALID_POSITION) {
            // Serialize and persist the activated item position.
            outState.putInt(STATE_ACTIVATED_POSITION, mActivatedPosition);
        }
    }


    /**
     * Turns on activate-on-click mode. When this mode is on, list items will be
     * given the 'activated' state when touched.
     */
    public void setActivateOnItemClick(boolean activateOnItemClick) {
        // When setting CHOICE_MODE_SINGLE, ListView will automatically
        // give items the 'activated' state when touched.
        getListView().setChoiceMode(activateOnItemClick
                ? ListView.CHOICE_MODE_SINGLE
                : ListView.CHOICE_MODE_NONE);
    }

    private void setActivatedPosition(int position) {
        if (position == ListView.INVALID_POSITION) {
            getListView().setItemChecked(mActivatedPosition, false);
        } else {
            getListView().setItemChecked(position, true);
        }

        mActivatedPosition = position;
    }

    // ******************************
    // ** CALLBACKS FOR INTERFACES
    // *****************************

    /*
    * Callback that's invoked when the system has initialized the Loader and
    * is ready to start the query. This usually happens when initLoader() is
    * called. The loaderID argument contains the ID value passed to the
    * initLoader() call.
    */
    @Override
    public Loader<Cursor> onCreateLoader(int loaderID, Bundle bundle) {
    /*
     * Takes action based on the ID of the Loader that's being created
     */
        switch (loaderID) {
            case PROGRAMS_LOADER:
                return new CursorLoader(
                        getActivity(),   // Parent activity context
                        BrewingProgramContentProvider.CONTENT_URI,// Table to query
                        mProjection,     // Projection to return
                        null,            // No selection clause
                        null,            // No selection arguments
                        "datetime(" + BrewingProgramHelper.COLUMN_MODIFIED_AT + ") DESC" // Default sort order
                );
            default:
                // An invalid id was passed in
                return null;
        }
    }

    /*
     * Defines the callback that CursorLoader calls
     * when it's finished its query
     */
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
    /*
     * Moves the query results into the adapter, causing the
     * ListView fronting this adapter to re-display
     */
        mAdapter.changeCursor(cursor);
    }

    /*
     * Invoked when the CursorLoader is being reset. For example, this is
     * called if the data in the provider changes and the Cursor becomes stale.
     */
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    /*
     * Clears out the adapter's reference to the Cursor.
     * This prevents memory leaks.
     */
        mAdapter.changeCursor(null);
    }

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callbacks {
        /**
         * Callback for when an item has been selected.
         */
        public void onItemSelected(String id);
    }
}
