package com.jmnow.wibeantakethree.brewingprograms;

/**
 * Created by John-Michael Fischer on 7/1/2014.
 * This class manages and maintains the connection and state
 * associated with the Arduino Yun version of the WiBean controller.
 *
 * This class performs network requests.  In Android 4.x+ this needs to occur
 * on a non-gui thread.  The caller is responsible for making sure this happens.
 */
public class WiBeanYunManager {

    // members
    private boolean mConnected;
    private String mIpAddressAsString;
    // constants
    private final int SAFE_TEMP_CELSIUS = 0;

    // **********
    // methods
    // **********
    public WiBeanYunManager() {
        mConnected = false;
        mIpAddressAsString = "";
    }


    //****************
    // ACCESSORS
    // **************
    public boolean isConnected() {
        return mConnected;
    }

    public String getIpAddress() {
        return mIpAddressAsString;
    }
    public boolean setIpAddress(String ipAddress) {
        if( ipAddress.length() > 15 ) {
            return false;
        }
        this.mIpAddressAsString = ipAddress;
        return true;
    }


    //************
    // ACTIONS
    //************
    public boolean connect() {
        if( mIpAddressAsString.isEmpty() ) {
            return false;
        }
        //do stuff
        return true;
    }
}
