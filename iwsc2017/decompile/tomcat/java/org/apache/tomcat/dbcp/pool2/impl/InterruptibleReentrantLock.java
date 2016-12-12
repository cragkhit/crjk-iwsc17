package org.apache.tomcat.dbcp.pool2.impl;
import java.util.Iterator;
import java.util.Collection;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
class InterruptibleReentrantLock extends ReentrantLock {
    private static final long serialVersionUID = 1L;
    public InterruptibleReentrantLock ( final boolean fairness ) {
        super ( fairness );
    }
    public void interruptWaiters ( final Condition condition ) {
        final Collection<Thread> threads = this.getWaitingThreads ( condition );
        for ( final Thread thread : threads ) {
            thread.interrupt();
        }
    }
}
