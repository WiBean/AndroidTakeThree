package com.jmnow.wibeantakethree.brewingprograms;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.TimePicker;

import com.jmnow.wibeantakethree.brewingprograms.wibean.WiBeanYunState;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link AlarmFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link AlarmFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AlarmFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_HEATTIME = "heatTime";
    private static final String ARG_HEATTEMP = "heatTemp";
    private static final String ARG_HEATFOR = "heatFor";
    private static final int SB_MINTEMPINCELSIUS = 40;
    private static final int SB_MAXTEMPINCELSIUS = 99;
    private static final float SB_SPAN = SB_MAXTEMPINCELSIUS - SB_MINTEMPINCELSIUS;
    private static final int TIME_ON_AFTER_MAX_IN_MINUTES = 120;
    private WiBeanYunState.WiBeanAlarmPack mAlarm = new WiBeanYunState.WiBeanAlarmPack();
    private WiBeanAlarmFragmentInteractionListener mListener;

    public AlarmFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param goalTempInCelsius    Desired temperature to pre-populate the temperature slider.  If
     *                             value is out of bounds, will be set to global default (see control).
     * @param minutesAfterMidnight Used to initialize the TimePicker control.
     * @param onForInMinutes       This controls how long the unit will automatically stay heating before shutting
     *                             off.  NOTE: if a user manually takes control during this time, the auto-off
     *                             timer is then immediately disabled, and the device must be manually shutdown.
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
        super.onCreateView(inflater, container, savedInstanceState);
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_alarm, container, false);
        SeekBar sb = (SeekBar) v.findViewById(R.id.sb_goalTemp);
        sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                View v = getView();
                // check if we are actually somewhere where we have the view and should update the screen
                if (v == null) {
                    return;
                }
                String asString = ((Integer) Math.round(SB_MINTEMPINCELSIUS + (SB_SPAN * progress / 100))).toString();
                ((TextView) v.findViewById(R.id.tv_temperatureSeekLabel)).setText(asString + " °C");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        return v;
    }

    @Override
    public void onActivityCreated(Bundle bundle) {
        super.onActivityCreated(bundle);
        setAlarmTime(mAlarm.mOnTimeAsMinutesAfterMidnight);
        setGoalTemp(mAlarm.mHeatTempInCelsius);
        setTimeOnAfter(mAlarm.mOnForInMinutes);
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

    public boolean setAlarmTime(int minutesAfterMidnight) {
        if ((minutesAfterMidnight < 0) || (minutesAfterMidnight > (24 * 60))) {
            return false;
        }
        TimePicker tp = (TimePicker) getView().findViewById(R.id.tp_timePicker);
        tp.setCurrentHour(minutesAfterMidnight / 60);
        tp.setCurrentMinute(minutesAfterMidnight % 60);
        return true;
    }

    public boolean setGoalTemp(int tempInCelsius) {
        if ((tempInCelsius < SB_MINTEMPINCELSIUS) || (tempInCelsius > SB_MAXTEMPINCELSIUS)) {
            return false;
        }
        SeekBar sb = (SeekBar) getView().findViewById(R.id.sb_goalTemp);
        sb.setProgress((int) ((tempInCelsius - SB_MINTEMPINCELSIUS) / SB_SPAN * 100));
        return true;
    }

    public boolean setTimeOnAfter(int timeAfterInMinutes) {
        if ((timeAfterInMinutes < 0) || (timeAfterInMinutes > TIME_ON_AFTER_MAX_IN_MINUTES)) {
            return false;
        }
        EditText et = (EditText) getView().findViewById(R.id.et_onForMinutes);
        et.setText(((Integer) timeAfterInMinutes).toString());

        return true;
    }


    public interface WiBeanAlarmFragmentInteractionListener {
        /* **
         * The YUN interface currently doesn't support alarms, so these don't need to do anything
         */
        public boolean sendAlarmRequest(WiBeanYunState.WiBeanAlarmPack requestedAlarm);

        public WiBeanYunState.WiBeanAlarmPack requestAlarmState();
    }

}
