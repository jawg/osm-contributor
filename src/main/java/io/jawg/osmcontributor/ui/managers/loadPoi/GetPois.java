package io.jawg.osmcontributor.ui.managers.loadPoi;

import javax.inject.Inject;

import io.jawg.osmcontributor.ui.managers.loadPoi.executors.PoiThreadExecutor;
import io.jawg.osmcontributor.ui.managers.loadPoi.executors.PostExecutionThread;
import io.jawg.osmcontributor.utils.Box;
import rx.Observable;

public class GetPois extends UseCase {
    private final PoiRepository poiRepository;
    private Box box;

    @Inject
    public GetPois(PoiThreadExecutor threadExecutor, PostExecutionThread postExecutionThread, PoiRepository poiRepository) {
        super(threadExecutor, postExecutionThread);
        this.poiRepository = poiRepository;
    }

    @Override
    protected Observable buildUseCaseObservable() {
        return this.poiRepository.getPoiFromBox(box);
    }

    public GetPois init(Box box) {
        this.box = box;
        return this;
    }
}
