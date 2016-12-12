package org.apache.tomcat.util.modeler;
import java.util.HashSet;
import javax.management.AttributeChangeNotification;
import javax.management.Notification;
import javax.management.NotificationFilter;
public class BaseAttributeFilter implements NotificationFilter {
    private static final long serialVersionUID = 1L;
    public BaseAttributeFilter ( String name ) {
        super();
        if ( name != null ) {
            addAttribute ( name );
        }
    }
    private HashSet<String> names = new HashSet<>();
    public void addAttribute ( String name ) {
        synchronized ( names ) {
            names.add ( name );
        }
    }
    public void clear() {
        synchronized ( names ) {
            names.clear();
        }
    }
    public String[] getNames() {
        synchronized ( names ) {
            return names.toArray ( new String[names.size()] );
        }
    }
    @Override
    public boolean isNotificationEnabled ( Notification notification ) {
        if ( notification == null ) {
            return false;
        }
        if ( ! ( notification instanceof AttributeChangeNotification ) ) {
            return false;
        }
        AttributeChangeNotification acn =
            ( AttributeChangeNotification ) notification;
        if ( !AttributeChangeNotification.ATTRIBUTE_CHANGE.equals ( acn.getType() ) ) {
            return false;
        }
        synchronized ( names ) {
            if ( names.size() < 1 ) {
                return true;
            } else {
                return ( names.contains ( acn.getAttributeName() ) );
            }
        }
    }
    public void removeAttribute ( String name ) {
        synchronized ( names ) {
            names.remove ( name );
        }
    }
}
