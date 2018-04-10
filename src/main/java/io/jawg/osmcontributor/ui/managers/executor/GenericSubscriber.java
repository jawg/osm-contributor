package io.jawg.osmcontributor.ui.managers.executor;

import android.support.annotation.CallSuper;

import rx.Subscriber;
import timber.log.Timber;

@SuppressWarnings({ "WeakerAccess", "unused" }) public class GenericSubscriber<T> extends Subscriber<T> {

  @Override
  public void onNext(T t) {
    // Empty
  }

  @Override
  public void onCompleted() {
    // Empty
  }

  @Override
  @CallSuper public void onError(Throwable e) {
    Timber.e(e, "Error occurred in observable");
  }
}
