package com.jmnow.wibeantakethree.brewingprograms;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TimePicker;

import com.jmnow.wibeantakethree.brewingprograms.wibean.WiBeanSparkState;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.TimeZone;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link AlarmFragment.WiBeanAlarmFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link AlarmFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AlarmFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_ALARMONTIME = "alarmOnTime";
    private static final String ARG_ALARMTIMEZONE = "alarmTimeZone";
    private static final String PREF_ALARM_TIME_HOUR = "alarmTimeHour";
    private static final String PREF_ALARM_TIME_MINUTE = "alarmTimeMinute";
    private static final String PREF_ALARM_TIMEZONE = "alarmTimeZone";

    private WiBeanSparkState.WiBeanAlarmPackV1 mRemoteAlarm = new WiBeanSparkState.WiBeanAlarmPackV1();
    private WiBeanSparkState.WiBeanAlarmPackV1 mLocalAlarm = new WiBeanSparkState.WiBeanAlarmPackV1();
    private WiBeanAlarmFragmentInteractionListener mListener;

    private TimePicker mTimePicker;
    private Spinner mTimeZoneSpinner;
    private Switch mArmedSwitch;
    private boolean mIgnoreNextSwitchEvent = false;
    private boolean mIgnoreNextSpinnerEvent = false;

    public AlarmFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param alarmPack The current remote alarm pack.
     * @return A new instance of fragment AlarmFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AlarmFragment newInstance(WiBeanSparkState.WiBeanAlarmPackV1 alarmPack) {
        AlarmFragment fragment = new AlarmFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_ALARMONTIME, alarmPack.getOnTimeAsMinutesAfterMidnight());
        args.putInt(ARG_ALARMTIMEZONE, alarmPack.getUtcOffset());
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // pull in any case from prefs
        SharedPreferences prefs = getActivity().getPreferences(Context.MODE_PRIVATE);
        int minutesAfter = prefs.getInt(PREF_ALARM_TIME_HOUR, 0) * 60;
        minutesAfter += prefs.getInt(PREF_ALARM_TIME_MINUTE, 0);
        mLocalAlarm.setOnTimeAsMinutesAfterMidnight(minutesAfter);
        mLocalAlarm.setUtcOffset(prefs.getInt(PREF_ALARM_TIMEZONE, 0));
        // if we have a bundle, bring it in
        if (getArguments() != null) {
            mRemoteAlarm.setOnTimeAsMinutesAfterMidnight(getArguments().getInt(ARG_ALARMONTIME, 480));
            mRemoteAlarm.setUtcOffset(getArguments().getInt(ARG_ALARMTIMEZONE, 0));
        }
        if (mRemoteAlarm.getAlarmArmed()) {
            mLocalAlarm = mRemoteAlarm;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_alarm, container, false);
        mTimePicker = (TimePicker) v.findViewById(R.id.tp_timePicker);
        mTimeZoneSpinner = (Spinner) v.findViewById(R.id.spn_dstOffset);
        mArmedSwitch = (Switch) v.findViewById(R.id.sw_toggleAlarm);

        mArmedSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!mIgnoreNextSwitchEvent) {
                    onClick_toggleAlarm(buttonView);
                }
                mIgnoreNextSwitchEvent = false;
            }
        });

        mTimeZoneSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mLocalAlarm.setUtcOffset(getIntegerFromTimeZoneSpinner(position));
                if (!mIgnoreNextSpinnerEvent && mRemoteAlarm.getAlarmArmed()) {
                    saveAndSendAlarm();
                }
                mIgnoreNextSpinnerEvent = false;
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // populate the TZ spinner
        String[] TZ = TimeZone.getAvailableIDs();
        ArrayList<String> TZ1 = new ArrayList<String>();
        for (int i = 0; i < TZ.length; i++) {
            TimeZone tz = TimeZone.getTimeZone(TZ[i]);

            String display = new DecimalFormat("+00;-00").format(tz.getRawOffset() / 3600000) + " " + tz.getDisplayName();
            if (!TZ1.contains(display)) {
                TZ1.add(display);
            }
        }
        Collections.sort(TZ1, Collections.reverseOrder());
        // find the first -01 netry
        int lastIndex = TZ1.lastIndexOf("+14 Line Islands Time");
        // Then grab the negative chunk, reverse it, and prepend it
        Collections.sort(TZ1.subList(lastIndex, TZ1.size()));
        ArrayAdapter<String> tzAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_dropdown_item, TZ1);
        mIgnoreNextSpinnerEvent = true;
        mTimeZoneSpinner.setAdapter(tzAdapter);

        updateUiFromAlarms();
        return v;
    }


    @Override
    public void onActivityCreated(Bundle bundle) {
        super.onActivityCreated(bundle);
        setAlarmTime(mLocalAlarm.getOnTimeAsMinutesAfterMidnight());
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
    public void onPause() {
        super.onPause();
        // save the users preferences
        SharedPreferences.Editor prefsEdit = getActivity().getPreferences(Context.MODE_PRIVATE).edit();
        prefsEdit.putInt(PREF_ALARM_TIME_HOUR, mTimePicker.getCurrentHour());
        prefsEdit.putInt(PREF_ALARM_TIME_MINUTE, mTimePicker.getCurrentMinute());
        prefsEdit.putInt(PREF_ALARM_TIMEZONE, mTimeZoneSpinner.getSelectedItemPosition());
        prefsEdit.commit();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    private void updateUiFromAlarms() {
        // if we don't have UI yet die
        if (!this.isResumed()) {
            return;
        }
        // if the alarm is armed AND local pref matches the remote alarm, use that ID as it will
        // have the real zone they picked and not just the closest equivalent
        SharedPreferences prefs = getActivity().getPreferences(Context.MODE_PRIVATE);
        mIgnoreNextSpinnerEvent = true;
        if (!mRemoteAlarm.getAlarmArmed() ||
                (mLocalAlarm.getUtcOffset() == getIntegerFromTimeZoneSpinner(prefs.getInt(PREF_ALARM_TIMEZONE, 0)))) {
            mTimeZoneSpinner.setSelection(prefs.getInt(PREF_ALARM_TIMEZONE, 0));
        } else {
            mTimeZoneSpinner.setSelection(findIntegerInTimeZoneSpinner(mLocalAlarm.getUtcOffset()));
        }
        setAlarmTime(mLocalAlarm.getOnTimeAsMinutesAfterMidnight());

        if (mRemoteAlarm.getAlarmArmed() != mArmedSwitch.isChecked()) {
            mIgnoreNextSwitchEvent = true;
            mArmedSwitch.setChecked(mRemoteAlarm.getAlarmArmed());
        }
    }

    private void onClick_toggleAlarm(View v) {
        // die on programmatic calls to the switch
        if (!this.isResumed()) {
            return;
        }
        if (mArmedSwitch.isChecked()) {
            int minutesAfterMidnight = mTimePicker.getCurrentHour() * 60 + mTimePicker.getCurrentMinute();
            mLocalAlarm.setOnTimeAsMinutesAfterMidnight(minutesAfterMidnight);
        } else {
            // set the alarm to 2 minutes more than minutes in a day, then it is deactivated.
            mLocalAlarm.setOnTimeAsMinutesAfterMidnight(WiBeanSparkState.WiBeanAlarmPackV1.MINUTES_IN_DAY + 2);
        }
        mLocalAlarm.setUtcOffset(getIntegerFromTimeZoneSpinner(mTimeZoneSpinner.getSelectedItemPosition()));
        saveAndSendAlarm();
    }

    private void saveAndSendAlarm() {
        // die if the UI isn't constructed
        if (!this.isResumed()) {
            return;
        }
        // save the users preferences
        SharedPreferences.Editor prefsEdit = getActivity().getPreferences(Context.MODE_PRIVATE).edit();
        prefsEdit.putInt(PREF_ALARM_TIME_HOUR, mTimePicker.getCurrentHour());
        prefsEdit.putInt(PREF_ALARM_TIME_MINUTE, mTimePicker.getCurrentMinute());
        prefsEdit.putInt(PREF_ALARM_TIMEZONE, mTimeZoneSpinner.getSelectedItemPosition());
        prefsEdit.commit();
        // send
        mListener.sendAlarmRequest(mLocalAlarm);
    }

    public boolean setAlarmTime(int minutesAfterMidnight) {
        if ((minutesAfterMidnight < 0) || (minutesAfterMidnight > (24 * 60))) {
            return false;
        }
        if (mTimePicker != null) {
            mTimePicker.setCurrentHour(minutesAfterMidnight / 60);
            mTimePicker.setCurrentMinute(minutesAfterMidnight % 60);
        }
        return true;
    }

    /**
     * UTILITIES
     */
    private int getIntegerFromTimeZoneSpinner(int position) {
        if (mTimeZoneSpinner == null) {
            return 0;
        }
        String text = ((String) mTimeZoneSpinner.getItemAtPosition(position)).substring(0, 3);
        text = text.substring((text.charAt(0) == '+') ? 1 : 0, 3);
        return Integer.valueOf(text);
    }

    private int findIntegerInTimeZoneSpinner(int utcOffset) {
        if (mTimeZoneSpinner == null) {
            return 0;
        }
        Integer bigI = Integer.valueOf(utcOffset);
        // start at the offset, because we have at least one per offset
        int k = utcOffset;
        try {
            while (true) {
                String text = ((String) mTimeZoneSpinner.getItemAtPosition(k)).substring(0, 3);
                text = text.substring((text.charAt(0) == '+') ? 1 : 0, 3);
                if (Integer.valueOf(text).equals(bigI)) {
                    return k;
                }
                ++k;
            }
        } catch (Exception e) {
            // none found
            return -1;
        }
    }

    public void updateRemoteAlarm(final WiBeanSparkState.WiBeanAlarmPackV1 newAlarm) {
        // if the remote alarm was changed, update the UI to reflect its current state
        boolean same = newAlarm.equals(mRemoteAlarm);
        mRemoteAlarm = newAlarm;
        if (!same) {
            mLocalAlarm = newAlarm;
            updateUiFromAlarms();
        }
    }

    public interface WiBeanAlarmFragmentInteractionListener {
        /* **
         * The YUN interface currently doesn't support alarms, so these don't need to do anything
         */
        public boolean sendAlarmRequest(WiBeanSparkState.WiBeanAlarmPackV1 requestedAlarm);
    }

}
