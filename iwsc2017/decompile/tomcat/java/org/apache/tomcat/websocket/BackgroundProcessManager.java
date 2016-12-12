package org.apache.tomcat.websocket;
import org.apache.juli.logging.LogFactory;
import java.util.Iterator;
import org.apache.tomcat.util.ExceptionUtils;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.apache.tomcat.util.res.StringManager;
import org.apache.juli.logging.Log;
public class BackgroundProcessManager {
    private static final Log log;
    private static final StringManager sm;
    private static final BackgroundProcessManager instance;
    private final Set<BackgroundProcess> processes;
    private final Object processesLock;
    private WsBackgroundThread wsBackgroundThread;
    public static BackgroundProcessManager getInstance() {
        return BackgroundProcessManager.instance;
    }
    private BackgroundProcessManager() {
        this.processes = new HashSet<BackgroundProcess>();
        this.processesLock = new Object();
        this.wsBackgroundThread = null;
    }
    public void register ( final BackgroundProcess process ) {
        synchronized ( this.processesLock ) {
            if ( this.processes.size() == 0 ) {
                ( this.wsBackgroundThread = new WsBackgroundThread ( this ) ).setContextClassLoader ( this.getClass().getClassLoader() );
                this.wsBackgroundThread.setDaemon ( true );
                this.wsBackgroundThread.start();
            }
            this.processes.add ( process );
        }
    }
    public void unregister ( final BackgroundProcess process ) {
        synchronized ( this.processesLock ) {
            this.processes.remove ( process );
            if ( this.wsBackgroundThread != null && this.processes.size() == 0 ) {
                this.wsBackgroundThread.halt();
                this.wsBackgroundThread = null;
            }
        }
    }
    private void process() {
        final Set<BackgroundProcess> currentProcesses = new HashSet<BackgroundProcess>();
        synchronized ( this.processesLock ) {
            currentProcesses.addAll ( this.processes );
        }
        for ( final BackgroundProcess process : currentProcesses ) {
            try {
                process.backgroundProcess();
            } catch ( Throwable t ) {
                ExceptionUtils.handleThrowable ( t );
                BackgroundProcessManager.log.error ( BackgroundProcessManager.sm.getString ( "backgroundProcessManager.processFailed" ), t );
            }
        }
    }
    int getProcessCount() {
        synchronized ( this.processesLock ) {
            return this.processes.size();
        }
    }
    void shutdown() {
        synchronized ( this.processesLock ) {
            this.processes.clear();
            if ( this.wsBackgroundThread != null ) {
                this.wsBackgroundThread.halt();
                this.wsBackgroundThread = null;
            }
        }
    }
    static {
        log = LogFactory.getLog ( BackgroundProcessManager.class );
        sm = StringManager.getManager ( BackgroundProcessManager.class );
        instance = new BackgroundProcessManager();
    }
    private static class WsBackgroundThread extends Thread {
        private final BackgroundProcessManager manager;
        private volatile boolean running;
        public WsBackgroundThread ( final BackgroundProcessManager manager ) {
            this.running = true;
            this.setName ( "WebSocket background processing" );
            this.manager = manager;
        }
        @Override
        public void run() {
            while ( this.running ) {
                try {
                    Thread.sleep ( 1000L );
                } catch ( InterruptedException ex ) {}
                this.manager.process();
            }
        }
        public void halt() {
            this.setName ( "WebSocket background processing - stopping" );
            this.running = false;
        }
    }
}
