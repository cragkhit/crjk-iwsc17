package org.apache.catalina.ha.deploy;
import org.apache.juli.logging.LogFactory;
import java.util.Iterator;
import java.io.FilenameFilter;
import java.util.HashMap;
import java.util.Map;
import java.io.File;
import org.apache.tomcat.util.res.StringManager;
import org.apache.juli.logging.Log;
public class WarWatcher {
    private static final Log log;
    private static final StringManager sm;
    protected final File watchDir;
    protected final FileChangeListener listener;
    protected final Map<String, WarInfo> currentStatus;
    public WarWatcher ( final FileChangeListener listener, final File watchDir ) {
        this.currentStatus = new HashMap<String, WarInfo>();
        this.listener = listener;
        this.watchDir = watchDir;
    }
    public void check() {
        if ( WarWatcher.log.isDebugEnabled() ) {
            WarWatcher.log.debug ( WarWatcher.sm.getString ( "warWatcher.checkingWars", this.watchDir ) );
        }
        File[] list = this.watchDir.listFiles ( new WarFilter() );
        if ( list == null ) {
            WarWatcher.log.warn ( WarWatcher.sm.getString ( "warWatcher.cantListWatchDir", this.watchDir ) );
            list = new File[0];
        }
        for ( int i = 0; i < list.length; ++i ) {
            if ( !list[i].exists() ) {
                WarWatcher.log.warn ( WarWatcher.sm.getString ( "warWatcher.listedFileDoesNotExist", list[i], this.watchDir ) );
            }
            this.addWarInfo ( list[i] );
        }
        final Iterator<Map.Entry<String, WarInfo>> j = this.currentStatus.entrySet().iterator();
        while ( j.hasNext() ) {
            final Map.Entry<String, WarInfo> entry = j.next();
            final WarInfo info = entry.getValue();
            if ( WarWatcher.log.isTraceEnabled() ) {
                WarWatcher.log.trace ( WarWatcher.sm.getString ( "warWatcher.checkingWar", info.getWar() ) );
            }
            final int check = info.check();
            if ( check == 1 ) {
                this.listener.fileModified ( info.getWar() );
            } else if ( check == -1 ) {
                this.listener.fileRemoved ( info.getWar() );
                j.remove();
            }
            if ( WarWatcher.log.isTraceEnabled() ) {
                WarWatcher.log.trace ( WarWatcher.sm.getString ( "warWatcher.checkWarResult", check, info.getWar() ) );
            }
        }
    }
    protected void addWarInfo ( final File warfile ) {
        WarInfo info = this.currentStatus.get ( warfile.getAbsolutePath() );
        if ( info == null ) {
            info = new WarInfo ( warfile );
            info.setLastState ( -1 );
            this.currentStatus.put ( warfile.getAbsolutePath(), info );
        }
    }
    public void clear() {
        this.currentStatus.clear();
    }
    static {
        log = LogFactory.getLog ( WarWatcher.class );
        sm = StringManager.getManager ( WarWatcher.class );
    }
    protected static class WarFilter implements FilenameFilter {
        @Override
        public boolean accept ( final File path, final String name ) {
            return name != null && name.endsWith ( ".war" );
        }
    }
    protected static class WarInfo {
        protected final File war;
        protected long lastChecked;
        protected long lastState;
        public WarInfo ( final File war ) {
            this.lastChecked = 0L;
            this.lastState = 0L;
            this.war = war;
            this.lastChecked = war.lastModified();
            if ( !war.exists() ) {
                this.lastState = -1L;
            }
        }
        public boolean modified() {
            return this.war.exists() && this.war.lastModified() > this.lastChecked;
        }
        public boolean exists() {
            return this.war.exists();
        }
        public int check() {
            int result = 0;
            if ( this.modified() ) {
                result = 1;
                this.lastState = result;
            } else if ( !this.exists() && this.lastState != -1L ) {
                result = -1;
                this.lastState = result;
            } else if ( this.lastState == -1L && this.exists() ) {
                result = 1;
                this.lastState = result;
            }
            this.lastChecked = System.currentTimeMillis();
            return result;
        }
        public File getWar() {
            return this.war;
        }
        @Override
        public int hashCode() {
            return this.war.getAbsolutePath().hashCode();
        }
        @Override
        public boolean equals ( final Object other ) {
            if ( other instanceof WarInfo ) {
                final WarInfo wo = ( WarInfo ) other;
                return wo.getWar().equals ( this.getWar() );
            }
            return false;
        }
        protected void setLastState ( final int lastState ) {
            this.lastState = lastState;
        }
    }
}
