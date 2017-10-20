package io.jawg.osmcontributor.ui.managers.loadPoi;

import org.joda.time.LocalDateTime;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.jawg.osmcontributor.database.dao.MapAreaDao;
import io.jawg.osmcontributor.database.dao.PoiDao;
import io.jawg.osmcontributor.model.entities.MapArea;
import io.jawg.osmcontributor.model.entities.Poi;
import io.jawg.osmcontributor.rest.Backend;
import io.jawg.osmcontributor.utils.Box;
import rx.Observable;
import rx.Subscriber;

import static io.jawg.osmcontributor.ui.managers.loadPoi.PoiLoadingProgress.LoadingStatus.FINISH;
import static io.jawg.osmcontributor.ui.managers.loadPoi.PoiLoadingProgress.LoadingStatus.LOADING_FROM_SERVER;
import static io.jawg.osmcontributor.ui.managers.loadPoi.PoiLoadingProgress.LoadingStatus.OUT_DATED_DATA;


/**
 * {@link PoiRepository} for retrieving poi data.
 */
@Singleton
public class PoiRepository {

    public static final BigDecimal GRANULARITY_LAT = new BigDecimal(1000);
    public static final BigDecimal GRANULARITY_LNG = new BigDecimal(1000);
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
                getPois(subscriber, box);
                subscriber.onCompleted();
            }
        });
    }

    private void getPois(Subscriber<? super PoiLoadingProgress> subscriber, Box box) {

        List<Long> ids = PoiRepository.this.computeIdOfBox(box);
        List<MapArea> mapAreas = mapAreaDao.queryForIds(ids);

        if (ids.size() != mapAreas.size()) {
            // some areas are not loaded in ou BD
            // we call the backend to received the data
            //we notify the subscriber that we have some loading to do
            loadMissingMapAreas(ids, mapAreas);

            PoiLoadingProgress loadingProgress = new PoiLoadingProgress();
            loadingProgress.setLoadingStatus(LOADING_FROM_SERVER);
            //add notion of progress
            //handle network errors

            subscriber.onNext(loadingProgress);

            getPois(subscriber, box);
        } else {
            //all the data is present in the BDD

            //todo paginate the querry by id or something and send by successive onNext
            // load in first the poi in the center of the box
            List<Poi> pois = poiDao.queryForAllInRect(box);
            PoiLoadingProgress loadingProgress = new PoiLoadingProgress();
            loadingProgress.setLoadingStatus(FINISH);
            loadingProgress.setPois(pois);

            subscriber.onNext(loadingProgress);
        }

        for (MapArea mapArea : mapAreas) {
            LocalDateTime lastUpate = new LocalDateTime(mapArea.getUpdatedDate());
            //todo check if the data is outDated and ask for Update
            boolean outDatedData = LocalDateTime.now().isAfter(lastUpate.plusMonths(1));
            if (outDatedData) {
                PoiLoadingProgress loadingProgress = new PoiLoadingProgress();
                loadingProgress.setLoadingStatus(OUT_DATED_DATA);

                //Todo progress as enum or cast
                subscriber.onNext(loadingProgress);
            }
        }
    }

    private void loadMissingMapAreas(List<Long> ids, List<MapArea> mapAreas) {
        List<Box> boxToLoad = getDiff(ids, mapAreas);
        for (Box b : boxToLoad) {
            backend.getPoisInBox(b);
            mapAreas.add(new MapArea());
        }
    }

    private List<Long> computeIdOfBox(Box box) {
        //a coup de modulo on calcul l'id

        BigDecimal north = new BigDecimal(box.getNorth());
        BigDecimal weast = new BigDecimal(box.getWest());
        BigDecimal east = new BigDecimal(box.getEast());
        BigDecimal south = new BigDecimal(box.getSouth());

        long northID = north.multiply(GRANULARITY_LAT).setScale(0, RoundingMode.HALF_UP).longValue();
        long westID = weast.multiply(GRANULARITY_LNG).setScale(0, RoundingMode.HALF_DOWN).longValue();

        long southID = south.multiply(GRANULARITY_LNG).setScale(0, RoundingMode.HALF_DOWN).longValue();
        long eastID = east.multiply(GRANULARITY_LNG).setScale(0, RoundingMode.HALF_UP).longValue();

        Long upLeftCorner = Long.valueOf(String.valueOf(northID) + String.valueOf(westID));
        Long downRightCorner = Long.valueOf(String.valueOf(southID) + String.valueOf(eastID));

        //we get the long up the area is rounded up for north and down for south

        if (upLeftCorner.equals(downRightCorner)) {
            return Collections.singletonList(downRightCorner);
        } else {
            List<Long> ids = new ArrayList<>();
            for (int latInc = 0; latInc < northID - southID; latInc++) {
                for (int lngInc = 0; lngInc < westID - eastID; lngInc++) {
                    long idLat = southID + latInc;
                    long idLng = westID + lngInc;
                    ids.add(Long.valueOf(String.valueOf(idLat) + String.valueOf(idLng)));
                }
            }
            // loop to find all ids
        }


        return Collections.singletonList(1203L);
    }

    private List<Box> getDiff(List<Long> ids, List<MapArea> mapAreas) {
        return new ArrayList<>();
    }

}
