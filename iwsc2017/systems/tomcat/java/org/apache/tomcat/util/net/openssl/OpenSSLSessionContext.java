package org.apache.tomcat.util.net.openssl;
import java.util.Enumeration;
import java.util.NoSuchElementException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSessionContext;
import org.apache.tomcat.jni.SSL;
import org.apache.tomcat.jni.SSLContext;
import org.apache.tomcat.util.res.StringManager;
public class OpenSSLSessionContext implements SSLSessionContext {
    private static final StringManager sm = StringManager.getManager ( OpenSSLSessionContext.class );
    private static final Enumeration<byte[]> EMPTY = new EmptyEnumeration();
    private final OpenSSLSessionStats stats;
    private final long context;
    OpenSSLSessionContext ( long context ) {
        this.context = context;
        stats = new OpenSSLSessionStats ( context );
    }
    @Override
    public SSLSession getSession ( byte[] bytes ) {
        return null;
    }
    @Override
    public Enumeration<byte[]> getIds() {
        return EMPTY;
    }
    public void setTicketKeys ( byte[] keys ) {
        if ( keys == null ) {
            throw new IllegalArgumentException ( sm.getString ( "sessionContext.nullTicketKeys" ) );
        }
        SSLContext.setSessionTicketKeys ( context, keys );
    }
    public void setSessionCacheEnabled ( boolean enabled ) {
        long mode = enabled ? SSL.SSL_SESS_CACHE_SERVER : SSL.SSL_SESS_CACHE_OFF;
        SSLContext.setSessionCacheMode ( context, mode );
    }
    public boolean isSessionCacheEnabled() {
        return SSLContext.getSessionCacheMode ( context ) == SSL.SSL_SESS_CACHE_SERVER;
    }
    public OpenSSLSessionStats stats() {
        return stats;
    }
    @Override
    public void setSessionTimeout ( int seconds ) {
        if ( seconds < 0 ) {
            throw new IllegalArgumentException();
        }
        SSLContext.setSessionCacheTimeout ( context, seconds );
    }
    @Override
    public int getSessionTimeout() {
        return ( int ) SSLContext.getSessionCacheTimeout ( context );
    }
    @Override
    public void setSessionCacheSize ( int size ) {
        if ( size < 0 ) {
            throw new IllegalArgumentException();
        }
        SSLContext.setSessionCacheSize ( context, size );
    }
    @Override
    public int getSessionCacheSize() {
        return ( int ) SSLContext.getSessionCacheSize ( context );
    }
    public boolean setSessionIdContext ( byte[] sidCtx ) {
        return SSLContext.setSessionIdContext ( context, sidCtx );
    }
    private static final class EmptyEnumeration implements Enumeration<byte[]> {
        @Override
        public boolean hasMoreElements() {
            return false;
        }
        @Override
        public byte[] nextElement() {
            throw new NoSuchElementException();
        }
    }
}
