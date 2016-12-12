package org.apache.naming.factory;
import java.util.Hashtable;
import java.util.Enumeration;
import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;
import javax.naming.RefAddr;
import java.util.Properties;
import javax.naming.Reference;
import javax.mail.Session;
import java.security.PrivilegedAction;
class MailSessionFactory$1 implements PrivilegedAction<Session> {
    final   Reference val$ref;
    @Override
    public Session run() {
        final Properties props = new Properties();
        ( ( Hashtable<String, String> ) props ).put ( "mail.transport.protocol", "smtp" );
        ( ( Hashtable<String, String> ) props ).put ( "mail.smtp.host", "localhost" );
        String password = null;
        final Enumeration<RefAddr> attrs = this.val$ref.getAll();
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
}
