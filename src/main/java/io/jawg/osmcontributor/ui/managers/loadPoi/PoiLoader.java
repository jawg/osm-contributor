package io.jawg.osmcontributor.ui.managers.loadPoi;

import com.j256.ormlite.misc.TransactionManager;

import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;

import io.jawg.osmcontributor.database.dao.MapAreaDao;
import io.jawg.osmcontributor.database.dao.PoiDao;
import io.jawg.osmcontributor.database.dao.PoiNodeRefDao;
import io.jawg.osmcontributor.database.dao.PoiTagDao;
import io.jawg.osmcontributor.database.dao.PoiTypeDao;
import io.jawg.osmcontributor.database.helper.DatabaseHelper;
import io.jawg.osmcontributor.model.entities.MapArea;
import io.jawg.osmcontributor.model.entities.Note;
import io.jawg.osmcontributor.model.entities.Poi;
import io.jawg.osmcontributor.model.entities.PoiNodeRef;
import io.jawg.osmcontributor.model.entities.PoiTag;
import io.jawg.osmcontributor.model.entities.PoiType;
import io.jawg.osmcontributor.rest.Backend;
import io.jawg.osmcontributor.rest.dtos.osm.NodeDto;
import io.jawg.osmcontributor.rest.dtos.osm.OsmDto;
import io.jawg.osmcontributor.rest.dtos.osm.PoiDto;
import io.jawg.osmcontributor.rest.dtos.osm.WayDto;
import io.jawg.osmcontributor.rest.mappers.PoiMapper;
import io.jawg.osmcontributor.ui.utils.BooleanHolder;
import rx.Subscriber;
import timber.log.Timber;

import static io.jawg.osmcontributor.ui.managers.loadPoi.PoiLoadingProgress.LoadingStatus.FINISH;
import static io.jawg.osmcontributor.ui.managers.loadPoi.PoiLoadingProgress.LoadingStatus.LOADING_FROM_SERVER;
import static io.jawg.osmcontributor.ui.managers.loadPoi.PoiLoadingProgress.LoadingStatus.MAPPING_POIS;
import static io.jawg.osmcontributor.ui.managers.loadPoi.PoiLoadingProgress.LoadingStatus.OUT_DATED_DATA;


/**
 * {@link PoiLoader} for retrieving poi data.
 */
public class PoiLoader {

    public static final int POI_PAGE_MAPPING = 5;
    private final PoiDao poiDao;
    private final MapAreaDao mapAreaDao;
    private final Backend backend;
    private Subscriber<? super PoiLoadingProgress> subscriber;
    private boolean refreshData;
    private PoiMapper poiMapper;
    private BooleanHolder mustBeKilled;
    List<PoiType> availableTypes;
    PoiTypeDao poiTypeDao;
    List<OsmDto> osmDtos;
    List<PoiDto> nodeDtos = new ArrayList<>();

    //progress
    private PoiLoadingProgress.LoadingStatus loadingStatus;
    private boolean dataNeedRefresh;
    private long totalsElements = 0L;
    private long loadedElements = 0L;
    private PoiTagDao poiTagDao;
    private PoiNodeRefDao poiNodeRefDao;
    private LocalDateTime lastUpate;
    private List<Poi> pois = new ArrayList<>();
    private List<Note> notes = new ArrayList<>();
    private DatabaseHelper databaseHelper;
    private MapArea mapArea;


    public PoiLoader(PoiDao poiDao, Backend backend, MapAreaDao mapAreaDao, Subscriber<? super PoiLoadingProgress> subscriber,
                     BooleanHolder mustBeKilled, PoiMapper poiMapper, PoiTypeDao poiTypeDao, PoiTagDao poiTagDao, PoiNodeRefDao poiNodeRefDao,
                     DatabaseHelper databaseHelper) {
        this.backend = backend;
        this.poiDao = poiDao;
        this.mapAreaDao = mapAreaDao;
        this.subscriber = subscriber;
        this.mustBeKilled = mustBeKilled;
        this.poiMapper = poiMapper;
        this.poiTypeDao = poiTypeDao;
        this.poiTagDao = poiTagDao;
        this.poiNodeRefDao = poiNodeRefDao;
        this.databaseHelper = databaseHelper;
    }

    public void init(final boolean refreshData, MapArea mapArea) {
        this.refreshData = refreshData;
        this.mapArea = mapArea;
        availableTypes = poiTypeDao.queryForAll();

    }

    public void getPoiFromBox(MapArea mapArea) {
        getPois(mapArea);
        loadingStatus = FINISH;
        subscriber.onCompleted();
    }

    private void getPois(MapArea areasNeeded) {
        handleAreas(areasNeeded);
    }

    private boolean handleAreas(MapArea areasNeeded) {
        boolean newAreasLoaded = false;
        boolean outDatedData = false;

        MapArea localArea = mapAreaDao.queryForId(areasNeeded.getId());

        if (localArea != null) {
            LocalDateTime lastUpate;
            lastUpate = new LocalDateTime(mapArea.getUpdateDate());
            //we check if the data is outDated and ask for Update
            if (LocalDateTime.now().isAfter(lastUpate.plusWeeks(1))) {
                outDatedData = true;
                this.lastUpate = lastUpate;
            }
        }


        if (refreshData || localArea == null) {
            // some areas are not loaded in ou BD
            // we call the backend to received the data
            //we notify the subscriber that we have some loading to do
            loadMissingMapAreas(areasNeeded);
            newAreasLoaded = true;
        }

        if (!refreshData) {
            if (outDatedData) {
                loadingStatus = OUT_DATED_DATA;
                dataNeedRefresh = true;
                publishProgress();
            }
        }

        return newAreasLoaded;
    }


