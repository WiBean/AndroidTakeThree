package com.jmnow.wibeantakethree.brewingprograms;

/**
 * Created by John-Michael on 7/7/2014.
 * This class models the state of a running WiBean unit so that we don't store the remote state
 * in the UI components of the client program (evil).
 */
public class WiBeanYunState {
    WiBeanAlarmPack mAlarm;


    static public class WiBeanAlarmPack {
        public int mHeatTempInCelsius;
        public int mOnTimeAsMinutesAfterMidnight;
        public int mOnForInMinutes;

        public WiBeanAlarmPack() {
            mHeatTempInCelsius = 25;
            mOnTimeAsMinutesAfterMidnight = 480;
            mOnForInMinutes = 10;
        }
    }
}
