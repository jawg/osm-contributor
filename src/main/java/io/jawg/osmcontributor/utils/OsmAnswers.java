package io.jawg.osmcontributor.utils;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;

/**
 * Created by hugo on 02/02/2017.
 */
public class OsmAnswers {

    public static void localPoiAction(String poiType, String action) {
        String event;
        switch (action) {
            case "add":
                event = "POIs créés localement";
                break;
            case "update":
                event = "POIs mis à jour localement";
                break;
            case "cancel":
                event = "Modifications abandonnées";
                break;
            case "delete":
                event = "POIs supprimés localement";
                break;
            default:
                event = "POIs mis à jour localement";
                break;
        }

        Answers.getInstance()
                // Create an event for creation of POI.
                .logCustom(new CustomEvent(event)
                        .putCustomAttribute("Type", poiType));
    }

    public static void remotePoiAction(String poiType, String action) {
        String event;
        switch (action) {
            case "add":
                event = "POIs créés et synchronisés";
                break;
            case "update":
                event = "POIs mis à jour et synchronisés";
                break;
            case "delete":
                event = "POIs supprimés et synchronisés";
                break;
            default:
                event = "POIs mis à jour et synchronisés";
                break;
        }

        Answers.getInstance()
                // Create an event for creation of POI.
                .logCustom(new CustomEvent(event)
                        .putCustomAttribute("Type", poiType));
    }

    public static void remoteRelationAction() {
        Answers.getInstance()
                // Create an event for modification of bus line
                .logCustom(new CustomEvent("Relation (bus line) updated"));
    }

    public static void visitedActivity(String activity) {
        Answers.getInstance()
                // Create an event for creation of POI.
                .logCustom(new CustomEvent("Pages visitées")
                        .putCustomAttribute("Page", activity));
    }


}
