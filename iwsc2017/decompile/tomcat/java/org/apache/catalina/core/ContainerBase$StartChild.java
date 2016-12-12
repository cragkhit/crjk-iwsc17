package org.apache.catalina.core;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.Container;
import java.util.concurrent.Callable;
private static class StartChild implements Callable<Void> {
    private Container child;
    public StartChild ( final Container child ) {
        this.child = child;
    }
    @Override
    public Void call() throws LifecycleException {
        this.child.start();
        return null;
    }
}
