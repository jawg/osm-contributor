package io.jawg.osmcontributor.ui.managers.login;

import javax.inject.Inject;

import io.jawg.osmcontributor.ui.managers.LoginManager;
import io.jawg.osmcontributor.ui.managers.UseCase;
import io.jawg.osmcontributor.ui.managers.executor.UIThread;
import io.jawg.osmcontributor.ui.managers.login.executors.LoginThreadExecutor;
import rx.Observable;

public class CheckUserLogged extends UseCase {

    private final LoginManager loginManager;

    @Inject
    protected CheckUserLogged(LoginThreadExecutor threadExecutor, UIThread postExecutionThread, LoginManager loginManager) {
        super(threadExecutor, postExecutionThread);
        this.loginManager = loginManager;
    }

    @Override
    protected Observable<Boolean> buildUseCaseObservable() {
        return Observable.create(subscriber -> {
            subscriber.onNext(loginManager.checkCredentials());
            subscriber.onCompleted();
        });
    }
}
