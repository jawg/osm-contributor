package io.jawg.osmcontributor.ui.managers.loadPoi.executors;

import rx.Observable;

public class CancelableObservable<T> {
    private CancelableSubscriber cancelableSubscriber;
    private Observable<T> observable;

    public CancelableObservable(CancelableSubscriber<T> f) {
        cancelableSubscriber = f;
        observable = Observable.create(f).onBackpressureBuffer();
    }

    public Observable<T> getObservable() {
        return observable;
    }

    public void kill() {
        if (cancelableSubscriber != null) {
            cancelableSubscriber.kill();
        }
    }
}
