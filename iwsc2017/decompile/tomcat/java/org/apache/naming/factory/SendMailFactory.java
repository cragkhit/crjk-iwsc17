package org.apache.naming.factory;
import java.security.AccessController;
import java.util.Enumeration;
import javax.mail.internet.MimePart;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.Session;
import javax.naming.RefAddr;
import java.util.Properties;
import javax.mail.internet.MimePartDataSource;
import java.security.PrivilegedAction;
import javax.naming.Reference;
import java.util.Hashtable;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.spi.ObjectFactory;
public class SendMailFactory implements ObjectFactory {
    protected static final String DataSourceClassName = "javax.mail.internet.MimePartDataSource";
    @Override
    public Object getObjectInstance ( final Object refObj, final Name name, final Context ctx, final Hashtable<?, ?> env ) throws Exception {
        final Reference ref = ( Reference ) refObj;
        if ( ref.getClassName().equals ( "javax.mail.internet.MimePartDataSource" ) ) {
            return AccessController.doPrivileged ( ( PrivilegedAction<Object> ) new PrivilegedAction<MimePartDataSource>() {
                @Override
                public MimePartDataSource run() {
                    final Properties props = new Properties();
                    final Enumeration<RefAddr> list = ref.getAll();
                    ( ( Hashtable<String, String> ) props ).put ( "mail.transport.protocol", "smtp" );
                    while ( list.hasMoreElements() ) {
                        final RefAddr refaddr = list.nextElement();
                        ( ( Hashtable<String, Object> ) props ).put ( refaddr.getType(), refaddr.getContent() );
                    }
                    final MimeMessage message = new MimeMessage ( Session.getInstance ( props ) );
                    try {
                        final RefAddr fromAddr = ref.get ( "mail.from" );
                        String from = null;
                        if ( fromAddr != null ) {
                            from = ( String ) ref.get ( "mail.from" ).getContent();
                        }
                        if ( from != null ) {
                            message.setFrom ( new InternetAddress ( from ) );
                        }
                        message.setSubject ( "" );
                    } catch ( Exception ex ) {}
                    final MimePartDataSource mds = new MimePartDataSource ( ( MimePart ) message );
                    return mds;
                }
            } );
        }
        return null;
    }
}
