package com.jmnow.wibean.testeditprograms;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link TakeControlFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link TakeControlFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class TakeControlFragment extends Fragment implements View.OnClickListener{
    private static final String ARG_IN_CONTROL = "in_control";

    private boolean mParamInControl;
    // httpClient, make one to save resources
    OkHttpClient mHttpClient;

    private OnFragmentInteractionListener mListener;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
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
    public TakeControlFragment() {
        // Required empty public constructor
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParamInControl = getArguments().getBoolean(ARG_IN_CONTROL);
        }
        mHttpClient = new OkHttpClient();
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_take_control, container, false);

        // hookup the button here in the Fragment
        // (onClicks generated from buttons in Fragments get sent to their Activity
        // removing modularity)
        Button b = (Button) v.findViewById(R.id.btnTakeControl);
        b.setOnClickListener(this);

        return v;
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

    public void btnTakeControl_onClick(View v) {
        //toggle
        if( takeControl(!mParamInControl) ) {
            mParamInControl = !mParamInControl;
            if (mListener != null) {
                mListener.changeControlState(mParamInControl);
            }
        }
    }

    public boolean takeControl(boolean takeControl) {
        try{
            // launch HTTP request async
            //TODO: pull IP Address from prefs
            String targetURL = "http://192.168.1.143";// + targetIpText.getText().toString().trim();
            if( takeControl ) {
                //TODO: pull temp from prefs
                targetURL += "/arduino/heat/25";// + targetTempText.getText().toString().trim();
            }
            else {
                targetURL += "/arduino/off/0";
            }
        /*
            // Start lengthy operation in a background thread
            new Thread(new Runnable() {
                public void run() {
                    try {
                    while (mProgressStatus < 100) {
                        mProgressStatus = doWork();

                        // Update the progress bar
                        mHandler.post(new Runnable() {
                            public void run() {
                                mProgress.setProgress(mProgressStatus);
                            }
                        });
                    }
                }
            }).start();
            */
            Request request = new Request.Builder().url(targetURL).build();
            Response response = mHttpClient.newCall(request).execute();
            final String bodyAsString = response.body().string();
            System.out.println( "Header: " + response.code() + " Body: " + bodyAsString );
            if( takeControl ) {
                return bodyAsString.trim().startsWith("thermometerTemperatureInCelsius:");
            }
            else {
                return bodyAsString.trim().startsWith("STOP REQUESTED!");
            }
        }
        catch (Exception e) {
            //responseText.setText("Err chk heat: " + e.getMessage() + ' ' + e.getClass());
            System.out.println("TakeControl Failed: " + e.getMessage() + ' ' + e.getClass());
            return false;
        }
    }
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
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
    public interface OnFragmentInteractionListener {
        public void changeControlState(boolean inControl);
    }

}
