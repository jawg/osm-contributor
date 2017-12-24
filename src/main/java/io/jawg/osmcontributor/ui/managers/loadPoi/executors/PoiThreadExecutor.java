package io.jawg.osmcontributor.ui.managers.loadPoi.executors;

import javax.inject.Inject;
import javax.inject.Singleton;


@Singleton
public class PoiThreadExecutor implements ThreadExecutor {

    private PoiThreadPoolExecutor threadPoolExecutor;

    @Inject
    public PoiThreadExecutor() {
        this.threadPoolExecutor = new PoiThreadPoolExecutor();
    }

    @Override
    public void execute(Runnable runnable) {
        if (runnable == null) {
            throw new IllegalArgumentException("Runnable to execute cannot be null");
        }
        this.threadPoolExecutor.execute(runnable);
    }

    public void clearQueue() {
        if (threadPoolExecutor != null) {
            threadPoolExecutor.getQueue().clear();
        }
    }
}
