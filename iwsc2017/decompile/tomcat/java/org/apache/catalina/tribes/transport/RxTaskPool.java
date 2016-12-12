package org.apache.catalina.tribes.transport;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.LinkedList;
import java.util.List;
public class RxTaskPool {
    final List<AbstractRxTask> idle;
    final List<AbstractRxTask> used;
    final Object mutex;
    boolean running;
    private int maxTasks;
    private int minTasks;
    private final TaskCreator creator;
    public RxTaskPool ( final int maxTasks, final int minTasks, final TaskCreator creator ) throws Exception {
        this.idle = new LinkedList<AbstractRxTask>();
        this.used = new LinkedList<AbstractRxTask>();
        this.mutex = new Object();
        this.running = true;
        this.maxTasks = maxTasks;
        this.minTasks = minTasks;
        this.creator = creator;
    }
    protected void configureTask ( final AbstractRxTask task ) {
        synchronized ( task ) {
            task.setTaskPool ( this );
        }
    }
    public AbstractRxTask getRxTask() {
        AbstractRxTask worker = null;
        synchronized ( this.mutex ) {
            while ( worker == null && this.running ) {
                if ( this.idle.size() > 0 ) {
                    try {
                        worker = this.idle.remove ( 0 );
                    } catch ( NoSuchElementException x ) {
                        worker = null;
                    }
                } else if ( this.used.size() < this.maxTasks && this.creator != null ) {
                    worker = this.creator.createRxTask();
                    this.configureTask ( worker );
                } else {
                    try {
                        this.mutex.wait();
                    } catch ( InterruptedException x2 ) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
            if ( worker != null ) {
                this.used.add ( worker );
            }
        }
        return worker;
    }
    public int available() {
        return this.idle.size();
    }
    public void returnWorker ( final AbstractRxTask worker ) {
        if ( this.running ) {
            synchronized ( this.mutex ) {
                this.used.remove ( worker );
                if ( this.idle.size() < this.maxTasks && !this.idle.contains ( worker ) ) {
                    this.idle.add ( worker );
                } else {
                    worker.setDoRun ( false );
                    synchronized ( worker ) {
                        worker.notify();
                    }
                }
                this.mutex.notify();
            }
        } else {
            worker.setDoRun ( false );
            synchronized ( worker ) {
                worker.notify();
            }
        }
    }
    public int getMaxThreads() {
        return this.maxTasks;
    }
    public int getMinThreads() {
        return this.minTasks;
    }
    public void stop() {
        this.running = false;
        synchronized ( this.mutex ) {
            final Iterator<AbstractRxTask> i = this.idle.iterator();
            while ( i.hasNext() ) {
                final AbstractRxTask worker = i.next();
                this.returnWorker ( worker );
                i.remove();
            }
        }
    }
    public void setMaxTasks ( final int maxThreads ) {
        this.maxTasks = maxThreads;
    }
    public void setMinTasks ( final int minThreads ) {
        this.minTasks = minThreads;
    }
    public TaskCreator getTaskCreator() {
        return this.creator;
    }
    public interface TaskCreator {
        AbstractRxTask createRxTask();
    }
}
