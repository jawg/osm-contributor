package io.jawg.osmcontributor.ui.managers.loadPoi.executors;

import io.jawg.osmcontributor.ui.utils.BooleanHolder;
import rx.Observable;
import rx.Subscriber;
import timber.log.Timber;

public abstract class CancelableSubscriber<T> implements Observable.OnSubscribe<T> {
    protected volatile BooleanHolder mustBeKilled = new BooleanHolder(false);

    public CancelableSubscriber() {
        //emtpy
    }

    @Override
    public void call(Subscriber<? super T> subscriber) {
        cancelableCall(subscriber);
    }

    public abstract void cancelableCall(Subscriber<? super T> subscriber);

    public BooleanHolder mustBeKilled() {
        return mustBeKilled;
    }

    public void kill() {
        Timber.e("nico ask for kill");
        mustBeKilled.setValue(true);
    }
}
