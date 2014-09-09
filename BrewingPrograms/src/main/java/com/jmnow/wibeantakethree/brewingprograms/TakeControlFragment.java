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
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.jmnow.wibeantakethree.brewingprograms.wibean.WiBeanSparkState;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link com.jmnow.wibeantakethree.brewingprograms.TakeControlFragment.TakeControlFragmentListener} interface
 * to handle interaction events.
 * Use the {@link TakeControlFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TakeControlFragment extends Fragment implements View.OnClickListener {
    private static final String ARG_CREDENTIALS_VALID = "in_control";

    private boolean mCredentialsValid = false;

    private TakeControlFragmentListener mListener;

    private Button mConnectButton;
    private Button mScanDeviceId;
    private Button mScanAccessToken;
    private EditText mDeviceId_editText;
    private EditText mAccessToken_editText;
    private EditText mGoalTemperature_editText;

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
        args.putBoolean(ARG_CREDENTIALS_VALID, inControl);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        if (getArguments() != null) {
            mCredentialsValid = getArguments().getBoolean(ARG_CREDENTIALS_VALID, false);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_take_control, container, false);

        // hookup convenience members
        mConnectButton = (Button) v.findViewById(R.id.btn_testCredentials);
        mScanDeviceId = (Button) v.findViewById(R.id.btn_scanDeviceId);
        mScanAccessToken = (Button) v.findViewById(R.id.btn_scanAccessToken);
        mDeviceId_editText = (EditText) v.findViewById(R.id.et_deviceId);
        mAccessToken_editText = (EditText) v.findViewById(R.id.et_accessToken);
        ;
        mGoalTemperature_editText = (EditText) v.findViewById(R.id.et_goalTemperature);
        ;
        // hookup the button here in the Fragment
        // (onClicks generated from buttons in Fragments get sent to their Activity
        // removing modularity)
        mConnectButton.setOnClickListener(this);
        mScanDeviceId.setOnClickListener(this);
        mScanAccessToken.setOnClickListener(this);
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // if we have a value in the store, load it and then connect
        String deviceId = "";
        String accessToken = "";
        String brewTemp = "";
        try {
            SharedPreferences prefs = getActivity().getPreferences(Context.MODE_PRIVATE);
            deviceId = prefs.getString(WiBeanSparkState.PREF_KEY_DEVICE_ID, "");
            accessToken = prefs.getString(WiBeanSparkState.PREF_KEY_ACCESS_TOKEN, "");
            brewTemp = prefs.getString(WiBeanSparkState.PREF_KEY_BREW_TEMP, "");
        } catch (Exception e) {
            System.out.println("FATAL Error: sharedPreference for credentials exists as wrong type???");
        }
        boolean testCredentials = false;
        boolean needCredential = false;
        if (!deviceId.isEmpty()) {
            // populate the dialog
            View v = getView();
            EditText ipText = (EditText) v.findViewById(R.id.et_deviceId);
            ipText.setText(deviceId);
            // try auto connect if we aren't in control
            testCredentials = true;
        } else {
            needCredential = true;
        }
        if (!accessToken.isEmpty()) {
            // populate the dialog
            View v = getView();
            EditText ipText = (EditText) v.findViewById(R.id.et_accessToken);
            ipText.setText(accessToken);
            // try auto connect if we aren't in control
            testCredentials = true;
        } else {
            needCredential = true;
        }
        if (needCredential) {
            mListener.alertUser("Need setting", getString(R.string.alert_ip_error_message));
        }
        if (testCredentials) {
            if (!mCredentialsValid) {
                btn_testCredentials_onClick(getView());
            }
        }
        // populate the temperature
        if (!brewTemp.isEmpty()) {
            ((EditText) getView().findViewById(R.id.et_goalTemperature)).setText(brewTemp);
        }
        // ensure the UI is initialized to the right state
        if (mCredentialsValid) {
            setCredentialsValid();
        } else {
            setCredentialsInvalid();
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
            case R.id.btn_testCredentials:
                btn_testCredentials_onClick(v);
                break;
            case R.id.btn_scanAccessToken:
                onClick_pullAccessTokenFromBarcode(v);
                break;
            case R.id.btn_scanDeviceId:
                onClick_pullDeviceIdFromBarcode(v);
                break;
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
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public void setCredentialsValid() {
        // assume whoever called this was finished with any network/pending operations
        enableInputs();
        mCredentialsValid = true;
    }

    public void setCredentialsInvalid() {
        // assume whoever called this was finished with any network/pending operations
        enableInputs();
        mCredentialsValid = false;
    }

    public void onClick_pullAccessTokenFromBarcode(View v) {
        mListener.setTargetControl(R.id.et_accessToken);
        IntentIntegrator integrator = new IntentIntegrator(getActivity());
        integrator.initiateScan();
    }

    public void onClick_pullDeviceIdFromBarcode(View v) {
        mListener.setTargetControl(R.id.et_deviceId);
        IntentIntegrator integrator = new IntentIntegrator(getActivity());
        integrator.initiateScan();
    }

    public void btn_testCredentials_onClick(View v) {
        //toggle
        //disable buttons
        disableInputs();
        // use the current values to update the database
        EditText etDeviceId = (EditText) getView().findViewById(R.id.et_deviceId);
        String deviceId = etDeviceId.getText().toString();
        EditText etAccessToken = (EditText) getView().findViewById(R.id.et_accessToken);
        String accessToken = etAccessToken.getText().toString();

        String currentTemp = ((EditText) getView().findViewById(R.id.et_goalTemperature)).getText().toString();
        if (deviceId.isEmpty() || accessToken.isEmpty()) {
            mListener.alertUser(getString(R.string.alert_ip_error_title), getString(R.string.alert_ip_error_message));
            mCredentialsValid = false;
            return;
        }
        // else continue
        SharedPreferences.Editor prefsEdit = getActivity().getPreferences(Context.MODE_PRIVATE).edit();
        prefsEdit.putString(WiBeanSparkState.PREF_KEY_DEVICE_ID, deviceId);
        prefsEdit.putString(WiBeanSparkState.PREF_KEY_ACCESS_TOKEN, accessToken);
        prefsEdit.putString(WiBeanSparkState.PREF_KEY_BREW_TEMP, currentTemp);
        prefsEdit.commit();
        Toast.makeText(getActivity(), R.string.action_testCredentials_toast, Toast.LENGTH_SHORT);
        try {
            mListener.temperaturePollLoop();
        } catch (Exception e) {
            //responseText.setText("Err chk heat: " + e.getMessage() + ' ' + e.getClass());
            System.out.println("TakeControl Failed: " + e.getMessage() + ' ' + e.getClass());
            //return false;
        }
    }

    private void enableInputs() {
        mConnectButton.setEnabled(true);
        mAccessToken_editText.setEnabled(true);
        mScanAccessToken.setEnabled(true);
        mScanDeviceId.setEnabled(true);
        mDeviceId_editText.setEnabled(true);
        mGoalTemperature_editText.setEnabled(true);
    }

    private void disableInputs() {
        mConnectButton.setEnabled(false);
        mAccessToken_editText.setEnabled(false);
        mScanAccessToken.setEnabled(false);
        mScanDeviceId.setEnabled(false);
        mDeviceId_editText.setEnabled(false);
        mGoalTemperature_editText.setEnabled(false);
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     */
    public interface TakeControlFragmentListener {
        public void alertUser(String title, String message);

        public void temperaturePollLoop();

        public void setTargetControl(int viewId);
    }

}
