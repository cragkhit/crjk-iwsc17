package org.apache.tomcat.util.net;
public abstract static class Acceptor implements Runnable {
    protected volatile AcceptorState state;
    private String threadName;
    public Acceptor() {
        this.state = AcceptorState.NEW;
    }
    public final AcceptorState getState() {
        return this.state;
    }
    protected final void setThreadName ( final String threadName ) {
        this.threadName = threadName;
    }
    protected final String getThreadName() {
        return this.threadName;
    }
    public enum AcceptorState {
        NEW,
        RUNNING,
        PAUSED,
        ENDED;
    }
}
