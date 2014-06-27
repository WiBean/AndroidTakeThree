package com.jmnow.wibean.testeditprograms;

import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import com.jmnow.wibean.testeditprograms.StaticContent.BuiltinBrewingPrograms;
import com.jmnow.wibean.testeditprograms.dummy.DummyContent;
import com.jmnow.wibean.testeditprograms.StaticContent.BuiltinBrewingPrograms.BrewingProgram;

/**
 * A fragment representing a single BrewingProgram detail screen.
 * This fragment is either contained in a {@link BrewingProgramListActivity}
 * in two-pane mode (on tablets) or a {@link BrewingProgramDetailActivity}
 * on handsets.
 */
public class BrewingProgramDetailFragment extends Fragment {
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_ITEM_ID = "item_id";

    /**
     * The dummy content this fragment is presenting.
     */
    //private DummyContent.DummyItem mItem;
    private BuiltinBrewingPrograms.BrewingProgram mItem;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public BrewingProgramDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_ITEM_ID)) {
            // Load the dummy content specified by the fragment
            // arguments. In a real-world scenario, use a Loader
            // to load content from a content provider.
            mItem = BuiltinBrewingPrograms.ITEM_MAP.get(getArguments().getString(ARG_ITEM_ID));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_brewingprogram_detail, container, false);

        // Show the dummy content as text in a TextView.
        if (mItem != null) {
            ((TextView) rootView.findViewById(R.id.brewingprogram_detail)).setText(mItem.name + " - onTimes: " + mItem.onTimes.toString());
        }

        return rootView;
    }
}
