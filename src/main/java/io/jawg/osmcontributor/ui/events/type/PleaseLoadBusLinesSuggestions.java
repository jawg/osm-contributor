package io.jawg.osmcontributor.ui.events.type;

public class PleaseLoadBusLinesSuggestions {

    private String search;

    public PleaseLoadBusLinesSuggestions(String search) {
        this.search = search;
    }

    public String getSearch() {
        return search;
    }

    public void setSearch(String search) {
        this.search = search;
    }
}
