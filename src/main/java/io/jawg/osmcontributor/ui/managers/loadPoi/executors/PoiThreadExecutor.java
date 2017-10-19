package io.jawg.osmcontributor.ui.managers.loadPoi.executors;

import javax.inject.Inject;
import javax.inject.Singleton;


@Singleton
public class PoiThreadExecutor extends SingleThreadExecutor {
    private static final String THREAD_NAME = "poi_";

    @Inject
    public PoiThreadExecutor() {
        super(THREAD_NAME);
    }
}
