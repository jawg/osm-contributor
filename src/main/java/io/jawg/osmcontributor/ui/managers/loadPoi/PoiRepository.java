package io.jawg.osmcontributor.ui.managers.loadPoi;

import org.joda.time.LocalDateTime;

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
                //todo add recursivity
                getPois(subscriber, box);

                subscriber.onCompleted();
            }
        });
    }

    private void getPois(Subscriber<? super PoiLoadingProgress> subscriber, Box box) {

        List<Long> ids = PoiRepository.this.computeIdOfBox(box);
        List<MapArea> mapAreas = mapAreaDao.queryForIds(ids);

        if (ids.size() == mapAreas.size()) {
            // some areas are not loaded in ou BD
            // we call the backend to received the data
            //we notify the subscriber that we have some loading to do
            loadMissingMapAreas(ids, mapAreas);

            PoiLoadingProgress loadingProgress = new PoiLoadingProgress();
            loadingProgress.setLoadingStatus(LOADING_FROM_SERVER);
            //add notion of progress
            //handle network errors

            subscriber.onNext(loadingProgress);


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
        return Collections.singletonList(1203L);
    }

    private List<Box> getDiff(List<Long> ids, List<MapArea> mapAreas) {
        return new ArrayList<>();
    }

}
