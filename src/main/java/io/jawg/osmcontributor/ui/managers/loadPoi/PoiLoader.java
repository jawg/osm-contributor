package io.jawg.osmcontributor.ui.managers.loadPoi;

import com.j256.ormlite.misc.TransactionManager;

import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;

import io.jawg.osmcontributor.OsmTemplateApplication;
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
import io.jawg.osmcontributor.model.entities.Note;
import io.jawg.osmcontributor.model.entities.Poi;
import io.jawg.osmcontributor.model.entities.PoiNodeRef;
import io.jawg.osmcontributor.model.entities.PoiTag;
import io.jawg.osmcontributor.model.entities.PoiType;
import io.jawg.osmcontributor.model.entities.RelationId;
import io.jawg.osmcontributor.model.entities.relation_display.RelationDisplay;
import io.jawg.osmcontributor.model.entities.relation_display.RelationDisplayTag;
import io.jawg.osmcontributor.rest.Backend;
import io.jawg.osmcontributor.rest.NetworkException;
import io.jawg.osmcontributor.rest.dtos.osm.BlockDto;
import io.jawg.osmcontributor.rest.dtos.osm.NodeDto;
import io.jawg.osmcontributor.rest.dtos.osm.OsmDtoInterface;
import io.jawg.osmcontributor.rest.dtos.osm.PoiDto;
import io.jawg.osmcontributor.rest.dtos.osm.RelationDto;
import io.jawg.osmcontributor.rest.dtos.osm.WayDto;
import io.jawg.osmcontributor.rest.mappers.PoiMapper;
import io.jawg.osmcontributor.rest.mappers.RelationDisplayMapper;
import io.jawg.osmcontributor.ui.utils.BooleanHolder;
import io.jawg.osmcontributor.utils.FlavorUtils;
import io.jawg.osmcontributor.utils.upload.PoiLoadWrapper;
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
    private int counter;
    private final PoiDao poiDao;
    private final RelationDisplayDao relationDisplayDao;
    private final MapAreaDao mapAreaDao;
    private final Backend backend;
    private Subscriber<? super PoiLoadingProgress> subscriber;
    private boolean refreshData;
    private PoiMapper poiMapper;
    private RelationDisplayMapper relationDisplayMapper;
    private BooleanHolder mustBeKilled;
    List<PoiType> availableTypes;
    PoiTypeDao poiTypeDao;
    List<OsmDtoInterface> osmDtos;
    List<OsmDtoInterface> relationDtos;
    List<PoiDto> nodeDtos = new ArrayList<>();
    List<BlockDto> blockDtos = new ArrayList<>();
    List<RelationDto> relationDisplayDtos = new ArrayList<>();

    //progress
    private PoiLoadingProgress.LoadingStatus loadingStatus;
    private boolean dataNeedRefresh;
    private long totalsElements = 0L;
    private long loadedElements = 0L;
    private PoiTagDao poiTagDao;
    private RelationDisplayTagDao relationDisplayTagDao;
    private PoiNodeRefDao poiNodeRefDao;
    private RelationIdDao relationIdDao;
    private LocalDateTime lastUpdate;
    private List<Poi> pois = new ArrayList<>();
    private List<Note> notes = new ArrayList<>();
    private DatabaseHelper databaseHelper;
    private MapArea mapArea;


    public PoiLoader(PoiDao poiDao, RelationDisplayDao relationDisplayDao, RelationDisplayTagDao relationDisplayTagDao,
                     Backend backend, MapAreaDao mapAreaDao, Subscriber<? super PoiLoadingProgress> subscriber,
                     BooleanHolder mustBeKilled, PoiMapper poiMapper, RelationDisplayMapper relationDisplayMapper, PoiTypeDao poiTypeDao,
                     PoiTagDao poiTagDao, PoiNodeRefDao poiNodeRefDao, RelationIdDao relationIdDao,
                     DatabaseHelper databaseHelper) {
        this.backend = backend;
        this.poiDao = poiDao;
        this.relationDisplayDao = relationDisplayDao;
        this.relationDisplayTagDao = relationDisplayTagDao;
        this.mapAreaDao = mapAreaDao;
        this.subscriber = subscriber;
        this.mustBeKilled = mustBeKilled;
        this.poiMapper = poiMapper;
        this.relationDisplayMapper = relationDisplayMapper;
        this.poiTypeDao = poiTypeDao;
        this.poiTagDao = poiTagDao;
        this.poiNodeRefDao = poiNodeRefDao;
        this.relationIdDao = relationIdDao;
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
            LocalDateTime lastUpdate = new LocalDateTime(mapArea.getUpdateDate());
            //we check if the data is outDated and ask for Update
            if (LocalDateTime.now().isAfter(lastUpdate.plusWeeks(1))) {
                outDatedData = true;
                this.lastUpdate = lastUpdate;
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

        //for jungle bus, load relations for displaying purpose
        //   loadAndSaveRelationDisplaysFromBackend(toLoadArea, refreshData);

        //publish poi loaded
        toLoadArea.setUpdateDate(new DateTime(System.currentTimeMillis()));

        Timber.d("----- Saving Area in BDD : " + toLoadArea.getId());
        mapAreaDao.createOrUpdate(toLoadArea);

    }


    private void loadAndSavePoisFromBackend(MapArea toLoadArea, boolean clean) {
        OsmDtoInterface osmDto;
        Boolean hasNetwork = OsmTemplateApplication.hasNetwork();
        if (hasNetwork != null && !hasNetwork) {
            throw new NetworkException();
        }

        loadingStatus = LOADING_FROM_SERVER;
        publishProgress();
        loadedElements = 0L;
        nodeDtos.clear();
        blockDtos.clear();

        final List<PoiLoadWrapper> poiLoadWrapper = backend.getPoisDtosInBox(toLoadArea.getBox());
        for (PoiLoadWrapper poiLoad : poiLoadWrapper) {
            killIfNeeded();
            osmDto = poiLoad.getOsmDto();

            if (FlavorUtils.isBus(poiLoad.getPoiType())) {
                if (osmDto.getBlockList() != null) {
                    for (BlockDto blockDto : osmDto.getBlockList()) {
                        if (blockDto != null && blockDto.getNodeDtoList() != null) {
                            blockDtos.add(blockDto);
                        }
                    }
                }
                if (osmDto.getRelationDtoList() != null) {
                    relationDisplayDtos.addAll(osmDto.getRelationDtoList());
                }
            } else {
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
        }

        loadingStatus = MAPPING_POIS;
        totalsElements = blockDtos.size() + nodeDtos.size();

        if (clean) {
            cleanArea(toLoadArea);
        }

        savePoisInDB();
        saveRelationDisplaysInDB();
    }

    private void saveRelationDisplaysInDB() {
        for (RelationDto re : relationDisplayDtos) {
            saveRelationDisplay(relationDisplayMapper.convertDTOtoRelation(re));
        }
    }

    private void savePoisInDB() {
        counter = 0;

        for (BlockDto dto : blockDtos) {
            killIfNeeded();
            savePoi(poiMapper.convertDtoToPoi(false, availableTypes, dto.getNodeDtoList().get(0), dto.getRelationIdDtoList()));
            manageProgress();
            counter++;
        }
        for (PoiDto dto : nodeDtos) {
            killIfNeeded();
            savePoi(poiMapper.convertDtoToPoi(false, availableTypes, dto, null));
            manageProgress();
            counter++;
        }

        nodeDtos.clear();
        blockDtos.clear();
    }

    private void manageProgress() {
        if (counter >= POI_PAGE_MAPPING) {
            Timber.d("----- Mapping and saving  POI on " + Thread.currentThread().getName());
            loadedElements += counter;
            publishProgress();
            counter = 0;
        }
    }

    private void savePoi(final Poi poi) {
        try {
            TransactionManager.callInTransaction(poiDao.getConnectionSource(),
                    (Callable<Void>) () -> {
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
                        if (poi.getRelationIds() != null) {
                            for (RelationId re : poi.getRelationIds()) {
                                re.setPoi(poi);
                                relationIdDao.create(re);
                            }
                        }
                        return null;
                    });
        } catch (SQLException e) {
            Timber.e(e, "error saving poi");
        }
    }

    private void saveRelationDisplay(final RelationDisplay relationDisplay) {
        try {
            TransactionManager.callInTransaction(relationDisplayDao.getConnectionSource(),
                    () -> {
                        RelationDisplay relation = relationDisplayDao.createIfNotExists(relationDisplay);
                        if (relation != null && relationDisplay.getTags() != null) {
                            for (RelationDisplayTag relationDisplayTag : relationDisplay.getTags()) {
                                relationDisplayTag.setRelationDisplay(relation);
                                relationDisplayTagDao.create(relationDisplayTag);
                            }
                        }
                        return null;
                    });
        } catch (SQLException e) {
            Timber.e(e, "error saving relationDisplay");
        }
    }

    private List<Long> cleanArea(final MapArea area) {
        final List<Long> poisModified = new ArrayList<>();
        databaseHelper.callInTransaction((Callable<Void>) () -> {
            List<Poi> pois = poiDao.queryForAllInRect(area.getBox());
            List<Poi> poisToDelete = new ArrayList<>();
            Collection<PoiNodeRef> nodeRefs = new ArrayList<>();
            Collection<PoiTag> tags = new ArrayList<>();
            Collection<RelationId> relationIds = new ArrayList<>();

            for (Poi poi : pois) {
                if ((!poi.getToDelete() && !poi.getUpdated() && !poi.getOld())) {
                    nodeRefs.addAll(poi.getNodeRefs());
                    tags.addAll(poi.getTags());
                    poisToDelete.add(poi);
                    relationIds.addAll(poi.getRelationIds());
                } else {
                    poisModified.add(poi.getId());
                }

                if (tags.size() > 50) {
                    //avoid too many params in sql
                    poiNodeRefDao.delete(nodeRefs);
                    poiTagDao.delete(tags);
                    poiDao.delete(poisToDelete);
                    relationIdDao.delete(relationIds);
                    nodeRefs.clear();
                    tags.clear();
                    poisToDelete.clear();
                    relationIds.clear();
                }
            }

            poiNodeRefDao.delete(nodeRefs);
            poiTagDao.delete(tags);
            poiDao.delete(poisToDelete);
            relationIdDao.delete(relationIds);
            return null;
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
        progress.setLastUpdateDate(lastUpdate);
        subscriber.onNext(progress);
        totalsElements = 0;
        loadedElements = 0;
    }
}
