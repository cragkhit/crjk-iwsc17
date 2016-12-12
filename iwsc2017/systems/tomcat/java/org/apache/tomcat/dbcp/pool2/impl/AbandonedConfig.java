package org.apache.tomcat.dbcp.pool2.impl;
import java.io.PrintWriter;
public class AbandonedConfig {
    private boolean removeAbandonedOnBorrow = false;
    public boolean getRemoveAbandonedOnBorrow() {
        return this.removeAbandonedOnBorrow;
    }
    public void setRemoveAbandonedOnBorrow ( final boolean removeAbandonedOnBorrow ) {
        this.removeAbandonedOnBorrow = removeAbandonedOnBorrow;
    }
    private boolean removeAbandonedOnMaintenance = false;
    public boolean getRemoveAbandonedOnMaintenance() {
        return this.removeAbandonedOnMaintenance;
    }
    public void setRemoveAbandonedOnMaintenance ( final boolean removeAbandonedOnMaintenance ) {
        this.removeAbandonedOnMaintenance = removeAbandonedOnMaintenance;
    }
    private int removeAbandonedTimeout = 300;
    public int getRemoveAbandonedTimeout() {
        return this.removeAbandonedTimeout;
    }
    public void setRemoveAbandonedTimeout ( final int removeAbandonedTimeout ) {
        this.removeAbandonedTimeout = removeAbandonedTimeout;
    }
    private boolean logAbandoned = false;
    public boolean getLogAbandoned() {
        return this.logAbandoned;
    }
    public void setLogAbandoned ( final boolean logAbandoned ) {
        this.logAbandoned = logAbandoned;
    }
    private PrintWriter logWriter = new PrintWriter ( System.out );
    public PrintWriter getLogWriter() {
        return logWriter;
    }
    public void setLogWriter ( final PrintWriter logWriter ) {
        this.logWriter = logWriter;
    }
    private boolean useUsageTracking = false;
    public boolean getUseUsageTracking() {
        return useUsageTracking;
    }
    public void setUseUsageTracking ( final boolean useUsageTracking ) {
        this.useUsageTracking = useUsageTracking;
    }
    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append ( "AbandonedConfig [removeAbandonedOnBorrow=" );
        builder.append ( removeAbandonedOnBorrow );
        builder.append ( ", removeAbandonedOnMaintenance=" );
        builder.append ( removeAbandonedOnMaintenance );
        builder.append ( ", removeAbandonedTimeout=" );
        builder.append ( removeAbandonedTimeout );
        builder.append ( ", logAbandoned=" );
        builder.append ( logAbandoned );
        builder.append ( ", logWriter=" );
        builder.append ( logWriter );
        builder.append ( ", useUsageTracking=" );
        builder.append ( useUsageTracking );
        builder.append ( "]" );
        return builder.toString();
    }
}
