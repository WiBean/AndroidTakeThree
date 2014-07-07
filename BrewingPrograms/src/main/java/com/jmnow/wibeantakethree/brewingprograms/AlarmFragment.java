package com.jmnow.wibeantakethree.brewingprograms;

import android.app.Activity;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link AlarmFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link AlarmFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class AlarmFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_HEATTIME = "heatTime";
    private static final String ARG_HEATTEMP = "heatTemp";
    private static final String ARG_HEATFOR = "heatFor";

    private WiBeanYunState.WiBeanAlarmPack mAlarm = new WiBeanYunState.WiBeanAlarmPack();

    private WiBeanAlarmFragmentInteractionListener mListener;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param goalTempInCelsius Desired temperature to pre-populate the temperature slider.  If
     *                          value is out of bounds, will be set to global default (see control).
     * @param minutesAfterMidnight Used to initialize the TimePicker control.
     * @param onForInMinutes  This controls how long the unit will automatically stay heating before shutting
     *               off.  NOTE: if a user manually takes control during this time, the auto-off
     *               timer is then immediately disabled, and the device must be manually shutdown.
     * @return A new instance of fragment AlarmFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AlarmFragment newInstance(int goalTempInCelsius, int minutesAfterMidnight, int onForInMinutes) {
        AlarmFragment fragment = new AlarmFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_HEATTEMP, goalTempInCelsius);
        args.putInt(ARG_HEATTIME, minutesAfterMidnight);
        args.putInt(ARG_HEATFOR, onForInMinutes);
        fragment.setArguments(args);
        return fragment;
    }
    public AlarmFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mAlarm.mOnTimeAsMinutesAfterMidnight = getArguments().getInt(ARG_HEATTIME, 480);
            mAlarm.mHeatTempInCelsius = getArguments().getInt(ARG_HEATTEMP, 25);
            mAlarm.mOnForInMinutes = getArguments().getInt(ARG_HEATFOR, 10);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_alarm, container, false);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (WiBeanAlarmFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement WiBeanAlarmFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }



    public interface WiBeanAlarmFragmentInteractionListener {
        /* **
         * TODO: Update argument type and name so host controller can set alarm.  Only needed
         * for SPARK micro-controller.
         */
        public boolean sendAlarmRequest(WiBeanYunState.WiBeanAlarmPack requestedAlarm);
        public WiBeanYunState.WiBeanAlarmPack requestAlarmState();
    }

}
