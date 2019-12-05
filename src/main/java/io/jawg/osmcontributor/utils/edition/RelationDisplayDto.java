package io.jawg.osmcontributor.utils.edition;

import java.util.Collection;
import java.util.HashMap;

import io.jawg.osmcontributor.model.entities.relation_display.RelationDisplay;
import io.jawg.osmcontributor.model.entities.relation_display.RelationDisplayTag;

public class RelationDisplayDto {

    private Long id;
    private String backendId;
    private HashMap<String, String> tags;

    public RelationDisplayDto(RelationDisplay relationDisplay) {

        this.id = relationDisplay.getId();
        this.backendId = relationDisplay.getBackendId();
        this.tags = tagsMapper(relationDisplay.getTags());
    }

    private HashMap<String, String> tagsMapper(Collection<RelationDisplayTag> tagsList) {
        HashMap<String, String> tagsMap = new HashMap<>(tagsList.size());
        for (RelationDisplayTag tag : tagsList) {
            tagsMap.put(tag.getKey(), tag.getValue());
        }
        return tagsMap;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBackendId() {
        return this.backendId;
    }

    public void setBackendId(String backendId) {
        this.backendId = backendId;
    }

    public HashMap<String, String> getTags() {
        return tags;
    }

    public void setTags(HashMap<String, String> tags) {
        this.tags = tags;
    }

    public String getTagValue(String key) {
        return tags.get(key);
    }

    public void setTag(String key, String value) {
        this.tags.put(key, value);
    }
}
