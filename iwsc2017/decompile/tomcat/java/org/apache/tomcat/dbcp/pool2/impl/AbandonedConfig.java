package org.apache.tomcat.dbcp.pool2.impl;
import java.io.OutputStream;
import java.io.PrintWriter;
public class AbandonedConfig {
    private boolean removeAbandonedOnBorrow;
    private boolean removeAbandonedOnMaintenance;
    private int removeAbandonedTimeout;
    private boolean logAbandoned;
    private PrintWriter logWriter;
    private boolean useUsageTracking;
    public AbandonedConfig() {
        this.removeAbandonedOnBorrow = false;
        this.removeAbandonedOnMaintenance = false;
        this.removeAbandonedTimeout = 300;
        this.logAbandoned = false;
        this.logWriter = new PrintWriter ( System.out );
        this.useUsageTracking = false;
    }
    public boolean getRemoveAbandonedOnBorrow() {
        return this.removeAbandonedOnBorrow;
    }
    public void setRemoveAbandonedOnBorrow ( final boolean removeAbandonedOnBorrow ) {
        this.removeAbandonedOnBorrow = removeAbandonedOnBorrow;
    }
    public boolean getRemoveAbandonedOnMaintenance() {
        return this.removeAbandonedOnMaintenance;
    }
    public void setRemoveAbandonedOnMaintenance ( final boolean removeAbandonedOnMaintenance ) {
        this.removeAbandonedOnMaintenance = removeAbandonedOnMaintenance;
    }
    public int getRemoveAbandonedTimeout() {
        return this.removeAbandonedTimeout;
    }
    public void setRemoveAbandonedTimeout ( final int removeAbandonedTimeout ) {
        this.removeAbandonedTimeout = removeAbandonedTimeout;
    }
    public boolean getLogAbandoned() {
        return this.logAbandoned;
    }
    public void setLogAbandoned ( final boolean logAbandoned ) {
        this.logAbandoned = logAbandoned;
    }
    public PrintWriter getLogWriter() {
        return this.logWriter;
    }
    public void setLogWriter ( final PrintWriter logWriter ) {
        this.logWriter = logWriter;
    }
    public boolean getUseUsageTracking() {
        return this.useUsageTracking;
    }
    public void setUseUsageTracking ( final boolean useUsageTracking ) {
        this.useUsageTracking = useUsageTracking;
    }
    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append ( "AbandonedConfig [removeAbandonedOnBorrow=" );
        builder.append ( this.removeAbandonedOnBorrow );
        builder.append ( ", removeAbandonedOnMaintenance=" );
        builder.append ( this.removeAbandonedOnMaintenance );
        builder.append ( ", removeAbandonedTimeout=" );
        builder.append ( this.removeAbandonedTimeout );
        builder.append ( ", logAbandoned=" );
        builder.append ( this.logAbandoned );
        builder.append ( ", logWriter=" );
        builder.append ( this.logWriter );
        builder.append ( ", useUsageTracking=" );
        builder.append ( this.useUsageTracking );
        builder.append ( "]" );
        return builder.toString();
    }
}
