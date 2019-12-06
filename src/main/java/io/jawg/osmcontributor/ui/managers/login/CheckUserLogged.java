package io.jawg.osmcontributor.ui.managers.login;

import javax.inject.Inject;

import io.jawg.osmcontributor.ui.managers.LoginManager;
import io.jawg.osmcontributor.ui.managers.UseCase;
import io.jawg.osmcontributor.ui.managers.executor.UIThread;
import io.jawg.osmcontributor.ui.managers.login.executors.PoolThreadExecutor;
import rx.Observable;

public class CheckUserLogged extends UseCase<Boolean> {

    private final LoginManager loginManager;

    @Inject
    protected CheckUserLogged(PoolThreadExecutor threadExecutor, UIThread postExecutionThread, LoginManager loginManager) {
        super(threadExecutor, postExecutionThread);
        this.loginManager = loginManager;
    }

    @Override
    protected Observable<Boolean> buildUseCaseObservable() {
        return Observable.unsafeCreate(subscriber -> {
            subscriber.onNext(loginManager.isUserLogged());
            subscriber.onCompleted();
        });
    }
}
