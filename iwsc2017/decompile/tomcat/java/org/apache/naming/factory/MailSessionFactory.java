package org.apache.naming.factory;
import java.security.AccessController;
import java.util.Enumeration;
import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;
import javax.naming.RefAddr;
import java.util.Properties;
import javax.mail.Session;
import java.security.PrivilegedAction;
import javax.naming.Reference;
import java.util.Hashtable;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.spi.ObjectFactory;
public class MailSessionFactory implements ObjectFactory {
    protected static final String factoryType = "javax.mail.Session";
    @Override
    public Object getObjectInstance ( final Object refObj, final Name name, final Context context, final Hashtable<?, ?> env ) throws Exception {
        final Reference ref = ( Reference ) refObj;
        if ( !ref.getClassName().equals ( "javax.mail.Session" ) ) {
            return null;
        }
        return AccessController.doPrivileged ( ( PrivilegedAction<Object> ) new PrivilegedAction<Session>() {
            @Override
            public Session run() {
                final Properties props = new Properties();
                ( ( Hashtable<String, String> ) props ).put ( "mail.transport.protocol", "smtp" );
                ( ( Hashtable<String, String> ) props ).put ( "mail.smtp.host", "localhost" );
                String password = null;
                final Enumeration<RefAddr> attrs = ref.getAll();
                while ( attrs.hasMoreElements() ) {
                    final RefAddr attr = attrs.nextElement();
                    if ( "factory".equals ( attr.getType() ) ) {
                        continue;
                    }
                    if ( "password".equals ( attr.getType() ) ) {
                        password = ( String ) attr.getContent();
                    } else {
                        ( ( Hashtable<String, Object> ) props ).put ( attr.getType(), attr.getContent() );
                    }
                }
                Authenticator auth = null;
                if ( password != null ) {
                    String user = props.getProperty ( "mail.smtp.user" );
                    if ( user == null ) {
                        user = props.getProperty ( "mail.user" );
                    }
                    if ( user != null ) {
                        final PasswordAuthentication pa = new PasswordAuthentication ( user, password );
                        auth = new Authenticator() {
                            protected PasswordAuthentication getPasswordAuthentication() {
                                return pa;
                            }
                        };
                    }
                }
                final Session session = Session.getInstance ( props, auth );
                return session;
            }
        } );
    }
}
