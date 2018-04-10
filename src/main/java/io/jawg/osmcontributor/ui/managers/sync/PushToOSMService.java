package io.jawg.osmcontributor.ui.managers.sync;

import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v4.app.JobIntentService;

import javax.inject.Inject;

import io.jawg.osmcontributor.OsmTemplateApplication;
import io.jawg.osmcontributor.modules.OsmTemplateComponent;
import io.jawg.osmcontributor.ui.managers.executor.GenericSubscriber;
import timber.log.Timber;

/**
 * This service is used to trigger a synchronization with the backend.
 * It will only be called when the network is available. If the user is in offline mode,
 * when he goes online, then this service will be triggered.
 */
public class PushToOSMService extends JobIntentService {

    @Inject
    StartSynchronisation startSynchronisation;
    private OsmTemplateComponent osmTemplateComponent;

    @Override
    public void onCreate() {
        super.onCreate();
        initializeInjector();
        osmTemplateComponent.inject(this);
    }

    @Override
    protected void onHandleWork(@Nullable Intent intent) {
        Timber.i("Synchronization backend is necessary.");
        startSynchronisation.execute(new StartSynchronisationSubscriber());
    }

    private void initializeInjector() {
        this.osmTemplateComponent = ((OsmTemplateApplication) getApplication()).getOsmTemplateComponent();
    }

    private class StartSynchronisationSubscriber extends GenericSubscriber<Void> {
        @Override
        public void onCompleted() {
            Timber.i("Synchronization with OSM is done");
        }
    }
}