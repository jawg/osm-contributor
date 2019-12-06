package io.jawg.osmcontributor.ui.adapters.item.shelter;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import io.jawg.osmcontributor.model.entities.PoiTag;

public enum ShelterType {
    POLE("", "platform", "no"), SHELTER("", "platform", "yes"), NONE("unofficial", "", ""), UNDEFINED("", "platform", "");

    String officialStatus;
    String publicTransport;
    String shelter;

//     "Informal / Aucune indication sur le terrain (informel)" would add the tag official_status=unofficial
//     "Pole / Poteau indicateur" would add public_transport=platform and shelter=no
//     "ShelterType / Abris-bus" would add public_transport=platform and shelter=yes

    ShelterType(String officialStatus, String publicTransport, String shelter) {
        this.officialStatus = officialStatus;
        this.publicTransport = publicTransport;
        this.shelter = shelter;
    }

    public Map<String, String> getOsmValues() {
        HashMap<String, String> map = new HashMap<>();
        map.put("official_status", officialStatus);
        map.put("public_transport", publicTransport);
        map.put("shelter", shelter);
        return map;
    }

    public static ShelterType getTypeFromValues(Collection<PoiTag> tags) {
        Boolean unofficial = null;
        Boolean shelter = null;

        for (PoiTag tag : tags) {
            String value = tag.getValue();
            if (value == null) {
                value = "";
            }
            if (tag.getKey().equals("official_status")) {
                unofficial = value.compareToIgnoreCase("unofficial") == 0;
            } else if (tag.getKey().equals("shelter")) {
                shelter = value.compareToIgnoreCase("yes") == 0;
            }
        }


        if (unofficial != null && unofficial) {
            return NONE;
        } else if (shelter != null) {
            return shelter ? SHELTER : POLE;
        }
        return UNDEFINED;
    }

    public static boolean handleTag(String key) {
        return key.equals("official_status") || key.equals("public_transport") || key.equals("shelter");
    }
}