package com.jmnow.wibeantakethree.brewingprograms.data;

import java.net.URI;

/**
 * Represents a single brewing program.
 */

public class BrewingProgram {
    public static final Integer NUMONOFFTIMES = 5;
    private Integer[] onTimes = new Integer[NUMONOFFTIMES];
    private Integer[] offTimes = new Integer[NUMONOFFTIMES];
    private String mId = "";
    private String mName = "";
    private String mDescription = "";
    private String mCreatedAt = "";
    private String mModifiedAt = "";

    public BrewingProgram(CharSequence id, CharSequence name) {
        this.mId = id.toString();
        this.mName = name.toString();
        this.mDescription = "";
        for (Integer k = 0; k < NUMONOFFTIMES; ++k) {
            onTimes[k] = 0;
            offTimes[k] = 0;
        }
    }

    public BrewingProgram(CharSequence id, CharSequence name, CharSequence description, Integer[] onTimes, Integer[] offTimes) {
        this.mId = id.toString();
        this.mName = name.toString();
        this.mDescription = description.toString();
        this.onTimes = onTimes;
        this.offTimes = offTimes;
    }

    public String getCreatedAt() {
        return mCreatedAt;
    }

    public void setCreatedAt(String createdAt) {
        this.mCreatedAt = createdAt;
    }

    public String getModifiedAt() {
        return mModifiedAt;
    }

    public void setModifiedAt(String modifiedAt) {
        this.mModifiedAt = modifiedAt;
    }

    public String getId() {
        return this.mId;
    }

    public String getName() {
        return this.mName;
    }

    public String getDescription() {
        return this.mDescription;
    }

    public Integer[] getOnTimes() {
        return this.onTimes;
    }

    public Integer[] getOffTimes() {
        return this.offTimes;
    }

    public boolean setId(String id) {
        this.mId = id;
        return true;
    }

    public boolean setName(CharSequence name) {
        this.mName = name.toString();
        return true;
    }

    public boolean setDescription(CharSequence description) {
        this.mDescription = description.toString();
        return true;
    }

    public boolean setOnTimes(Integer[] onTimes) {
        if (onTimes.length > BrewingProgram.NUMONOFFTIMES) {
            return false;
        }
        int k = 0; //counter
        // make sure the times they gave are good
        for (; k < onTimes.length; k++) {
            if (onTimes[k] < 0) {
                return false;
            }
        }
        // fill the rest
        for (; k < BrewingProgram.NUMONOFFTIMES; k++) {
            onTimes[k] = 0;
        }
        this.onTimes = onTimes;
        return true;
    }

    public boolean setOffTimes(Integer[] offTimes) {
        if (offTimes.length > BrewingProgram.NUMONOFFTIMES) {
            return false;
        }
        int k = 0; //counter
        // make sure the times they gave are good
        for (; k < offTimes.length; k++) {
            if (offTimes[k] < 0) {
                return false;
            }
        }
        // fill the rest
        for (; k < BrewingProgram.NUMONOFFTIMES; k++) {
            offTimes[k] = 0;
        }
        this.offTimes = offTimes;
        return true;
    }

    @Override
    public String toString() {
        return mName + mDescription;
    }

    public URI toUri() {
        StringBuilder queryString = new StringBuilder();
        queryString.append("name=").append(mName).append("&");
        queryString.append("description=").append(mDescription).append("&");
        queryString.append("modified_at=").append(mModifiedAt).append("&");
        queryString.append("created_at=").append(mCreatedAt).append("&");
        for (int k = 0; k < NUMONOFFTIMES; ++k) {
            queryString.append("onF[").append(k).append("]=").append(onTimes[k]).append("&");
            queryString.append("offF[").append(k).append("]=").append(offTimes[k]).append("&");
        }
        try {
            return new URI("http", "www.wibean.com", "brewingProgram/v1", queryString.toString(), "");
        } catch (Exception e) {
            return null;
        }
    }
}
