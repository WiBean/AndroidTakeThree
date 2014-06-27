package com.jmnow.wibean.testeditprograms.StaticContent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper class containing static list of content for the fragment display.
 * <p>
 * TODO: Upgrade to flexible SQL based provider for next release
 */
public class BuiltinBrewingPrograms {

    /**
     * A dummy item representing a piece of content.
     */
    public static final Integer NUMONOFFTIMES = 4;
    public static class BrewingProgram {
        public String   id;
        public String   name;
        public Integer [] onTimes = {0,0,0,0};
        public Integer [] offTimes = {0,0,0,0};

        public BrewingProgram(String id, String content) {
            this.id = id;
            this.name = content;
            for(Integer k=0;k<NUMONOFFTIMES;++k) {
                onTimes[k] = 0;
                offTimes[k] = 0;
            }
        }
        public BrewingProgram(String id, String content, Integer[] onTimes, Integer[] offTimes) {
            this.id = id;
            this.name = content;
            this.onTimes = onTimes;
            this.offTimes = offTimes;
        }

        @Override
        public String toString() {
            return name;
        }
    }
    /**
     * An array of sample (dummy) items.
     */
    public static List<BrewingProgram> ITEMS = new ArrayList<BrewingProgram>();
    /**
     * A map of sample (dummy) items, by ID.
     */
    public static Map<String, BrewingProgram> ITEM_MAP = new HashMap<String, BrewingProgram>();

    static {
        Integer[] onTimes = {Integer.valueOf(30), Integer.valueOf(50), Integer.valueOf(0), Integer.valueOf(0)};
        Integer[] offTimes = {Integer.valueOf(30), Integer.valueOf(0), Integer.valueOf(0), Integer.valueOf(0)};
        // Add 3 sample items.
        addItem(new BrewingProgram("1", "B Program 1", onTimes, offTimes));
        addItem(new BrewingProgram("2", "B Program 2", onTimes, offTimes));
        addItem(new BrewingProgram("3", "B Program 3", onTimes, offTimes));
    }
    private static void addItem(BrewingProgram item) {
        ITEMS.add(item);
        ITEM_MAP.put(item.id, item);
    }

}
