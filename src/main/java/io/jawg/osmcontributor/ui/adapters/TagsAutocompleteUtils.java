package io.jawg.osmcontributor.ui.adapters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.jawg.osmcontributor.rest.mappers.PoiTypeMapper;

public class TagsAutocompleteUtils {

    private TagsAutocompleteUtils() {
        // utils class
    }

    /**
     * Get a list of all possible values without duplicate
     *
     * @param possibleValues     possible values from h2geo
     * @param autoCompleteValues proposition from last used values
     * @return a merged list
     */
    public static Map<String, String> removeDuplicate(List<String> possibleValues, List<String> autoCompleteValues) {
        // Create a default empty map to avoid null pointer exception
        Map<String, String> values = new HashMap<>();

        if (possibleValues != null) {
            // If possible values are not null, init values with it
            for (String possibleValue : possibleValues) {
                // if values are type of 'XXX;;XXX' split'em
                if (possibleValue.contains(PoiTypeMapper.VALUE_SEPARATOR)) {
                    String[] split = possibleValue.split(PoiTypeMapper.VALUE_SEPARATOR);
                    values.put(split[0], split[1]);
                } else {
                    values.put(possibleValue, possibleValue);
                }
            }
        }

        if (autoCompleteValues != null) {
            // If auto complete values are not null and values are empty, fill it with it
            if (values.isEmpty()) {
                for (String autoCompleteValue : autoCompleteValues) {
                    values.put(autoCompleteValue, autoCompleteValue);
                }
            }

            // For each auto complete values, if the value does not exist, add it to the new map
            for (String possibleValue : autoCompleteValues) {
                if (!values.containsKey(possibleValue) && (!possibleValue.isEmpty() || !possibleValue.trim().isEmpty())) {
                    values.put(possibleValue, possibleValue);
                }
            }
        }

        // Sometimes, with yes value there is no value, this is a non sens
        if (values.containsKey("yes") && !values.containsKey("no")) {
            values.put("no", "No");
        } else if (values.containsKey("no") && !values.containsKey("yes")) {
            values.put("yes", "Yes");
        }
        return values;
    }

    /**
     * Convert possible values from String to List.
     *
     * @param possibleValuesAsString string of possible values
     * @return list of possible values
     */
    public static List<String> getPossibleValuesAsList(String possibleValuesAsString) {
        if (possibleValuesAsString == null || possibleValuesAsString.isEmpty()) {
            return null;
        }
        // Split into an array of value:label
        String[] valuesAndLabels = possibleValuesAsString.split(PoiTypeMapper.ITEM_SEPARATOR);
        return new ArrayList<>(Arrays.asList(valuesAndLabels));
    }
}
