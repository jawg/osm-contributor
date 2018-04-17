package io.jawg.osmcontributor.ui.managers.sync;

import com.firebase.jobdispatcher.Constraint;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.RetryStrategy;
import com.firebase.jobdispatcher.Trigger;

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
public class PushToOSMService extends JobService {

    private static final String JOB_TAG = "JOB_TAG";
    private static final int TRIGGER_DELAY = 300;

    @Inject
    StartSynchronisation startSynchronisation;
    private OsmTemplateComponent osmTemplateComponent;

    @Override
    public void onCreate() {
        super.onCreate();
        initializeInjector();
        osmTemplateComponent.inject(this);
    }

    private void initializeInjector() {
        this.osmTemplateComponent = ((OsmTemplateApplication) getApplication()).getOsmTemplateComponent();
    }

    @Override
    public boolean onStartJob(JobParameters job) {
        Timber.i("Synchronization backend is necessary.");
        startSynchronisation.execute(new StartSynchronisationSubscriber(job));
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters job) {
        return false;
    }

    public static void schedulePushJob(FirebaseJobDispatcher dispatcher) {
        Timber.i("Scheduled sync job");
        Job job = dispatcher.newJobBuilder()
                .setService(PushToOSMService.class)
                .setTag(JOB_TAG)
                .setRecurring(false)
                .setLifetime(Lifetime.FOREVER)
                .setTrigger(Trigger.executionWindow(TRIGGER_DELAY, TRIGGER_DELAY))
                .setReplaceCurrent(true)
                .setRetryStrategy(RetryStrategy.DEFAULT_EXPONENTIAL)
                .setConstraints(Constraint.ON_ANY_NETWORK)
                .build();
        dispatcher.mustSchedule(job);
    }

    private class StartSynchronisationSubscriber extends GenericSubscriber<Void> {
        private final JobParameters job;

        StartSynchronisationSubscriber(JobParameters job) {
            this.job = job;
        }

        @Override
        public void onCompleted() {
            Timber.i("Synchronization with OSM is done");
            jobFinished(job, false);
        }

        @Override
        public void onError(Throwable e) {
            super.onError(e);
            jobFinished(job, true);
        }
    }
}