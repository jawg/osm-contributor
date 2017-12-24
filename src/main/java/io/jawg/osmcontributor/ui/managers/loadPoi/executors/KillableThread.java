package io.jawg.osmcontributor.ui.managers.loadPoi.executors;

/**
 * Created by nico on 24/12/2017.
 */

public class KillableThread extends Thread {

    private volatile boolean cancel;

    public KillableThread(Runnable target, String name) {
        super(target, name);
        cancel = false;
    }

    public boolean isCancel() {
        return cancel;
    }

    public void setCancel(boolean cancel) {
        this.cancel = cancel;
    }
}
