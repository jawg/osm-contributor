package io.jawg.osmcontributor.ui.managers.sync;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.jawg.osmcontributor.rest.managers.SyncManager;
import io.jawg.osmcontributor.ui.managers.executor.PostExecutionThread;
import io.jawg.osmcontributor.ui.managers.UseCase;
import io.jawg.osmcontributor.ui.managers.executor.SingleThreadExecutor;
import rx.Observable;

@Singleton
public class StartSynchronisation extends UseCase {

    private final SyncManager syncManager;

    @Inject
    public StartSynchronisation(SingleThreadExecutor threadExecutor, PostExecutionThread postExecutionThread,
                                SyncManager syncManager) {
        super(threadExecutor, postExecutionThread);
        this.syncManager = syncManager;
    }

    @Override
    protected Observable<Void> buildUseCaseObservable() {
        return syncManager.sync();
    }
}
