package org.apache.catalina.ha.deploy;
import java.io.File;
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
