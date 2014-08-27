package com.jmnow.wibeantakethree.brewingprograms.wibean;

import com.jmnow.wibeantakethree.brewingprograms.data.BrewingProgram;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONObject;

import java.util.concurrent.TimeUnit;

/**
 * Created by John-Michael on 7/7/2014.
 * This class models the state of a running WiBean unit so that we don't store the remote state
 * in the UI components of the client program (evil).
 */
public class WiBeanSparkState {


    public static final String PREF_KEY_DEVICE_ID = "SPARK_DEVICE_ID";
    public static final String PREF_KEY_ACCESS_TOKEN = "SPARK_ACCESS_TOKEN";
    public static final String PREF_KEY_BREW_TEMP = "BREW_TEMP_IDEAL";
    public static final MediaType TEXT
            = MediaType.parse("application/json; charset=utf-8");
    // constants
    public static final int MIN_TEMP = 20;
    public static final int MAX_TEMP = 120;
    private final String SPARK_BASE_URL = "https://api.spark.io/v1/devices/";
    // httpClient, make one to save resources
    private final OkHttpClient mHttpClient = new OkHttpClient();
    private WiBeanAlarmPackV1 mAlarm = new WiBeanAlarmPackV1();

    private boolean mHeating = false;
    private boolean mBrewing = false;
    private float mDesiredTemperatureInCelsius = 95;

    private float mCurrentHeadTemperatureInCelsius = 0;
    private float mCurrentAmbientTemperatureInCelsius = 0;

    private String mSparkDeviceId;
    private String mSparkAccessToken;
    private int mRequestTimeoutInSeconds;


    public WiBeanSparkState() {
        setRequestTimeout(10);
    }

    public boolean isHeating() {
        return mHeating;
    }

    public boolean isBrewing() {
        return mBrewing;
    }

    public boolean setTemperature(float desiredTemperature) {
        StringBuilder targetURL = new StringBuilder();
        targetURL.append(assembleBaseUrl()).append("heatTarget");
        StringBuilder paramBuilder = new StringBuilder();
        // it doesn't make sense to specify more than single degrees
        paramBuilder.append(String.format("%.0f", mDesiredTemperatureInCelsius));
        RequestBody formBody = new FormEncodingBuilder()
                .add("access_token", mSparkAccessToken)
                .add("params", paramBuilder.toString())
                .build();
        return basicSparkFunctionPost(targetURL.toString(), formBody);
    }

    public boolean makeHeat(float desiredTemperature) {
        mDesiredTemperatureInCelsius = Math.min(Math.max(desiredTemperature, MIN_TEMP), MAX_TEMP);
        return makeHeat();
    }

    // take control of the machine, so WiBean is in charge of heating
    public boolean makeHeat() {
        setTemperature(mDesiredTemperatureInCelsius);

        StringBuilder targetURL = new StringBuilder();
        targetURL.append(assembleBaseUrl()).append("heatToggle");
        StringBuilder paramBuilder = new StringBuilder();
        // it doesn't make sense to specify more than single degrees
        paramBuilder.append(1);
        RequestBody formBody = new FormEncodingBuilder()
                .add("access_token", mSparkAccessToken)
                .add("params", paramBuilder.toString())
                .build();
        boolean success = basicSparkFunctionPost(targetURL.toString(), formBody);
        if (success) {
            mHeating = true;
        }
        return success;
    }

    // reset control of the WiBean device so the machine acts as if we aren't even there.
    public boolean makeHibernate() {
        StringBuilder targetURL = new StringBuilder();
        targetURL.append(assembleBaseUrl()).append("heatToggle");
        StringBuilder paramBuilder = new StringBuilder();
        // it doesn't make sense to specify more than single degrees
        paramBuilder.append(0);
        RequestBody formBody = new FormEncodingBuilder()
                .add("access_token", mSparkAccessToken)
                .add("params", paramBuilder.toString())
                .build();
        boolean success = basicSparkFunctionPost(targetURL.toString(), formBody);
        if (success) {
            mHeating = false;
        }
        return success;
    }

