package io.jawg.osmcontributor.ui.managers.loadPoi;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.jawg.osmcontributor.BuildConfig;
import io.jawg.osmcontributor.database.dao.MapAreaDao;
import io.jawg.osmcontributor.database.dao.PoiDao;
import io.jawg.osmcontributor.database.dao.PoiNodeRefDao;
import io.jawg.osmcontributor.database.dao.PoiTagDao;
import io.jawg.osmcontributor.database.dao.PoiTypeDao;
import io.jawg.osmcontributor.database.dao.RelationDisplayDao;
import io.jawg.osmcontributor.database.dao.RelationDisplayTagDao;
import io.jawg.osmcontributor.database.dao.RelationIdDao;
import io.jawg.osmcontributor.database.helper.DatabaseHelper;
import io.jawg.osmcontributor.model.entities.MapArea;
import io.jawg.osmcontributor.rest.Backend;
import io.jawg.osmcontributor.rest.mappers.PoiMapper;
import io.jawg.osmcontributor.rest.mappers.RelationDisplayMapper;
import io.jawg.osmcontributor.ui.managers.loadPoi.exception.TooManyPois;
import io.jawg.osmcontributor.ui.managers.loadPoi.executors.CancelableObservable;
import io.jawg.osmcontributor.ui.managers.loadPoi.executors.CancelableSubscriber;
import io.jawg.osmcontributor.utils.Box;
import rx.Observable;
import rx.Subscriber;

import static io.jawg.osmcontributor.ui.managers.loadPoi.PoiLoadingProgress.LoadingStatus.POI_LOADING;


/**
 * {@link PoiRepository} for retrieving poi data.
 */
@Singleton
public class PoiRepository {
    private final PoiDao poiDao;
    private final MapAreaDao mapAreaDao;
    private final Backend backend;
    private final PoiMapper poiMapper;
    private final RelationDisplayMapper relationDisplayMapper;
    private final PoiTypeDao poiTypeDao;
    private final PoiTagDao poiTagDao;
    private final PoiNodeRefDao poiNodeRefDao;
    private final RelationIdDao relationIdDao;
    private final DatabaseHelper databaseHelper;
    private final RelationDisplayDao relationDisplayDao;
    private final RelationDisplayTagDao relationDisplayTagDao;

    /**
     * Construct a {@link PoiRepository}.
     */
    @Inject
    public PoiRepository(PoiDao poiDao, RelationDisplayDao relationDisplayDao, RelationDisplayTagDao relationDisplayTagDao, Backend backend, MapAreaDao mapAreaDao, PoiMapper poiMapper, RelationDisplayMapper relationDisplayMapper, PoiTypeDao poiTypeDao, PoiTagDao poiTagDao,
                         PoiNodeRefDao poiNodeRefDao, RelationIdDao relationIdDao, DatabaseHelper databaseHelper) {
        this.backend = backend;
        this.poiDao = poiDao;
        this.relationDisplayDao = relationDisplayDao;
        this.relationDisplayTagDao = relationDisplayTagDao;
        this.relationDisplayMapper = relationDisplayMapper;
        this.mapAreaDao = mapAreaDao;
        this.poiMapper = poiMapper;
        this.poiTypeDao = poiTypeDao;
        this.poiTagDao = poiTagDao;
        this.poiNodeRefDao = poiNodeRefDao;
        this.relationIdDao = relationIdDao;
        this.databaseHelper = databaseHelper;
    }

    public CancelableObservable<PoiLoadingProgress> getPoiFromArea(final MapArea mapArea, final boolean refreshData) {
        CancelableSubscriber cancelableSubscriber = new CancelableSubscriber<PoiLoadingProgress>() {
            @Override
            public void cancelableCall(Subscriber<? super PoiLoadingProgress> subscriber) {
                PoiLoader poiLoader = new PoiLoader(poiDao, relationDisplayDao, relationDisplayTagDao, backend, mapAreaDao,
                        subscriber, mustBeKilled, poiMapper, relationDisplayMapper, poiTypeDao, poiTagDao, poiNodeRefDao, relationIdDao, databaseHelper);
                poiLoader.init(refreshData, mapArea);
                poiLoader.getPoiFromBox(mapArea);
            }
        };
        return new CancelableObservable<PoiLoadingProgress>(cancelableSubscriber);
    }

    public Observable<PoiLoadingProgress> verifyCount(final Box box) {
        return Observable.create(subscriber -> {
            Long count = poiDao.countForAllInRect(box);
            if (count > BuildConfig.MAX_POIS_ON_MAP) {
                TooManyPois tooManyPois = new TooManyPois();
                tooManyPois.setCount(count);
                throw tooManyPois;
            }
            subscriber.onCompleted();
        });

    }

    public Observable<PoiLoadingProgress> getPoisFromDB(final Box box) {
        return Observable.create(subscriber -> {
            PoiLoadingProgress poiLoadingProgress = new PoiLoadingProgress(POI_LOADING);
            poiLoadingProgress.setPois(poiDao.queryForAllInRect(box));
            subscriber.onNext(poiLoadingProgress);
            subscriber.onCompleted();
        });

    }
}
