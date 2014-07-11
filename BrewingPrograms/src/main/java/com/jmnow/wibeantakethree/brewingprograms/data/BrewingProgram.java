package com.jmnow.wibeantakethree.brewingprograms.data;

/**
 * Represents a single brewing program.
 */

public class BrewingProgram {
    public static final Integer NUMONOFFTIMES = 5;
    public Integer[] onTimes = new Integer[NUMONOFFTIMES];
    public Integer[] offTimes = new Integer[NUMONOFFTIMES];
    public String id;
    public String name;
    public String description;


    public BrewingProgram(String id, String content) {
        this.id = id;
        this.name = content;
        this.description = "";
        for (Integer k = 0; k < NUMONOFFTIMES; ++k) {
            onTimes[k] = 0;
            offTimes[k] = 0;
        }
    }

    public BrewingProgram(String id, String content, String description, Integer[] onTimes, Integer[] offTimes) {
        this.id = id;
        this.name = content;
        this.description = description;
        this.onTimes = onTimes;
        this.offTimes = offTimes;
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

    public boolean setName(String name) {
        this.name = name;
        return true;
    }

    public boolean setDescription(String description) {
        this.description = description;
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