    // take control of the machine, so WiBean is in charge of heating
    public boolean setAlarm(WiBeanAlarmPackV1 alarm) {
        StringBuilder targetURL = new StringBuilder();
        targetURL.append(assembleBaseUrl()).append("toggleAlarm");
        StringBuilder paramBuilder = new StringBuilder();
        // it doesn't make sense to specify more than single degrees
        paramBuilder.append(alarm.getOnTimeAsMinutesAfterMidnight());
        paramBuilder.append(",").append(alarm.getUtcOffset());
        RequestBody formBody = new FormEncodingBuilder()
                .add("access_token", mSparkAccessToken)
                .add("params", paramBuilder.toString())
                .build();
        boolean success = basicSparkFunctionPost(targetURL.toString(), formBody);
        mAlarm = alarm;
        return success;
    }

    public WiBeanAlarmPackV1 getAlarm() {
        return mAlarm;
    }

    private boolean basicSparkFunctionPost(String targetUrl, RequestBody body) {
        Request.Builder builder = new Request.Builder().url(targetUrl);
        if (body != null) {
            builder.post(body);
        }
        Request request = builder.build();
        try {
            Response response = mHttpClient.newCall(request).execute();
            final JSONObject bodyAsObject = new JSONObject(response.body().string().trim().replace("\n", ""));
            if ((response.code() == 200) &&
                    bodyAsObject.has("return_value")) {
                int returnCode = Integer.valueOf(bodyAsObject.getString("return_value"));
                return (returnCode == 1);
            }
        } catch (Exception e) {
            //responseText.setText("Err chk heat: " + e.getMessage() + ' ' + e.getClass());
            System.out.println("basicSparkFunctionPost failed: " + e.getMessage() + ' ' + e.getClass());
        }
        return false;
    }

    // queries for the state of the headTemperature sensor
    public float getHeadTemperature() {
        return mCurrentHeadTemperatureInCelsius;
        // THE OLD WAY
        /*
        String targetURL = assembleBaseUrl() + "headTemp?" + "access_token=" + mSparkAccessToken;
        Request request = new Request.Builder().url(targetURL).build();
        try {
            Response response = mHttpClient.newCall(request).execute();
            final JSONObject bodyAsObject = new JSONObject(response.body().string().trim().replace("\n", ""));
            if ((response.code() == 200) &&
                    bodyAsObject.has("result")) {
                mCurrentHeadTemperatureInCelsius = Float.valueOf(bodyAsObject.getString("result"));
                emptyBuilderForTemperatureReturn.append(String.format("%.1f", mCurrentHeadTemperatureInCelsius));
                return true;
            }
            if ((response.code() == 408)) {
                emptyBuilderForTemperatureReturn.append("LOST?");
                return true;
            }
            if ((response.code() == 403)) {
                // signifies bad credentials
                emptyBuilderForTemperatureReturn.append("ERR");
                return true;
            }
        } catch (Exception e) {
            //responseText.setText("Err chk heat: " + e.getMessage() + ' ' + e.getClass());
            System.out.println("getHeadTemperature Failed: " + e.getMessage() + ' ' + e.getClass());
        }
        // if we get here, something went wrong
        emptyBuilderForTemperatureReturn.append("ERR");
        return true;
        */
    }

    // reset control of the WiBean device so the machine acts as if we aren't even there.
    public boolean runBrewProgram(BrewingProgram theProgram) {
        if (!mHeating) {
            return false;
        }
        boolean success = false;
        Integer[] onTimes = theProgram.getOnTimes();
        Integer[] offTimes = theProgram.getOffTimes();

        StringBuilder targetURL = new StringBuilder();
        targetURL.append(assembleBaseUrl()).append("pumpControl");
        StringBuilder paramBuilder = new StringBuilder();
        for (int k = 0; k < onTimes.length; ++k) {
            paramBuilder.append(onTimes[k]).append(',').append(offTimes[k]).append(',');
        }
        final String targetUrlAsString = targetURL.toString();
        final String paramsAsString = paramBuilder.toString();
        RequestBody formBody = new FormEncodingBuilder()
                .add("access_token", mSparkAccessToken)
                .add("params", paramsAsString)
                .build();
        Request request = new Request.Builder()
                .url(targetUrlAsString)
                .post(formBody)
                .build();
        try {
            Response response = mHttpClient.newCall(request).execute();
            final JSONObject bodyAsObject = new JSONObject(response.body().string().trim().replace("\n", ""));
            if ((response.code() == 200) &&
                    bodyAsObject.has("return_value")) {
                int returnCode = Integer.valueOf(bodyAsObject.getString("return_value"));
                return (returnCode == 1);
            }
        } catch (Exception e) {
            //responseText.setText("Err chk heat: " + e.getMessage() + ' ' + e.getClass());
            System.out.println("runProgram Failed: " + e.getMessage() + ' ' + e.getClass());
        }
        return false;
    }


