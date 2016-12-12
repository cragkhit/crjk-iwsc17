package org.apache.tomcat.util.modeler;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import javax.management.MBeanNotificationInfo;
public class NotificationInfo extends FeatureInfo {
    static final long serialVersionUID = -6319885418912650856L;
    transient MBeanNotificationInfo info = null;
    protected String notifTypes[] = new String[0];
    protected final ReadWriteLock notifTypesLock = new ReentrantReadWriteLock();
    @Override
    public void setDescription ( String description ) {
        super.setDescription ( description );
        this.info = null;
    }
    @Override
    public void setName ( String name ) {
        super.setName ( name );
        this.info = null;
    }
    public String[] getNotifTypes() {
        Lock readLock = notifTypesLock.readLock();
        readLock.lock();
        try {
            return this.notifTypes;
        } finally {
            readLock.unlock();
        }
    }
    public void addNotifType ( String notifType ) {
        Lock writeLock = notifTypesLock.writeLock();
        writeLock.lock();
        try {
            String results[] = new String[notifTypes.length + 1];
            System.arraycopy ( notifTypes, 0, results, 0, notifTypes.length );
            results[notifTypes.length] = notifType;
            notifTypes = results;
            this.info = null;
        } finally {
            writeLock.unlock();
        }
    }
    public MBeanNotificationInfo createNotificationInfo() {
        if ( info != null ) {
            return info;
        }
        info = new MBeanNotificationInfo
        ( getNotifTypes(), getName(), getDescription() );
        return info;
    }
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder ( "NotificationInfo[" );
        sb.append ( "name=" );
        sb.append ( name );
        sb.append ( ", description=" );
        sb.append ( description );
        sb.append ( ", notifTypes=" );
        Lock readLock = notifTypesLock.readLock();
        readLock.lock();
        try {
            sb.append ( notifTypes.length );
        } finally {
            readLock.unlock();
        }
        sb.append ( "]" );
        return ( sb.toString() );
    }
}
