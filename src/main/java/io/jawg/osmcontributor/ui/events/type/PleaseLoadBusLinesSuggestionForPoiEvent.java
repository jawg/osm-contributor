package io.jawg.osmcontributor.ui.events.type;

public class PleaseLoadBusLinesSuggestionForPoiEvent {

    private String search;

    public PleaseLoadBusLinesSuggestionForPoiEvent(String search) {
        this.search = search;
    }

    public String getSearch() {
        return search;
    }

    public void setSearch(String search) {
        this.search = search;
    }
}
