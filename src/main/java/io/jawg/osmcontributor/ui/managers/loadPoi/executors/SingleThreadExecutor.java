package io.jawg.osmcontributor.ui.managers.loadPoi.executors;


import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public abstract class SingleThreadExecutor implements ThreadExecutor {

    private final Executor executor;
    private final ThreadFactory threadFactory;

    public SingleThreadExecutor(String threadName) {
        this.threadFactory = new SingleThreadFactory(threadName);
        this.executor = Executors.newSingleThreadExecutor(threadFactory);
    }

    @Override
    public void execute(Runnable runnable) {
        if (runnable == null) {
            throw new IllegalArgumentException("Runnable to execute cannot be null");
        }
        executor.execute(runnable);
    }

    private static class SingleThreadFactory implements ThreadFactory {
        private final String threadName;
        private int counter = 0;

        private SingleThreadFactory(String threadName) {
            this.threadName = threadName;
        }

        @Override
        public Thread newThread(Runnable runnable) {
            return new Thread(runnable, threadName + counter++);
        }
    }
}
