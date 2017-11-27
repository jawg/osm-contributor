package io.jawg.osmcontributor.ui.managers.loadPoi;

import javax.inject.Inject;

import io.jawg.osmcontributor.ui.managers.loadPoi.executors.CancelableObservable;
import io.jawg.osmcontributor.ui.managers.loadPoi.executors.PoiThreadExecutor;
import io.jawg.osmcontributor.ui.managers.loadPoi.executors.PostExecutionThread;
import io.jawg.osmcontributor.utils.Box;
import rx.Observable;

public class GetPois extends UseCase {
    private final PoiRepository poiRepository;
    private Box box;
    private boolean refreshData;
    CancelableObservable<PoiLoadingProgress> cancelableObservable;

    @Inject
    public GetPois(PoiThreadExecutor threadExecutor, PostExecutionThread postExecutionThread, PoiRepository poiRepository) {
        super(threadExecutor, postExecutionThread);
        this.poiRepository = poiRepository;
    }

    @Override
    protected Observable<PoiLoadingProgress> buildUseCaseObservable() {
        cancelableObservable = this.poiRepository.getPoiFromBox(box, refreshData);
        return cancelableObservable.getObservable();
    }

    @Override
    public void unsubscribe() {
        if (cancelableObservable != null) {
            cancelableObservable.kill();
        }
        super.unsubscribe();
    }

    public GetPois init(Box box, boolean refreshData) {
        this.box = box;
        this.refreshData = refreshData;
        return this;
    }
}
