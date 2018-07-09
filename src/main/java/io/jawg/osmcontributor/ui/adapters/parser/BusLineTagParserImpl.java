package io.jawg.osmcontributor.ui.adapters.parser;


import java.util.List;

import io.jawg.osmcontributor.ui.adapters.item.shelter.TagItem;


public class BusLineTagParserImpl implements TagParser {

    @Override
    public TagItem.Type getType() {
        return TagItem.Type.BUS_LINE;
    }

    @Override
    public boolean isCandidate(String key, List<String> values) {
        return key.contains("route_ref");
    }

    @Override
    public boolean supports(String value) {
        return true;
    }

    @Override
    public int getPriority() {
        return ParserManager.PRIORITY_NORMAL;
    }
}
