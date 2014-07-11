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
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ShareActionProvider;
import android.widget.TextView;

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
    //for the share button
    private ShareActionProvider mShareActionProvider;
    /**
     * The content this fragment is presenting.
     */
    private BrewingProgram mItem = new BrewingProgram("-1", "No Name", "No Program",
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
        // hookup the button here in the Fragment
        // (onClicks generated from buttons in Fragments get sent to their Activity
        // removing modularity)
        Button b = (Button) rootView.findViewById(R.id.btn_brew);
        b.setOnClickListener(this);
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
        shareIntent.putExtra(Intent.EXTRA_TEXT, "My Awesome Brew Program #" + getArguments().getString(ARG_ITEM_ID,"UNDEF"));
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
        ((BrewingProgramListActivity) activity).onSectionAttached(0);
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

    private void updateUiFromItem() {
        // do stuff;
        TextView v;
        v = (TextView) getView().findViewById(R.id.tv_program_name);
        v.setText(mItem.getName());
        v = (TextView) getView().findViewById(R.id.tv_program_description);
        v.setText(mItem.getDescription());

        Integer[] onTimes = mItem.getOnTimes();
        Integer[] offTimes = mItem.getOffTimes();
        v = (TextView) getView().findViewById(R.id.tv_onForOne);
        v.setText(onTimes[0].toString());
        v = (TextView) getView().findViewById(R.id.tv_offForOne);
        v.setText(offTimes[0].toString());
        v = (TextView) getView().findViewById(R.id.tv_onForTwo);
        v.setText(onTimes[1].toString());
        v = (TextView) getView().findViewById(R.id.tv_offForTwo);
        v.setText(offTimes[1].toString());
        v = (TextView) getView().findViewById(R.id.tv_onForThree);
        v.setText(onTimes[2].toString());
        v = (TextView) getView().findViewById(R.id.tv_offForThree);
        v.setText(offTimes[2].toString());
        v = (TextView) getView().findViewById(R.id.tv_onForFour);
        v.setText(onTimes[3].toString());
        v = (TextView) getView().findViewById(R.id.tv_offForFour);
        v.setText(offTimes[3].toString());
        v = (TextView) getView().findViewById(R.id.tv_onForFive);
        v.setText(onTimes[4].toString());
        v = (TextView) getView().findViewById(R.id.tv_offForFive);
        v.setText(offTimes[4].toString());
    }

    private void brewButtonClicked(View v) {
        if (mListener != null) {
            mListener.brewProgram(mItem);
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
                brewButtonClicked(v);
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
        boolean brewProgram(BrewingProgram theProgram);
    }
}
