package io.jawg.osmcontributor.ui.managers.loadPoi.executors;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


public class PoiThreadPoolExecutor extends ThreadPoolExecutor {
    private static final int INITIAL_POOL_SIZE = 5;
    private static final int MAX_POOL_SIZE = 5;

    // Sets the amount of time an idle thread waits before terminating
    private static final long KEEP_ALIVE_TIME = 10;

    // Sets the Time Unit to seconds
    private static final TimeUnit KEEP_ALIVE_TIME_UNIT = TimeUnit.SECONDS;


    private final Map<Runnable, Thread> executingThreads;
    private int counterPool = 0;

    public PoiThreadPoolExecutor() {
        super(INITIAL_POOL_SIZE, MAX_POOL_SIZE,
                KEEP_ALIVE_TIME, KEEP_ALIVE_TIME_UNIT, new LinkedBlockingQueue<Runnable>(), new JobThreadFactory());
        executingThreads = new HashMap<>(MAX_POOL_SIZE);
    }

    @Override
    protected synchronized void beforeExecute(Thread t, Runnable r) {
        super.beforeExecute(t, r);
        executingThreads.put(r, t);
    }

    @Override
    protected synchronized void afterExecute(Runnable r, Throwable t) {
        super.afterExecute(r, t);
        if (executingThreads.containsKey(r)) {
            executingThreads.remove(r);
        }
    }

    private static class JobThreadFactory implements ThreadFactory {
        private static final String THREAD_NAME = "android_";
        private int counter = 0;

        @Override
        public Thread newThread(Runnable runnable) {
            return new KillableThread(runnable, THREAD_NAME + counter++);
        }
    }

}