package io.jawg.osmcontributor.ui.utils;

import android.content.Context;

import java.util.HashMap;
import java.util.Map;

import io.jawg.osmcontributor.R;

public class Translator {
    private static Map<String, Integer> translateTable;

    static {
        translateTable = new HashMap<>();
        translateTable.put("name", R.string.tag_poi_name);
        translateTable.put("shelter", R.string.tag_poi_shelter);
        translateTable.put("wheelchair", R.string.tag_poi_wheelchair);
        translateTable.put("bench", R.string.tag_poi_bench);
        translateTable.put("route_ref", R.string.tag_poi_rout_ref);
        translateTable.put("note", R.string.bus_shelter_note);
    }


    public static String getTranslation(String s, Context context) {
        if (translateTable.containsKey(s)) {
            return context.getString(translateTable.get(s));
        }
        return null;
    }
}
