package org.apache.catalina.tribes.transport;
import org.apache.catalina.tribes.io.ListenCallback;
public abstract class AbstractRxTask implements Runnable {
    public static final int OPTION_DIRECT_BUFFER = 4;
    private ListenCallback callback;
    private RxTaskPool pool;
    private boolean doRun;
    private int options;
    protected boolean useBufferPool;
    public AbstractRxTask ( final ListenCallback callback ) {
        this.doRun = true;
        this.useBufferPool = true;
        this.callback = callback;
    }
    public void setTaskPool ( final RxTaskPool pool ) {
        this.pool = pool;
    }
    public void setOptions ( final int options ) {
        this.options = options;
    }
    public void setCallback ( final ListenCallback callback ) {
        this.callback = callback;
    }
    public void setDoRun ( final boolean doRun ) {
        this.doRun = doRun;
    }
    public RxTaskPool getTaskPool() {
        return this.pool;
    }
    public int getOptions() {
        return this.options;
    }
    public ListenCallback getCallback() {
        return this.callback;
    }
    public boolean isDoRun() {
        return this.doRun;
    }
    public void close() {
        this.doRun = false;
        this.notify();
    }
    public void setUseBufferPool ( final boolean usebufpool ) {
        this.useBufferPool = usebufpool;
    }
    public boolean getUseBufferPool() {
        return this.useBufferPool;
    }
}
