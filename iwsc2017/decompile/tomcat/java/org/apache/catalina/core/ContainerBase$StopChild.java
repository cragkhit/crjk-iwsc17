package org.apache.catalina.core;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.Container;
import java.util.concurrent.Callable;
private static class StopChild implements Callable<Void> {
    private Container child;
    public StopChild ( final Container child ) {
        this.child = child;
    }
    @Override
    public Void call() throws LifecycleException {
        if ( this.child.getState().isAvailable() ) {
            this.child.stop();
        }
        return null;
    }
}
