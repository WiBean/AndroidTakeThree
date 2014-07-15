package com.jmnow.wibeantakethree.brewingprograms.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper class containing static list of content for the fragment display.
 * <p/>
 * TODO: Upgrade to flexible SQL based provider for next release
 */
public class BuiltinBrewingPrograms {
    /**
     * An array of sample items used by the ListAdapter.
     */
    public static List<BrewingProgram> ITEMS = new ArrayList<BrewingProgram>();
    /**
     * A map of sample items, by ID, used by the ListAdapter.
     */
    public static Map<String, BrewingProgram> ITEM_MAP = new HashMap<String, BrewingProgram>();

    static {
        Integer[] onTimes = {Integer.valueOf(30), Integer.valueOf(50), Integer.valueOf(0), Integer.valueOf(0)};
        Integer[] offTimes = {Integer.valueOf(30), Integer.valueOf(0), Integer.valueOf(0), Integer.valueOf(0)};
        // Add 3 sample items.
        addItem(new BrewingProgram("1", "B Program 1", "desc", onTimes, offTimes));
        addItem(new BrewingProgram("2", "B Program 2", "desc", onTimes, offTimes));
        addItem(new BrewingProgram("3", "B Program 3", "desc", onTimes, offTimes));
    }

    private static void addItem(BrewingProgram item) {
        ITEMS.add(item);
        ITEM_MAP.put(item.getId(), item);
    }

}
