package io.jawg.osmcontributor.ui.managers.loadPoi;

import java.util.List;

import io.jawg.osmcontributor.model.entities.Note;
import io.jawg.osmcontributor.model.entities.Poi;

public class PoiLoadingProgress {

    //todo create fonction that return new instance by state like getInstanceFinish ...


    private LoadingStatus loadingStatus;
    private boolean dataNeedRefresh;
    private long totalsElements = 0L;
    private long loadedElements = 0L;
    private List<Poi> pois;
    private List<Note> notes;


    public LoadingStatus getLoadingStatus() {
        return loadingStatus;
    }

    public void setLoadingStatus(LoadingStatus loadingStatus) {
        this.loadingStatus = loadingStatus;
    }

    public boolean isDataNeedRefresh() {
        return dataNeedRefresh;
    }

    public void setDataNeedRefresh(boolean dataNeedRefresh) {
        this.dataNeedRefresh = dataNeedRefresh;
    }

    public List<Poi> getPois() {
        return pois;
    }

    public List<Note> getNotes() {
        return notes;
    }


    public void setPois(List<Poi> pois) {
        this.pois = pois;
    }


    public long getTotalsElements() {
        return totalsElements;
    }

    public void setTotalsElements(long totalsElements) {
        this.totalsElements = totalsElements;
    }

    public long getLoadedElements() {
        return loadedElements;
    }

    public void setLoadedElements(long loadedElements) {
        this.loadedElements = loadedElements;
    }

    public void setNotes(List<Note> notes) {
        this.notes = notes;
    }

    public enum LoadingStatus {
        POI_LOADING,
        NOTE_LOADING,
        LOADING_FROM_SERVER,
        FINISH,
        OUT_DATED_DATA,
        NETWORK_ERROR,
    }
}
