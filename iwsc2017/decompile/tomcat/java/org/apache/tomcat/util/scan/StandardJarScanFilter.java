package org.apache.tomcat.util.scan;
import java.util.StringTokenizer;
import org.apache.tomcat.util.file.Matcher;
import org.apache.tomcat.JarScanType;
import java.util.concurrent.locks.Lock;
import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import org.apache.tomcat.JarScanFilter;
public class StandardJarScanFilter implements JarScanFilter {
    private final ReadWriteLock configurationLock;
    private static final String defaultSkip;
    private static final String defaultScan;
    private static final Set<String> defaultSkipSet;
    private static final Set<String> defaultScanSet;
    private String tldSkip;
    private String tldScan;
    private final Set<String> tldSkipSet;
    private final Set<String> tldScanSet;
    private boolean defaultTldScan;
    private String pluggabilitySkip;
    private String pluggabilityScan;
    private final Set<String> pluggabilitySkipSet;
    private final Set<String> pluggabilityScanSet;
    private boolean defaultPluggabilityScan;
    public StandardJarScanFilter() {
        this.configurationLock = new ReentrantReadWriteLock();
        this.defaultTldScan = true;
        this.defaultPluggabilityScan = true;
        this.tldSkip = StandardJarScanFilter.defaultSkip;
        this.tldSkipSet = new HashSet<String> ( StandardJarScanFilter.defaultSkipSet );
        this.tldScan = StandardJarScanFilter.defaultScan;
        this.tldScanSet = new HashSet<String> ( StandardJarScanFilter.defaultScanSet );
        this.pluggabilitySkip = StandardJarScanFilter.defaultSkip;
        this.pluggabilitySkipSet = new HashSet<String> ( StandardJarScanFilter.defaultSkipSet );
        this.pluggabilityScan = StandardJarScanFilter.defaultScan;
        this.pluggabilityScanSet = new HashSet<String> ( StandardJarScanFilter.defaultScanSet );
    }
    public String getTldSkip() {
        return this.tldSkip;
    }
    public void setTldSkip ( final String tldSkip ) {
        this.tldSkip = tldSkip;
        final Lock writeLock = this.configurationLock.writeLock();
        writeLock.lock();
        try {
            populateSetFromAttribute ( tldSkip, this.tldSkipSet );
        } finally {
            writeLock.unlock();
        }
    }
    public String getTldScan() {
        return this.tldScan;
    }
    public void setTldScan ( final String tldScan ) {
        this.tldScan = tldScan;
        final Lock writeLock = this.configurationLock.writeLock();
        writeLock.lock();
        try {
            populateSetFromAttribute ( tldScan, this.tldScanSet );
        } finally {
            writeLock.unlock();
        }
    }
    public boolean isDefaultTldScan() {
        return this.defaultTldScan;
    }
    public void setDefaultTldScan ( final boolean defaultTldScan ) {
        this.defaultTldScan = defaultTldScan;
    }
    public String getPluggabilitySkip() {
        return this.pluggabilitySkip;
    }
    public void setPluggabilitySkip ( final String pluggabilitySkip ) {
        this.pluggabilitySkip = pluggabilitySkip;
        final Lock writeLock = this.configurationLock.writeLock();
        writeLock.lock();
        try {
            populateSetFromAttribute ( pluggabilitySkip, this.pluggabilitySkipSet );
        } finally {
            writeLock.unlock();
        }
    }
    public String getPluggabilityScan() {
        return this.pluggabilityScan;
    }
    public void setPluggabilityScan ( final String pluggabilityScan ) {
        this.pluggabilityScan = pluggabilityScan;
        final Lock writeLock = this.configurationLock.writeLock();
        writeLock.lock();
        try {
            populateSetFromAttribute ( pluggabilityScan, this.pluggabilityScanSet );
        } finally {
            writeLock.unlock();
        }
    }
    public boolean isDefaultPluggabilityScan() {
        return this.defaultPluggabilityScan;
    }
    public void setDefaultPluggabilityScan ( final boolean defaultPluggabilityScan ) {
        this.defaultPluggabilityScan = defaultPluggabilityScan;
    }
    @Override
    public boolean check ( final JarScanType jarScanType, final String jarName ) {
        final Lock readLock = this.configurationLock.readLock();
        readLock.lock();
        try {
            boolean defaultScan = false;
            Set<String> toSkip = null;
            Set<String> toScan = null;
            switch ( jarScanType ) {
            case TLD: {
                defaultScan = this.defaultTldScan;
                toSkip = this.tldSkipSet;
                toScan = this.tldScanSet;
                break;
            }
            case PLUGGABILITY: {
                defaultScan = this.defaultPluggabilityScan;
                toSkip = this.pluggabilitySkipSet;
                toScan = this.pluggabilityScanSet;
                break;
            }
            default: {
                defaultScan = true;
                toSkip = StandardJarScanFilter.defaultSkipSet;
                toScan = StandardJarScanFilter.defaultScanSet;
                break;
            }
            }
            if ( defaultScan ) {
                return !Matcher.matchName ( toSkip, jarName ) || Matcher.matchName ( toScan, jarName );
            }
            return Matcher.matchName ( toScan, jarName ) && !Matcher.matchName ( toSkip, jarName );
        } finally {
            readLock.unlock();
        }
    }
    private static void populateSetFromAttribute ( final String attribute, final Set<String> set ) {
        set.clear();
        if ( attribute != null ) {
            final StringTokenizer tokenizer = new StringTokenizer ( attribute, "," );
            while ( tokenizer.hasMoreElements() ) {
                final String token = tokenizer.nextToken().trim();
                if ( token.length() > 0 ) {
                    set.add ( token );
                }
            }
        }
    }
    static {
        defaultSkipSet = new HashSet<String>();
        defaultScanSet = new HashSet<String>();
        populateSetFromAttribute ( defaultSkip = System.getProperty ( "tomcat.util.scan.StandardJarScanFilter.jarsToSkip" ), StandardJarScanFilter.defaultSkipSet );
        populateSetFromAttribute ( defaultScan = System.getProperty ( "tomcat.util.scan.StandardJarScanFilter.jarsToScan" ), StandardJarScanFilter.defaultScanSet );
    }
}
