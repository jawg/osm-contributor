package io.jawg.osmcontributor.ui.managers.login.executors;

import android.support.annotation.NonNull;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.jawg.osmcontributor.ui.managers.executor.ThreadExecutor;


@Singleton
public class LoginThreadExecutor implements ThreadExecutor {

    private final ThreadPoolExecutor threadPoolExecutor;

    @Inject
    public LoginThreadExecutor() {
        this.threadPoolExecutor = new ThreadPoolExecutor(1, 2, 10, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(), new LoginThreadFactory());
    }

    @Override
    public void execute(@NonNull Runnable runnable) {
        this.threadPoolExecutor.execute(runnable);
    }

    private static class LoginThreadFactory implements ThreadFactory {
        private int counter = 0;

        @Override
        public Thread newThread(@NonNull Runnable runnable) {
            return new Thread(runnable, "login_" + counter++);
        }
    }
}