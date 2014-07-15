package com.jmnow.wibeantakethree.brewingprograms.data;

/**
 * Represents a single brewing program.
 */

public class BrewingProgram {
    public static final Integer NUMONOFFTIMES = 5;
    private Integer[] onTimes = new Integer[NUMONOFFTIMES];
    private Integer[] offTimes = new Integer[NUMONOFFTIMES];
    private String id = "";
    private String name = "";
    private String description = "";
    private String createdAt = "";
    private String modifiedAt = "";

    public BrewingProgram(CharSequence id, CharSequence name) {
        this.id = id.toString();
        this.name = name.toString();
        this.description = "";
        for (Integer k = 0; k < NUMONOFFTIMES; ++k) {
            onTimes[k] = 0;
            offTimes[k] = 0;
        }
    }

    public BrewingProgram(CharSequence id, CharSequence name, CharSequence description, Integer[] onTimes, Integer[] offTimes) {
        this.id = id.toString();
        this.name = name.toString();
        this.description = description.toString();
        this.onTimes = onTimes;
        this.offTimes = offTimes;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getModifiedAt() {
        return modifiedAt;
    }

    public void setModifiedAt(String modifiedAt) {
        this.modifiedAt = modifiedAt;
    }

    public String getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public String getDescription() {
        return this.description;
    }

    public Integer[] getOnTimes() {
        return this.onTimes;
    }

    public Integer[] getOffTimes() {
        return this.offTimes;
    }

    public boolean setId(String id) {
        this.id = id;
        return true;
    }

    public boolean setName(CharSequence name) {
        this.name = name.toString();
        return true;
    }

    public boolean setDescription(CharSequence description) {
        this.description = description.toString();
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
        return name + description;
    }
}
