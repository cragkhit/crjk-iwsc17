package org.apache.catalina.tribes.transport;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
public class RxTaskPool {
    final List<AbstractRxTask> idle = new LinkedList<>();
    final List<AbstractRxTask> used = new LinkedList<>();
    final Object mutex = new Object();
    boolean running = true;
    private int maxTasks;
    private int minTasks;
    private final TaskCreator creator;
    public RxTaskPool ( int maxTasks, int minTasks, TaskCreator creator ) throws Exception {
        this.maxTasks = maxTasks;
        this.minTasks = minTasks;
        this.creator = creator;
    }
    protected void configureTask ( AbstractRxTask task ) {
        synchronized ( task ) {
            task.setTaskPool ( this );
        }
    }
    public AbstractRxTask getRxTask() {
        AbstractRxTask worker = null;
        synchronized ( mutex ) {
            while ( worker == null && running ) {
                if ( idle.size() > 0 ) {
                    try {
                        worker = idle.remove ( 0 );
                    } catch ( java.util.NoSuchElementException x ) {
                        worker = null;
                    }
                } else if ( used.size() < this.maxTasks && creator != null ) {
                    worker = creator.createRxTask();
                    configureTask ( worker );
                } else {
                    try {
                        mutex.wait();
                    } catch ( java.lang.InterruptedException x ) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
            if ( worker != null ) {
                used.add ( worker );
            }
        }
        return ( worker );
    }
    public int available() {
        return idle.size();
    }
    public void returnWorker ( AbstractRxTask worker ) {
        if ( running ) {
            synchronized ( mutex ) {
                used.remove ( worker );
                if ( idle.size() < maxTasks && !idle.contains ( worker ) ) {
                    idle.add ( worker );
                } else {
                    worker.setDoRun ( false );
                    synchronized ( worker ) {
                        worker.notify();
                    }
                }
                mutex.notify();
            }
        } else {
            worker.setDoRun ( false );
            synchronized ( worker ) {
                worker.notify();
            }
        }
    }
    public int getMaxThreads() {
        return maxTasks;
    }
    public int getMinThreads() {
        return minTasks;
    }
    public void stop() {
        running = false;
        synchronized ( mutex ) {
            Iterator<AbstractRxTask> i = idle.iterator();
            while ( i.hasNext() ) {
                AbstractRxTask worker = i.next();
                returnWorker ( worker );
                i.remove();
            }
        }
    }
    public void setMaxTasks ( int maxThreads ) {
        this.maxTasks = maxThreads;
    }
    public void setMinTasks ( int minThreads ) {
        this.minTasks = minThreads;
    }
    public TaskCreator getTaskCreator() {
        return this.creator;
    }
    public static interface TaskCreator  {
        public AbstractRxTask createRxTask();
    }
}
