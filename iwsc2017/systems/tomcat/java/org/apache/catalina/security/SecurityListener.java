package org.apache.catalina.security;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;
import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleListener;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.util.res.StringManager;
public class SecurityListener implements LifecycleListener {
    private static final Log log = LogFactory.getLog ( SecurityListener.class );
    private static final StringManager sm =
        StringManager.getManager ( Constants.PACKAGE );
    private static final String UMASK_PROPERTY_NAME =
        Constants.PACKAGE + ".SecurityListener.UMASK";
    private static final String UMASK_FORMAT = "%04o";
    private final Set<String> checkedOsUsers = new HashSet<>();
    private Integer minimumUmask = Integer.valueOf ( 7 );
    public SecurityListener() {
        checkedOsUsers.add ( "root" );
    }
    @Override
    public void lifecycleEvent ( LifecycleEvent event ) {
        if ( event.getType().equals ( Lifecycle.BEFORE_INIT_EVENT ) ) {
            doChecks();
        }
    }
    public void setCheckedOsUsers ( String userNameList ) {
        if ( userNameList == null || userNameList.length() == 0 ) {
            checkedOsUsers.clear();
        } else {
            String[] userNames = userNameList.split ( "," );
            for ( String userName : userNames ) {
                if ( userName.length() > 0 ) {
                    checkedOsUsers.add ( userName.toLowerCase ( Locale.getDefault() ) );
                }
            }
        }
    }
    public String getCheckedOsUsers() {
        if ( checkedOsUsers.size() == 0 ) {
            return "";
        }
        StringBuilder result = new StringBuilder();
        Iterator<String> iter = checkedOsUsers.iterator();
        result.append ( iter.next() );
        while ( iter.hasNext() ) {
            result.append ( ',' );
            result.append ( iter.next() );
        }
        return result.toString();
    }
    public void setMinimumUmask ( String umask ) {
        if ( umask == null || umask.length() == 0 ) {
            minimumUmask = Integer.valueOf ( 0 );
        } else {
            minimumUmask = Integer.valueOf ( umask, 8 );
        }
    }
    public String getMinimumUmask() {
        return String.format ( UMASK_FORMAT, minimumUmask );
    }
    protected void doChecks() {
        checkOsUser();
        checkUmask();
    }
    protected void checkOsUser() {
        String userName = System.getProperty ( "user.name" );
        if ( userName != null ) {
            String userNameLC = userName.toLowerCase ( Locale.getDefault() );
            if ( checkedOsUsers.contains ( userNameLC ) ) {
                throw new Error ( sm.getString (
                                      "SecurityListener.checkUserWarning", userName ) );
            }
        }
    }
    protected void checkUmask() {
        String prop = System.getProperty ( UMASK_PROPERTY_NAME );
        Integer umask = null;
        if ( prop != null ) {
            try {
                umask = Integer.valueOf ( prop, 8 );
            } catch ( NumberFormatException nfe ) {
                log.warn ( sm.getString ( "SecurityListener.checkUmaskParseFail",
                                          prop ) );
            }
        }
        if ( umask == null ) {
            if ( Constants.CRLF.equals ( System.lineSeparator() ) ) {
                if ( log.isDebugEnabled() ) {
                    log.debug ( sm.getString ( "SecurityListener.checkUmaskSkip" ) );
                }
                return;
            } else {
                if ( minimumUmask.intValue() > 0 ) {
                    log.warn ( sm.getString (
                                   "SecurityListener.checkUmaskNone",
                                   UMASK_PROPERTY_NAME, getMinimumUmask() ) );
                }
                return;
            }
        }
        if ( ( umask.intValue() & minimumUmask.intValue() ) !=
                minimumUmask.intValue() ) {
            throw new Error ( sm.getString ( "SecurityListener.checkUmaskFail",
                                             String.format ( UMASK_FORMAT, umask ), getMinimumUmask() ) );
        }
    }
}
