package io.jawg.osmcontributor.ui.managers.login;

import javax.inject.Inject;

import io.jawg.osmcontributor.ui.managers.LoginManager;
import io.jawg.osmcontributor.ui.managers.UseCase;
import io.jawg.osmcontributor.ui.managers.executor.UIThread;
import io.jawg.osmcontributor.ui.managers.login.executors.PoolThreadExecutor;
import rx.Observable;

public class UpdateCredentialsIfValid extends UseCase<Boolean> {

    private final LoginManager loginManager;
    private String login;
    private String password;

    @Inject
    protected UpdateCredentialsIfValid(PoolThreadExecutor threadExecutor, UIThread postExecutionThread, LoginManager loginManager) {
        super(threadExecutor, postExecutionThread);
        this.loginManager = loginManager;
    }

    @Override
    protected Observable<Boolean> buildUseCaseObservable() {
        return Observable.create(subscriber -> {
            subscriber.onNext(loginManager.updateCredentialsIfValid(login, password));
            subscriber.onCompleted();
        });
    }


    public UpdateCredentialsIfValid init(String login, String password) {
        this.login = login;
        this.password = password;
        return this;
    }
}
