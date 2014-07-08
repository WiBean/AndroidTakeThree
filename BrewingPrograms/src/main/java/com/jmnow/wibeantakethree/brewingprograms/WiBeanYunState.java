package com.jmnow.wibeantakethree.brewingprograms;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.util.concurrent.TimeUnit;

/**
 * Created by John-Michael on 7/7/2014.
 * This class models the state of a running WiBean unit so that we don't store the remote state
 * in the UI components of the client program (evil).
 */
public class WiBeanYunState {

    public static final String UNIT_IP_PREF_KEY = "REMOTE_IP_ADDRESS";
    // httpClient, make one to save resources
    private final OkHttpClient mHttpClient = new OkHttpClient();
    private WiBeanAlarmPack mAlarm = new WiBeanAlarmPack();
    // we are either in control or not.  In control means actively heating towards a goal temp.
    // REMEMBER: that goal temp can be really low, so effectively heating is 'off'
    private boolean mInControl = false;
    private String mDeviceIp;
    private int mRequestTimeoutInSeconds = 5;


    public WiBeanYunState() {
        setIpAddress("192.168.0.1");
        mHttpClient.setConnectTimeout(5, TimeUnit.SECONDS);
    }

    public boolean takeControl() {
        boolean success = false;
        String targetURL = "http://" + mDeviceIp + "/arduino/heat/25";// + targetTempText.getText().toString().trim();
        try {
            Request request = new Request.Builder().url(targetURL).build();
            Response response = mHttpClient.newCall(request).execute();
            final String bodyAsString = response.body().string();
            //System.out.println("TakeControl Response: Header: " + response.code() + " Body: " + bodyAsString);
            if (bodyAsString.trim().startsWith("thermometerTemperatureInCelsius:")) {
                // only modify member if we know something happened
                mInControl = true;
                return true;
            }
        } catch (Exception e) {
            //responseText.setText("Err chk heat: " + e.getMessage() + ' ' + e.getClass());
            System.out.println("TakeControl Failed: " + e.getMessage() + ' ' + e.getClass());
        }
        return false;
    }

    public boolean returnControl() {
        boolean success = false;
        String targetURL = "http://" + mDeviceIp + "/arduino/off/0";
        try {
            Request request = new Request.Builder().url(targetURL).build();
            Response response = mHttpClient.newCall(request).execute();
            final String bodyAsString = response.body().string();
            if (bodyAsString.trim().startsWith("STOP REQUESTED!")) {
                // only modify the member variable if we know something happened.
                mInControl = false;
                return true;
            }
        } catch (Exception e) {
            //responseText.setText("Err chk heat: " + e.getMessage() + ' ' + e.getClass());
            System.out.println("returnControl Failed: " + e.getMessage() + ' ' + e.getClass());
        }
        return false;
    }


    public boolean setIpAddress(String ipAsString) {
        if (ipAsString.length() > 19) {
            return false;
        }
        mDeviceIp = ipAsString;
        return true;
    }

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
