package org.apache.tomcat.util.modeler;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReadWriteLock;
import javax.management.MBeanNotificationInfo;
public class NotificationInfo extends FeatureInfo {
    static final long serialVersionUID = -6319885418912650856L;
    transient MBeanNotificationInfo info;
    protected String[] notifTypes;
    protected final ReadWriteLock notifTypesLock;
    public NotificationInfo() {
        this.info = null;
        this.notifTypes = new String[0];
        this.notifTypesLock = new ReentrantReadWriteLock();
    }
    @Override
    public void setDescription ( final String description ) {
        super.setDescription ( description );
        this.info = null;
    }
    @Override
    public void setName ( final String name ) {
        super.setName ( name );
        this.info = null;
    }
    public String[] getNotifTypes() {
        final Lock readLock = this.notifTypesLock.readLock();
        readLock.lock();
        try {
            return this.notifTypes;
        } finally {
            readLock.unlock();
        }
    }
    public void addNotifType ( final String notifType ) {
        final Lock writeLock = this.notifTypesLock.writeLock();
        writeLock.lock();
        try {
            final String[] results = new String[this.notifTypes.length + 1];
            System.arraycopy ( this.notifTypes, 0, results, 0, this.notifTypes.length );
            results[this.notifTypes.length] = notifType;
            this.notifTypes = results;
            this.info = null;
        } finally {
            writeLock.unlock();
        }
    }
    public MBeanNotificationInfo createNotificationInfo() {
        if ( this.info != null ) {
            return this.info;
        }
        return this.info = new MBeanNotificationInfo ( this.getNotifTypes(), this.getName(), this.getDescription() );
    }
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder ( "NotificationInfo[" );
        sb.append ( "name=" );
        sb.append ( this.name );
        sb.append ( ", description=" );
        sb.append ( this.description );
        sb.append ( ", notifTypes=" );
        final Lock readLock = this.notifTypesLock.readLock();
        readLock.lock();
        try {
            sb.append ( this.notifTypes.length );
        } finally {
            readLock.unlock();
        }
        sb.append ( "]" );
        return sb.toString();
    }
}
