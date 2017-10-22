package io.jawg.osmcontributor.ui.managers.loadPoi;

import org.joda.time.LocalDateTime;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.jawg.osmcontributor.database.dao.MapAreaDao;
import io.jawg.osmcontributor.database.dao.PoiDao;
import io.jawg.osmcontributor.model.entities.MapArea;
import io.jawg.osmcontributor.model.entities.Poi;
import io.jawg.osmcontributor.rest.Backend;
import io.jawg.osmcontributor.rest.NetworkException;
import io.jawg.osmcontributor.utils.Box;
import rx.Observable;
import rx.Subscriber;

import static io.jawg.osmcontributor.ui.managers.loadPoi.PoiLoadingProgress.LoadingStatus.FINISH;
import static io.jawg.osmcontributor.ui.managers.loadPoi.PoiLoadingProgress.LoadingStatus.POI_LOADING;
import static io.jawg.osmcontributor.ui.managers.loadPoi.PoiLoadingProgress.LoadingStatus.LOADING_FROM_SERVER;
import static io.jawg.osmcontributor.ui.managers.loadPoi.PoiLoadingProgress.LoadingStatus.OUT_DATED_DATA;


/**
 * {@link PoiRepository} for retrieving poi data.
 */
@Singleton
public class PoiRepository {

    public static final BigDecimal GRANULARITY_LAT = new BigDecimal(1000);
    public static final BigDecimal GRANULARITY_LNG = new BigDecimal(1000);
    public static final int POI_PAGE = 20;
    public static final int SAVE_POI_PAGE = 20;
    private final PoiDao poiDao;
    private final MapAreaDao mapAreaDao;
    private final Backend backend;

    /**
     * Construct a {@link PoiRepository}.
     */
    @Inject
    public PoiRepository(PoiDao poiDao, Backend backend, MapAreaDao mapAreaDao) {
        this.backend = backend;
        this.poiDao = poiDao;
        this.mapAreaDao = mapAreaDao;
    }


    public Observable<PoiLoadingProgress> getPoiFromBox(final Box box) {
        return Observable.create(new Observable.OnSubscribe<PoiLoadingProgress>() {
            @Override
            public void call(Subscriber<? super PoiLoadingProgress> subscriber) {
                List<MapArea> areasNeeded = computeMapAreaOfBox(box);

                getPois(subscriber, box, areasNeeded);
                subscriber.onCompleted();
            }
        });
    }

    private void getPois(Subscriber<? super PoiLoadingProgress> subscriber, Box box, List<MapArea> areasNeeded) {

        List<MapArea> localAreas = mapAreaDao.queryForIds(getIds(areasNeeded));

        if (areasNeeded.size() != localAreas.size()) {
            // some areas are not loaded in ou BD
            // we call the backend to received the data
            //we notify the subscriber that we have some loading to do

            PoiLoadingProgress loadingProgress = new PoiLoadingProgress();
            loadingProgress.setLoadingStatus(LOADING_FROM_SERVER);
            // todo add notion of progress
            //handle network errors
            subscriber.onNext(loadingProgress);

            loadMissingMapAreas(areasNeeded, localAreas, subscriber);

            getPois(subscriber, box, areasNeeded);
        } else {
            //all the data is present in the BDD
            // load in first the poi in the center of the box
            boolean allLoaded = false;
            int page = 0;
            do {
                List<Poi> pois = poiDao.queryForAllInRect(box, page * POI_PAGE, POI_PAGE);
                allLoaded = pois.size() < POI_PAGE;
                PoiLoadingProgress loadingProgress = new PoiLoadingProgress();
                loadingProgress.setLoadingStatus(allLoaded ? FINISH : POI_LOADING);
                loadingProgress.setPois(pois);
                subscriber.onNext(loadingProgress);
            } while (!allLoaded);

        }

        for (MapArea mapArea : localAreas) {
            LocalDateTime lastUpate = new LocalDateTime(mapArea.getUpdatedDate());
            //we check if the data is outDated and ask for Update
            boolean outDatedData = LocalDateTime.now().isAfter(lastUpate.plusMonths(1));
            if (outDatedData) {
                PoiLoadingProgress loadingProgress = new PoiLoadingProgress();
                loadingProgress.setLoadingStatus(OUT_DATED_DATA);
                subscriber.onNext(loadingProgress);
            }
        }
    }

    private List<Long> getIds(List<MapArea> areasNeeded) {
        List<Long> ids = new ArrayList<>();
        for (MapArea mapArea : areasNeeded) {
            ids.add(mapArea.getId());
        }
        return ids;
    }

    private void loadMissingMapAreas(List<MapArea> toLoadAreas, List<MapArea> loadedAreas, Subscriber<? super PoiLoadingProgress> subscriber) {
        for (MapArea toLoadArea : toLoadAreas) {
            if (!loadedAreas.contains(toLoadArea)) {
                try {
                    List<Poi> poisInBox = backend.getPoisInBox(toLoadArea.getBox());
                    int i = 0;
                    for (Poi poi : poisInBox) {
                        poiDao.createOrUpdate(poi);
                        i++;

                        if (i > SAVE_POI_PAGE) {
                            i = 0;
                            //todo publish result
                        }
                    }
                    //publish poi loaded
                    toLoadArea.setUpdatedDate(new Date(System.currentTimeMillis()));
                    loadedAreas.add(toLoadArea);
                } catch (NetworkException e) {
                    subscriber.onNext(new PoiLoadingProgress());
                }

            }
        }
    }

    private List<MapArea> computeMapAreaOfBox(Box box) {
        List<MapArea> areas = new ArrayList<>();
        //a coup de modulo on calcul l'id

        BigDecimal north = new BigDecimal(box.getNorth());
        BigDecimal west = new BigDecimal(box.getWest());
        BigDecimal east = new BigDecimal(box.getEast());
        BigDecimal south = new BigDecimal(box.getSouth());

        long northArea = north.multiply(GRANULARITY_LAT).setScale(0, RoundingMode.HALF_UP).longValue();
        long westArea = west.multiply(GRANULARITY_LNG).setScale(0, RoundingMode.HALF_DOWN).longValue();
        long southArea = south.multiply(GRANULARITY_LNG).setScale(0, RoundingMode.HALF_DOWN).longValue();
        long eastArea = east.multiply(GRANULARITY_LNG).setScale(0, RoundingMode.HALF_UP).longValue();

        //we get the long up the area is rounded up for north and down for south
        for (int latInc = 0; latInc <= northArea - southArea; latInc++) {
            for (int lngInc = 0; lngInc <= westArea - eastArea; lngInc++) {
                long idLat = southArea + latInc;
                long idLng = westArea + lngInc;
                Long id = Long.valueOf(String.valueOf(idLat) + String.valueOf(idLng));

                areas.add(new MapArea(id,
                        northArea / GRANULARITY_LAT.doubleValue(),
                        westArea / GRANULARITY_LAT.doubleValue(),
                        southArea / GRANULARITY_LAT.doubleValue(),
                        eastArea / GRANULARITY_LAT.doubleValue()));
            }
        }

        return areas;
    }
}
