package org.apache.tomcat.util.scan;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.apache.tomcat.JarScanFilter;
import org.apache.tomcat.JarScanType;
import org.apache.tomcat.util.file.Matcher;
public class StandardJarScanFilter implements JarScanFilter {
    private final ReadWriteLock configurationLock =
        new ReentrantReadWriteLock();
    private static final String defaultSkip;
    private static final String defaultScan;
    private static final Set<String> defaultSkipSet = new HashSet<>();
    private static final Set<String> defaultScanSet = new HashSet<>();
    static {
        defaultSkip = System.getProperty ( Constants.SKIP_JARS_PROPERTY );
        populateSetFromAttribute ( defaultSkip, defaultSkipSet );
        defaultScan = System.getProperty ( Constants.SCAN_JARS_PROPERTY );
        populateSetFromAttribute ( defaultScan, defaultScanSet );
    }
    private String tldSkip;
    private String tldScan;
    private final Set<String> tldSkipSet;
    private final Set<String> tldScanSet;
    private boolean defaultTldScan = true;
    private String pluggabilitySkip;
    private String pluggabilityScan;
    private final Set<String> pluggabilitySkipSet;
    private final Set<String> pluggabilityScanSet;
    private boolean defaultPluggabilityScan = true;
    public StandardJarScanFilter() {
        tldSkip = defaultSkip;
        tldSkipSet = new HashSet<> ( defaultSkipSet );
        tldScan = defaultScan;
        tldScanSet = new HashSet<> ( defaultScanSet );
        pluggabilitySkip = defaultSkip;
        pluggabilitySkipSet = new HashSet<> ( defaultSkipSet );
        pluggabilityScan = defaultScan;
        pluggabilityScanSet = new HashSet<> ( defaultScanSet );
    }
    public String getTldSkip() {
        return tldSkip;
    }
    public void setTldSkip ( String tldSkip ) {
        this.tldSkip = tldSkip;
        Lock writeLock = configurationLock.writeLock();
        writeLock.lock();
        try {
            populateSetFromAttribute ( tldSkip, tldSkipSet );
        } finally {
            writeLock.unlock();
        }
    }
    public String getTldScan() {
        return tldScan;
    }
    public void setTldScan ( String tldScan ) {
        this.tldScan = tldScan;
        Lock writeLock = configurationLock.writeLock();
        writeLock.lock();
        try {
            populateSetFromAttribute ( tldScan, tldScanSet );
        } finally {
            writeLock.unlock();
        }
    }
    public boolean isDefaultTldScan() {
        return defaultTldScan;
    }
    public void setDefaultTldScan ( boolean defaultTldScan ) {
        this.defaultTldScan = defaultTldScan;
    }
    public String getPluggabilitySkip() {
        return pluggabilitySkip;
    }
    public void setPluggabilitySkip ( String pluggabilitySkip ) {
        this.pluggabilitySkip = pluggabilitySkip;
        Lock writeLock = configurationLock.writeLock();
        writeLock.lock();
        try {
            populateSetFromAttribute ( pluggabilitySkip, pluggabilitySkipSet );
        } finally {
            writeLock.unlock();
        }
    }
    public String getPluggabilityScan() {
        return pluggabilityScan;
    }
    public void setPluggabilityScan ( String pluggabilityScan ) {
        this.pluggabilityScan = pluggabilityScan;
        Lock writeLock = configurationLock.writeLock();
        writeLock.lock();
        try {
            populateSetFromAttribute ( pluggabilityScan, pluggabilityScanSet );
        } finally {
            writeLock.unlock();
        }
    }
    public boolean isDefaultPluggabilityScan() {
        return defaultPluggabilityScan;
    }
    public void setDefaultPluggabilityScan ( boolean defaultPluggabilityScan ) {
        this.defaultPluggabilityScan = defaultPluggabilityScan;
    }
    @Override
    public boolean check ( JarScanType jarScanType, String jarName ) {
        Lock readLock = configurationLock.readLock();
        readLock.lock();
        try {
            final boolean defaultScan;
            final Set<String> toSkip;
            final Set<String> toScan;
            switch ( jarScanType ) {
            case TLD: {
                defaultScan = defaultTldScan;
                toSkip = tldSkipSet;
                toScan = tldScanSet;
                break;
            }
            case PLUGGABILITY: {
                defaultScan = defaultPluggabilityScan;
                toSkip = pluggabilitySkipSet;
                toScan = pluggabilityScanSet;
                break;
            }
            case OTHER:
            default: {
                defaultScan = true;
                toSkip = defaultSkipSet;
                toScan = defaultScanSet;
            }
            }
            if ( defaultScan ) {
                if ( Matcher.matchName ( toSkip, jarName ) ) {
                    if ( Matcher.matchName ( toScan, jarName ) ) {
                        return true;
                    } else {
                        return false;
                    }
                }
                return true;
            } else {
                if ( Matcher.matchName ( toScan, jarName ) ) {
                    if ( Matcher.matchName ( toSkip, jarName ) ) {
                        return false;
                    } else {
                        return true;
                    }
                }
                return false;
            }
        } finally {
            readLock.unlock();
        }
    }
    private static void populateSetFromAttribute ( String attribute, Set<String> set ) {
        set.clear();
        if ( attribute != null ) {
            StringTokenizer tokenizer = new StringTokenizer ( attribute, "," );
            while ( tokenizer.hasMoreElements() ) {
                String token = tokenizer.nextToken().trim();
                if ( token.length() > 0 ) {
                    set.add ( token );
                }
            }
        }
    }
}
