package io.jawg.osmcontributor.ui.managers.loadPoi;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.jawg.osmcontributor.database.dao.MapAreaDao;
import io.jawg.osmcontributor.database.dao.PoiDao;
import io.jawg.osmcontributor.database.dao.PoiTypeDao;
import io.jawg.osmcontributor.rest.Backend;
import io.jawg.osmcontributor.rest.mappers.PoiMapper;
import io.jawg.osmcontributor.ui.managers.loadPoi.executors.CancelableObservable;
import io.jawg.osmcontributor.ui.managers.loadPoi.executors.CancelableSubscriber;
import io.jawg.osmcontributor.utils.Box;
import rx.Subscriber;


/**
 * {@link PoiRepository} for retrieving poi data.
 */
@Singleton
public class PoiRepository {
    private final PoiDao poiDao;
    private final MapAreaDao mapAreaDao;
    private final Backend backend;
    private final PoiMapper poiMapper;
    private final PoiTypeDao poiTypeDao;

    /**
     * Construct a {@link PoiRepository}.
     */
    @Inject
    public PoiRepository(PoiDao poiDao, Backend backend, MapAreaDao mapAreaDao, PoiMapper poiMapper, PoiTypeDao poiTypeDao) {
        this.backend = backend;
        this.poiDao = poiDao;
        this.mapAreaDao = mapAreaDao;
        this.poiMapper = poiMapper;
        this.poiTypeDao = poiTypeDao;
    }


    public CancelableObservable<PoiLoadingProgress> getPoiFromBox(final Box box, final boolean refreshData) {
        CancelableSubscriber cancelableSubscriber = new CancelableSubscriber<PoiLoadingProgress>() {
            @Override
            public void cancelableCall(Subscriber<? super PoiLoadingProgress> subscriber) {
                PoiLoader poiLoader = new PoiLoader(poiDao, backend, mapAreaDao, subscriber, mustBeKilled, poiMapper, poiTypeDao);
                poiLoader.init(box, refreshData);
                poiLoader.getPoiFromBox();
            }
        };
        return new CancelableObservable<PoiLoadingProgress>(cancelableSubscriber);
    }
}