    private void loadMissingMapAreas(final MapArea toLoadArea) {
        Timber.d("----- Downloading Area : " + toLoadArea.getId());
        loadedElements = 0L;
        totalsElements = 0L;
        loadAndSavePoisFromBackend(toLoadArea, refreshData);

        //publish poi loaded
        toLoadArea.setUpdateDate(new DateTime(System.currentTimeMillis()));

        Timber.d("----- Saving Area in BDD : " + toLoadArea.getId());
        mapAreaDao.createOrUpdate(toLoadArea);

    }

    private void loadAndSavePoisFromBackend(MapArea toLoadArea, boolean clean) {
        loadingStatus = LOADING_FROM_SERVER;
        publishProgress();
        loadedElements = 0L;
        nodeDtos.clear();
        osmDtos = backend.getPoisDtosInBox(toLoadArea.getBox());

        for (OsmDto osmDto : osmDtos) {
            killIfNeeded();
            if (osmDto != null) {
                List<NodeDto> nodeDtoList = osmDto.getNodeDtoList();
                if (nodeDtoList != null) {
                    nodeDtos.addAll(nodeDtoList);
                }
                List<WayDto> wayDtoList = osmDto.getWayDtoList();
                if (wayDtoList != null) {
                    nodeDtos.addAll(wayDtoList);
                }
            }
        }

        osmDtos.clear();

        loadingStatus = MAPPING_POIS;
        totalsElements = nodeDtos.size();

        if (clean) {
            cleanArea(toLoadArea);
        }

        savePoisInDB();
    }

    private void savePoisInDB() {
        int i = 0;
        for (PoiDto dto : nodeDtos) {
            killIfNeeded();
            savePoi(poiMapper.convertDtoToPoi(false, availableTypes, dto));

            if (i >= POI_PAGE_MAPPING) {
                Timber.d("----- Mapping and saving  POI on " + Thread.currentThread().getName());
                loadedElements += i;
                loadedElements = loadedElements;
                publishProgress();
                i = 0;
            }
            i++;
        }
        nodeDtos.clear();
    }

    private void savePoi(final Poi poi) {
        try {
            TransactionManager.callInTransaction(poiDao.getConnectionSource(),
                    new Callable<Void>() {
                        public Void call() throws Exception {
                            poiDao.create(poi);

                            if (poi.getTags() != null) {
                                for (PoiTag poiTag : poi.getTags()) {
                                    poiTag.setPoi(poi);
                                    poiTagDao.create(poiTag);
                                }
                            }

                            if (poi.getNodeRefs() != null) {
                                for (PoiNodeRef poiNodeRef : poi.getNodeRefs()) {
                                    poiNodeRef.setPoi(poi);
                                    poiNodeRefDao.create(poiNodeRef);
                                }
                            }
                            return null;
                        }
                    });
        } catch (SQLException e) {
            Timber.e(e, "error saving poi");
        }
    }

    private List<Long> cleanArea(final MapArea area) {
        final List<Long> poisModified = new ArrayList<>();
        databaseHelper.callInTransaction(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                List<Poi> pois = poiDao.queryForAllInRect(area.getBox());
                List<Poi> poisToDelete = new ArrayList<>();
                Collection<PoiNodeRef> nodeRefs = new ArrayList<>();
                Collection<PoiTag> tags = new ArrayList<>();

                for (Poi poi : pois) {
                    if ((!poi.getToDelete() && !poi.getUpdated() && !poi.getOld())) {
                        nodeRefs.addAll(poi.getNodeRefs());
                        tags.addAll(poi.getTags());
                        poisToDelete.add(poi);
                    } else {
                        poisModified.add(poi.getId());
                    }

                    if (tags.size() > 50) {
                        //avoid too many params in sql
                        poiNodeRefDao.delete(nodeRefs);
                        poiTagDao.delete(tags);
                        poiDao.delete(poisToDelete);
                        nodeRefs.clear();
                        tags.clear();
                        poisToDelete.clear();
                    }
                }

                poiNodeRefDao.delete(nodeRefs);
                poiTagDao.delete(tags);
                poiDao.delete(poisToDelete);
                return null;
            }
        });
        return poisModified;
    }

    private void killIfNeeded() {
        if (mustBeKilled.getValue()) {
            throw new KillByRequestException();
        }
    }

    private void publishProgress() {
        PoiLoadingProgress progress = new PoiLoadingProgress();
        progress.setTotalsElements(totalsElements);
        progress.setLoadedElements(loadedElements);
        progress.setLoadingStatus(loadingStatus);
        progress.setPois(pois);
        progress.setNotes(notes);
        progress.setDataNeedRefresh(dataNeedRefresh);
        progress.setLastUpdateDate(lastUpate);
        subscriber.onNext(progress);
        totalsElements = 0;
        loadedElements = 0;
    }
}
