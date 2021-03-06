package com.jmnow.wibeantakethree.brewingprograms;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.jmnow.wibeantakethree.brewingprograms.wibean.WiBeanYunState;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link com.jmnow.wibeantakethree.brewingprograms.TakeControlFragment.TakeControlFragmentListener} interface
 * to handle interaction events.
 * Use the {@link TakeControlFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TakeControlFragment extends Fragment implements View.OnClickListener {
    private static final String ARG_IN_CONTROL = "in_control";

    private boolean mInControl = false;

    private TakeControlFragmentListener mListener;

    public TakeControlFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param inControl Boolean describing whether or not the fragment state "in control" is active.
     * @return A new instance of fragment TakeControlFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static TakeControlFragment newInstance(boolean inControl) {
        TakeControlFragment fragment = new TakeControlFragment();
        Bundle args = new Bundle();
        args.putBoolean(ARG_IN_CONTROL, inControl);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        if (getArguments() != null) {
            mInControl = getArguments().getBoolean(ARG_IN_CONTROL, false);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_take_control, container, false);

        // hookup the button here in the Fragment
        // (onClicks generated from buttons in Fragments get sent to their Activity
        // removing modularity)
        Button b = (Button) v.findViewById(R.id.btnTakeControl);
        b.setOnClickListener(this);
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // if we have a value in the store, load it and then connect
        String ipAddress = "";
        String brewTemp = "";
        try {
            SharedPreferences prefs = getActivity().getPreferences(Context.MODE_PRIVATE);
            ipAddress = prefs.getString(WiBeanYunState.PREF_KEY_UNIT_IP, "");
            brewTemp = prefs.getString(WiBeanYunState.PREF_KEY_BREW_TEMP, "");
        } catch (Exception e) {
            System.out.println("FATAL Error: sharedPreference for IP Address exists as wrong type???");
        }
        if (!ipAddress.isEmpty()) {
            // populate the dialog
            View v = getView();
            EditText ipText = (EditText) v.findViewById(R.id.etIpAddress);
            ipText.setText(ipAddress);
            // try auto connect if we aren't in control
            if (!mInControl) {
                btnTakeControl_onClick(v);
            }
        } else {
            mListener.alertUser("Need setting", getString(R.string.dialog_ip_error_message));
        }
        // populate the temperature
        if (!brewTemp.isEmpty()) {
            ((EditText) getView().findViewById(R.id.et_goalTemperature)).setText(brewTemp);
        }
        // ensure the UI is initialized to the right state
        if (mInControl) {
            setInControl();
        } else {
            setNoControl();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        //inflater.inflate(R.menu.brewing_program, menu);  We don't need extra menus here
        ActionBar actionBar = getActivity().getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle("Take Control");
    }

    // handle onClick in the fragment, yay Android Fragments
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnTakeControl:
                btnTakeControl_onClick(v);
                break;
        }
    }


    public void setInControl() {
        mInControl = true;
        View v = getView();
        if (v != null) {
            Button b = (Button) v.findViewById(R.id.btnTakeControl);
            b.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.control_button_hot_selector), null, null);
            b.setText(R.string.action_release_control);
        }
    }

    public void setNoControl() {
        mInControl = false;
        View v = getView();
        if (v != null) {
            Button b = (Button) v.findViewById(R.id.btnTakeControl);
            b.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.control_button_cold_selector), null, null);
            b.setText(R.string.action_take_control);
        }
    }


    public void btnTakeControl_onClick(View v) {
        //toggle
        // use the current ip value to update the database
        EditText etIp = (EditText) getView().findViewById(R.id.etIpAddress);
        String currentIp = etIp.getText().toString();
        String currentTemp = ((EditText) getView().findViewById(R.id.et_goalTemperature)).getText().toString();
        if (currentIp.isEmpty()) {
            mListener.alertUser(getString(R.string.dialog_ip_error_title), getString(R.string.dialog_ip_error_message));
            return;
        }
        // else continue
        SharedPreferences.Editor prefsEdit = getActivity().getPreferences(Context.MODE_PRIVATE).edit();
        prefsEdit.putString(WiBeanYunState.PREF_KEY_UNIT_IP, currentIp);
        prefsEdit.putString(WiBeanYunState.PREF_KEY_BREW_TEMP, currentTemp);
        prefsEdit.commit();
        try {
            if (!mInControl) {
                mListener.takeControl();
            } else {
                mListener.returnControl();
            }
        } catch (Exception e) {
            //responseText.setText("Err chk heat: " + e.getMessage() + ' ' + e.getClass());
            System.out.println("TakeControl Failed: " + e.getMessage() + ' ' + e.getClass());
            //return false;
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (TakeControlFragmentListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement TakeControlFragmentListener");
        }
        ((BrewingProgramListActivity) activity).onSectionAttached(0);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     */
    public interface TakeControlFragmentListener {
        public void alertUser(String title, String message);

        public void takeControl();

        public void returnControl();
    }

}
