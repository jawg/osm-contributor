package io.jawg.osmcontributor.ui.managers.loadPoi;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.jawg.osmcontributor.model.entities.MapArea;
import io.jawg.osmcontributor.ui.managers.loadPoi.executors.CancelableObservable;
import io.jawg.osmcontributor.ui.managers.loadPoi.executors.PoiThreadExecutor;
import io.jawg.osmcontributor.ui.managers.loadPoi.executors.PostExecutionThread;
import io.jawg.osmcontributor.utils.Box;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import timber.log.Timber;

import static io.jawg.osmcontributor.ui.managers.loadPoi.AreasUtils.computeMapAreaOfBox;

public class GetPois extends UseCase {
    private final PoiRepository poiRepository;
    private Box box;
    private boolean refreshData;
    List<CancelableObservable<PoiLoadingProgress>> cancelableObservable;

    @Inject
    public GetPois(PoiThreadExecutor threadExecutor, PostExecutionThread postExecutionThread, PoiRepository poiRepository) {
        super(threadExecutor, postExecutionThread);
        this.poiRepository = poiRepository;
        cancelableObservable = new ArrayList<>();
    }

    @Override
    protected Observable<PoiLoadingProgress> buildUseCaseObservable() {
        return null;
    }

    @Override
    public void execute(Subscriber useCaseSubscriber) {
        final List<MapArea> areasNeeded = computeMapAreaOfBox(box);
        ((PoiThreadExecutor) threadExecutor).clearQueue();
        poiRepository.verifyCount(box)
                .concatWith(Observable.from(areasNeeded)
                        .flatMap(new Func1<MapArea, Observable<PoiLoadingProgress>>() {
                            @Override
                            public Observable<PoiLoadingProgress> call(MapArea mapArea) {
                                return Observable.just(mapArea)
                                        .subscribeOn(Schedulers.from(threadExecutor))
                                        .flatMap(new Func1<MapArea, Observable<PoiLoadingProgress>>() {
                                            @Override
                                            public Observable<PoiLoadingProgress> call(MapArea mapArea) {
                                                Timber.d(" lodaing " + mapArea.getId() + "  on  " + Thread.currentThread().getName());
                                                CancelableObservable<PoiLoadingProgress> poiFromArea = poiRepository.getPoiFromArea(mapArea, refreshData);
                                                cancelableObservable.add(poiFromArea);
                                                return poiFromArea.getObservable();
                                            }
                                        });
                            }
                        }))
                .concatWith(poiRepository.getPoisFromDB(box).subscribeOn(Schedulers.from(threadExecutor)))
                .concatWith(Observable.just(new PoiLoadingProgress(PoiLoadingProgress.LoadingStatus.FINISH)))
                .onBackpressureBuffer()
                .observeOn(postExecutionThread.getScheduler())
                .subscribe(useCaseSubscriber);
    }

    @Override
    public void unsubscribe() {
        if (cancelableObservable != null) {
            for (CancelableObservable<PoiLoadingProgress> observable : cancelableObservable) {
                if (observable != null) {
                    observable.kill();
                }
            }
            cancelableObservable.clear();
        }
        super.unsubscribe();
    }


    public GetPois init(Box box, boolean refreshData) {
        this.box = box;
        this.refreshData = refreshData;
        return this;
    }
}
