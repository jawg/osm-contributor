package io.jawg.osmcontributor.ui.managers.loadPoi;

import java.util.List;

import io.jawg.osmcontributor.model.entities.Poi;

public class PoiLoadingProgress {

    private LoadingStatus loadingStatus;
    private boolean dataNeedRefresh;
    private List<Poi> pois;


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

    public void setPois(List<Poi> pois) {
        this.pois = pois;
    }

    public enum LoadingStatus {
        LOADING_FROM_DB,
        LOADING_FROM_SERVER,
        FINISH,
        OUT_DATED_DATA,
        NETWORK_ERROR,
    }
}