    // Query and update status
    // reset control of the WiBean device so the machine acts as if we aren't even there.
    public boolean queryStatus() {
        String targetURL = assembleBaseUrl() + "status?" + "access_token=" + mSparkAccessToken;
        Request request = new Request.Builder().url(targetURL).build();
        try {
            Response response = mHttpClient.newCall(request).execute();
            final JSONObject bodyAsObject = new JSONObject(response.body().string().trim().replace("\n", ""));
            if ((response.code() == 200) &&
                    bodyAsObject.has("result")) {
                final JSONObject statusAsObject = new JSONObject(bodyAsObject.getString("result"));
                // object contains the following
                // ala: alarm active
                // alt: alarm time as minutes after midnight (values greater than minutes_in_day imply will never fire)
                // b: boolean is machine currently in pumping cycle?
                // h: boolean, is machine currently seeking a goal temperature
                mAlarm.setOnTimeAsMinutesAfterMidnight(statusAsObject.getInt("alt"));
                mAlarm.setUtcOffset(statusAsObject.getInt("clktz"));
                mHeating = (statusAsObject.getInt("h") != 0);
                mBrewing = statusAsObject.getBoolean("b");
                mCurrentHeadTemperatureInCelsius = (float) statusAsObject.getDouble("t_h");
                mCurrentAmbientTemperatureInCelsius = (float) statusAsObject.getDouble("t_a");
                return true;
            }
            if ((response.code() == 408)) {
                return false;
            }
            if ((response.code() == 403)) {
                // signifies bad credentials
                return false;
            }
        } catch (Exception e) {
            //responseText.setText("Err chk heat: " + e.getMessage() + ' ' + e.getClass());
            System.out.println("queryStatus Failed: " + e.getMessage() + ' ' + e.getClass());
        }
        // if we get here, something went wrong
        return false;
    }

    /**
     * UTILITIES!
     */

    private String assembleBaseUrl() {
        return (SPARK_BASE_URL + mSparkDeviceId + "/");
    }

    public String getSparkDeviceId() {
        return mSparkDeviceId;
    }

    public boolean setSparkDeviceId(String mSparkDeviceId) {
        this.mSparkDeviceId = mSparkDeviceId;
        return true;
    }

    public String getSparkAccessToken() {
        return mSparkAccessToken;
    }

    public boolean setSparkAccessToken(String mSparkAccessToken) {
        this.mSparkAccessToken = mSparkAccessToken;
        return true;
    }

    /**
     * Sets the request timeout of all HTTP requests in the class, in seconds.
     *
     * @param timeout Requested timeout, in seconds.
     * @return True if requested timeout is greater than 1, otherwise false.
     */
    public boolean setRequestTimeout(int timeout) {
        if (timeout < 1) {
            return false;
        }
        mRequestTimeoutInSeconds = timeout;
        mHttpClient.setConnectTimeout(mRequestTimeoutInSeconds, TimeUnit.SECONDS);
        return true;
    }

    static public class WiBeanAlarmPackV1 {
        public static final int MINUTES_IN_DAY = 24 * 60;

        private int mOnTimeAsMinutesAfterMidnight;
        private boolean mAlarmArmed = false;
        private int mUtcOffset = 0;

        public WiBeanAlarmPackV1() {
            mOnTimeAsMinutesAfterMidnight = 480;
        }

        public int getOnTimeAsMinutesAfterMidnight() {
            return mOnTimeAsMinutesAfterMidnight;
        }

        public void setOnTimeAsMinutesAfterMidnight(int mOnTimeAsMinutesAfterMidnight) {
            this.mOnTimeAsMinutesAfterMidnight = mOnTimeAsMinutesAfterMidnight;
            mAlarmArmed = (this.mOnTimeAsMinutesAfterMidnight < MINUTES_IN_DAY);
        }

        public boolean setUtcOffset(int utcOffset) {
            if (Math.abs(utcOffset) > 12) {
                return false;
            } else {
                mUtcOffset = utcOffset;
                return true;
            }
        }

        ;

        public int getUtcOffset() {
            return mUtcOffset;
        }

        public boolean getAlarmArmed() {
            return mAlarmArmed;
        }
    }
}
