package io.jawg.osmcontributor.ui.adapters.item.shelter;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import io.jawg.osmcontributor.model.entities.Poi;
import io.jawg.osmcontributor.model.entities.PoiTag;
import io.jawg.osmcontributor.model.entities.PoiTypeTag;
import io.jawg.osmcontributor.ui.adapters.parser.ParserManager;

import static io.jawg.osmcontributor.ui.adapters.TagsAutocompleteUtils.getPossibleValuesAsList;
import static io.jawg.osmcontributor.ui.adapters.TagsAutocompleteUtils.removeDuplicate;
import static io.jawg.osmcontributor.ui.adapters.item.shelter.TagItem.Type.CONSTANT;
import static io.jawg.osmcontributor.ui.adapters.item.shelter.TagItem.Type.SHELTER;
import static io.jawg.osmcontributor.ui.adapters.item.shelter.TagItem.Type.TEXT;

public class TagMapper {
    private Poi poi;
    private Map<String, List<String>> tagValueSuggestionsMap;
    private boolean expertMode;
    private int nbMandatory = 0;
    private Collection<PoiTypeTag> tagToMap;
    private List<TagItem> mappedTags;
    private List<TagItem> constantTags;

    private TagMapper(Poi poi, Map<String, List<String>> tagValueSuggestionsMap, boolean expertMode) {
        this.poi = poi;
        this.tagValueSuggestionsMap = tagValueSuggestionsMap;
        this.expertMode = expertMode;
        tagToMap = poi.getType().getTags();
        mappedTags = new ArrayList<>();
        constantTags = new ArrayList<>();
    }

    public static List<TagItem> getTagItems(Poi poi, Map<String, List<String>> tagValueSuggestionsMap, boolean expertMode) {
        TagMapper tagMapper = new TagMapper(poi, tagValueSuggestionsMap, expertMode);
        tagMapper.mapAllTags();
        tagMapper.mappedTags.addAll(tagMapper.constantTags);
        return tagMapper.mappedTags;
    }

    private void mapAllTags() {
        //map all item that are a combination of several tags
        mapGroupTags();
        //map a tag into a item to display
        mapSingleTagsFromType();
        if (expertMode) {
            //map all tags that are present in the Poi but not in the poiType
            mapPoiTags();
        }
    }

    /**
     * Go throw the PoiType and add all widgets with their autocomplete value, already set value...
     */
    private void mapSingleTagsFromType() {
        for (PoiTypeTag poiTypeTag : tagToMap) {
            addTag(poiTypeTag, poiTypeTag.getValue() == null);
        }
    }


    /**
     * Map all remaining tags of the poi not referenced in the PoiType.
     */
    private void mapPoiTags() {
        for (PoiTag poiTag : poi.getTags()) {
            String key = poiTag.getKey();
            String value = poiTag.getValue();
            Map<String, String> values = removeDuplicate(tagValueSuggestionsMap.get(key),
                    Collections.singletonList(value));
            TagItem tagItem = new SingleTagItem.SingleTagItemBuilder(key, value)
                    // Display tags as mandatory if they are mandatory and we are not in expert mode
                    .mandatory(false)
                    .values(values)
                    .type(TEXT)
                    .isConform(true)
                    // Display the tags of the poi that are not in the PoiType
                    .show(true)
                    .build();
            if (!mappedTags.contains(tagItem) && !constantTags.contains(tagItem)) {
                mappedTags.add(getPosition(false), tagItem);
            }
        }
    }


    /**
     * Add tag into global list of tag
     *
     * @param poiTypeTag poiTypeTag value
     */
    private void addTag(PoiTypeTag poiTypeTag, boolean updatable) {
        String key = poiTypeTag.getKey();
        Map<String, String> values = removeDuplicate(getPossibleValuesAsList(poiTypeTag.getPossibleValues()),
                tagValueSuggestionsMap.get(key));
        // Parse value if needed
        String value = poi.getTagsMap().get(key);
        TagItem.Type type = mapType(key, updatable, poiTypeTag.getTagType());

        boolean supportValue = ParserManager.supportValue(value, type);

        boolean display = (poiTypeTag.getShow() != null ? poiTypeTag.getShow() : true) && type != CONSTANT;
        TagItem tagItem = new SingleTagItem.SingleTagItemBuilder(key, value)
                // Display tags as mandatory if they are mandatory and we are not in expert mode
                .mandatory(!expertMode && poiTypeTag.getMandatory())
                .values(values)
                .type(type)
                .isConform(supportValue || type == TagItem.Type.NUMBER)
                // Display the tags of the poi that are not in the PoiType
                .show(expertMode || display)
                .build();

        // Add into the list
        if (!mappedTags.contains(tagItem) && !constantTags.contains(tagItem)) {
            if (display) {
                int position;
                if (tagItem.getKey().equals("name")) {
                    position = 0;
                    nbMandatory++;
                } else {
                    position = getPosition(tagItem.mandatory);
                }
                mappedTags.add(position, tagItem);
            } else {
                constantTags.add(constantTags.size(), tagItem);
            }
        }
    }

    @NonNull
    private TagItem.Type mapType(String key, boolean updatable, TagItem.Type type) {
        if (type == null) {
            type = TEXT;
        }
        type = key.equals("collection_times") ? TagItem.Type.TIME : type;
        type = key.equals("opening_hours") || key.contains("hours") ? TagItem.Type.OPENING_HOURS : type;

        return updatable ? type : TagItem.Type.CONSTANT;
    }


    private void mapGroupTags() {
        //todo generify if you have other group to map
        if (poi.getType().getTechnicalName().equals("highway=bus_stop")) {
            Collection<PoiTypeTag> unusedTags = new ArrayList<>();

            TagItem.TagItemBuilder tagItemBuilder = new ShelterTagItem.ShelterTagItemBuilder("shelter", "none")
                    .shelterType(ShelterType.getTypeFromValues(poi.getTags()))
                    .mandatory(true)
                    .type(SHELTER)
                    .isConform(true)
                    .show(true);

            //remove all tags from the type so they aren't display
            for (PoiTypeTag tag : tagToMap) {
                if (!ShelterType.handleTag(tag.getKey())) {
                    unusedTags.add(tag);
                }
            }

            tagToMap = unusedTags;
            mappedTags.add(0, tagItemBuilder.build());
            nbMandatory++;
        }
    }

    private int getPosition(boolean mandatory) {
        if (mandatory) {
            return ++nbMandatory;
        }
        return mappedTags.size();
    }
}
