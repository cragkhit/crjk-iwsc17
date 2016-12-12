package org.apache.naming.factory;
import java.util.Hashtable;
import java.util.Enumeration;
import javax.mail.internet.MimePart;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.Session;
import javax.naming.RefAddr;
import java.util.Properties;
import javax.naming.Reference;
import javax.mail.internet.MimePartDataSource;
import java.security.PrivilegedAction;
class SendMailFactory$1 implements PrivilegedAction<MimePartDataSource> {
    final   Reference val$ref;
    @Override
    public MimePartDataSource run() {
        final Properties props = new Properties();
        final Enumeration<RefAddr> list = this.val$ref.getAll();
        ( ( Hashtable<String, String> ) props ).put ( "mail.transport.protocol", "smtp" );
        while ( list.hasMoreElements() ) {
            final RefAddr refaddr = list.nextElement();
            ( ( Hashtable<String, Object> ) props ).put ( refaddr.getType(), refaddr.getContent() );
        }
        final MimeMessage message = new MimeMessage ( Session.getInstance ( props ) );
        try {
            final RefAddr fromAddr = this.val$ref.get ( "mail.from" );
            String from = null;
            if ( fromAddr != null ) {
                from = ( String ) this.val$ref.get ( "mail.from" ).getContent();
            }
            if ( from != null ) {
                message.setFrom ( new InternetAddress ( from ) );
            }
            message.setSubject ( "" );
        } catch ( Exception ex ) {}
        final MimePartDataSource mds = new MimePartDataSource ( ( MimePart ) message );
        return mds;
    }
}
