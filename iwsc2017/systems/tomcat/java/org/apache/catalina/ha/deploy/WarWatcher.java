package org.apache.catalina.ha.deploy;
import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.util.res.StringManager;
public class WarWatcher {
    private static final Log log = LogFactory.getLog ( WarWatcher.class );
    private static final StringManager sm = StringManager.getManager ( WarWatcher.class );
    protected final File watchDir;
    protected final FileChangeListener listener;
    protected final Map<String, WarInfo> currentStatus = new HashMap<>();
    public WarWatcher ( FileChangeListener listener, File watchDir ) {
        this.listener = listener;
        this.watchDir = watchDir;
    }
    public void check() {
        if ( log.isDebugEnabled() ) {
            log.debug ( sm.getString ( "warWatcher.checkingWars", watchDir ) );
        }
        File[] list = watchDir.listFiles ( new WarFilter() );
        if ( list == null ) {
            log.warn ( sm.getString ( "warWatcher.cantListWatchDir",
                                      watchDir ) );
            list = new File[0];
        }
        for ( int i = 0; i < list.length; i++ ) {
            if ( !list[i].exists() )
                log.warn ( sm.getString ( "warWatcher.listedFileDoesNotExist",
                                          list[i], watchDir ) );
            addWarInfo ( list[i] );
        }
        for ( Iterator<Map.Entry<String, WarInfo>> i =
                    currentStatus.entrySet().iterator(); i.hasNext(); ) {
            Map.Entry<String, WarInfo> entry = i.next();
            WarInfo info = entry.getValue();
            if ( log.isTraceEnabled() )
                log.trace ( sm.getString ( "warWatcher.checkingWar",
                                           info.getWar() ) );
            int check = info.check();
            if ( check == 1 ) {
                listener.fileModified ( info.getWar() );
            } else if ( check == -1 ) {
                listener.fileRemoved ( info.getWar() );
                i.remove();
            }
            if ( log.isTraceEnabled() )
                log.trace ( sm.getString ( "warWatcher.checkWarResult",
                                           Integer.valueOf ( check ),
                                           info.getWar() ) );
        }
    }
    protected void addWarInfo ( File warfile ) {
        WarInfo info = currentStatus.get ( warfile.getAbsolutePath() );
        if ( info == null ) {
            info = new WarInfo ( warfile );
            info.setLastState ( -1 );
            currentStatus.put ( warfile.getAbsolutePath(), info );
        }
    }
    public void clear() {
        currentStatus.clear();
    }
    protected static class WarFilter implements java.io.FilenameFilter {
        @Override
        public boolean accept ( File path, String name ) {
            if ( name == null ) {
                return false;
            }
            return name.endsWith ( ".war" );
        }
    }
    protected static class WarInfo {
        protected final File war;
        protected long lastChecked = 0;
        protected long lastState = 0;
        public WarInfo ( File war ) {
            this.war = war;
            this.lastChecked = war.lastModified();
            if ( !war.exists() ) {
                lastState = -1;
            }
        }
        public boolean modified() {
            return war.exists() && war.lastModified() > lastChecked;
        }
        public boolean exists() {
            return war.exists();
        }
        public int check() {
            int result = 0;
            if ( modified() ) {
                result = 1;
                lastState = result;
            } else if ( ( !exists() ) && ( ! ( lastState == -1 ) ) ) {
                result = -1;
                lastState = result;
            } else if ( ( lastState == -1 ) && exists() ) {
                result = 1;
                lastState = result;
            }
            this.lastChecked = System.currentTimeMillis();
            return result;
        }
        public File getWar() {
            return war;
        }
        @Override
        public int hashCode() {
            return war.getAbsolutePath().hashCode();
        }
        @Override
        public boolean equals ( Object other ) {
            if ( other instanceof WarInfo ) {
                WarInfo wo = ( WarInfo ) other;
                return wo.getWar().equals ( getWar() );
            } else {
                return false;
            }
        }
        protected void setLastState ( int lastState ) {
            this.lastState = lastState;
        }
    }
}
