package org.apache.tomcat.util.modeler;
import java.util.ArrayList;
import java.util.Iterator;
import javax.management.ListenerNotFoundException;
import javax.management.MBeanNotificationInfo;
import javax.management.Notification;
import javax.management.NotificationBroadcaster;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
public class BaseNotificationBroadcaster implements NotificationBroadcaster {
    protected ArrayList<BaseNotificationBroadcasterEntry> entries =
        new ArrayList<>();
    @Override
    public void addNotificationListener ( NotificationListener listener,
                                          NotificationFilter filter,
                                          Object handback )
    throws IllegalArgumentException {
        synchronized ( entries ) {
            if ( filter instanceof BaseAttributeFilter ) {
                BaseAttributeFilter newFilter = ( BaseAttributeFilter ) filter;
                Iterator<BaseNotificationBroadcasterEntry> items =
                    entries.iterator();
                while ( items.hasNext() ) {
                    BaseNotificationBroadcasterEntry item = items.next();
                    if ( ( item.listener == listener ) &&
                            ( item.filter != null ) &&
                            ( item.filter instanceof BaseAttributeFilter ) &&
                            ( item.handback == handback ) ) {
                        BaseAttributeFilter oldFilter =
                            ( BaseAttributeFilter ) item.filter;
                        String newNames[] = newFilter.getNames();
                        String oldNames[] = oldFilter.getNames();
                        if ( newNames.length == 0 ) {
                            oldFilter.clear();
                        } else {
                            if ( oldNames.length != 0 ) {
                                for ( int i = 0; i < newNames.length; i++ ) {
                                    oldFilter.addAttribute ( newNames[i] );
                                }
                            }
                        }
                        return;
                    }
                }
            }
            entries.add ( new BaseNotificationBroadcasterEntry
                          ( listener, filter, handback ) );
        }
    }
    @Override
    public MBeanNotificationInfo[] getNotificationInfo() {
        return ( new MBeanNotificationInfo[0] );
    }
    @Override
    public void removeNotificationListener ( NotificationListener listener )
    throws ListenerNotFoundException {
        synchronized ( entries ) {
            Iterator<BaseNotificationBroadcasterEntry> items =
                entries.iterator();
            while ( items.hasNext() ) {
                BaseNotificationBroadcasterEntry item = items.next();
                if ( item.listener == listener ) {
                    items.remove();
                }
            }
        }
    }
    public void sendNotification ( Notification notification ) {
        synchronized ( entries ) {
            Iterator<BaseNotificationBroadcasterEntry> items =
                entries.iterator();
            while ( items.hasNext() ) {
                BaseNotificationBroadcasterEntry item = items.next();
                if ( ( item.filter != null ) &&
                        ( !item.filter.isNotificationEnabled ( notification ) ) ) {
                    continue;
                }
                item.listener.handleNotification ( notification, item.handback );
            }
        }
    }
}
class BaseNotificationBroadcasterEntry {
    public BaseNotificationBroadcasterEntry ( NotificationListener listener,
            NotificationFilter filter,
            Object handback ) {
        this.listener = listener;
        this.filter = filter;
        this.handback = handback;
    }
    public NotificationFilter filter = null;
    public Object handback = null;
    public NotificationListener listener = null;
}
