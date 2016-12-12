package org.apache.tomcat.util.threads;
private static class WrappingRunnable implements Runnable {
    private Runnable wrappedRunnable;
    WrappingRunnable ( final Runnable wrappedRunnable ) {
        this.wrappedRunnable = wrappedRunnable;
    }
    @Override
    public void run() {
        try {
            this.wrappedRunnable.run();
        } catch ( StopPooledThreadException exc ) {
            TaskThread.access$000().debug ( "Thread exiting on purpose", exc );
        }
    }
}
