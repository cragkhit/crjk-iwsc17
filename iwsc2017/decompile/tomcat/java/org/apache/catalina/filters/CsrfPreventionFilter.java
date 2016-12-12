package org.apache.catalina.filters;
import java.util.LinkedHashMap;
import java.util.Map;
import java.io.Serializable;
import javax.servlet.http.HttpServletResponseWrapper;
import javax.servlet.ServletException;
import java.io.IOException;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.FilterChain;
import javax.servlet.ServletResponse;
import javax.servlet.ServletRequest;
import java.util.HashSet;
import java.util.Set;
public class CsrfPreventionFilter extends CsrfPreventionFilterBase {
    private final Set<String> entryPoints;
    private int nonceCacheSize;
    public CsrfPreventionFilter() {
        this.entryPoints = new HashSet<String>();
        this.nonceCacheSize = 5;
    }
    public void setEntryPoints ( final String entryPoints ) {
        final String[] split;
        final String[] values = split = entryPoints.split ( "," );
        for ( final String value : split ) {
            this.entryPoints.add ( value.trim() );
        }
    }
    public void setNonceCacheSize ( final int nonceCacheSize ) {
        this.nonceCacheSize = nonceCacheSize;
    }
    public void doFilter ( final ServletRequest request, final ServletResponse response, final FilterChain chain ) throws IOException, ServletException {
        ServletResponse wResponse = null;
        if ( request instanceof HttpServletRequest && response instanceof HttpServletResponse ) {
            final HttpServletRequest req = ( HttpServletRequest ) request;
            final HttpServletResponse res = ( HttpServletResponse ) response;
            boolean skipNonceCheck = false;
            if ( "GET".equals ( req.getMethod() ) && this.entryPoints.contains ( this.getRequestedPath ( req ) ) ) {
                skipNonceCheck = true;
            }
            HttpSession session = req.getSession ( false );
            LruCache<String> nonceCache = ( LruCache<String> ) ( ( session == null ) ? null : ( ( LruCache ) session.getAttribute ( "org.apache.catalina.filters.CSRF_NONCE" ) ) );
            if ( !skipNonceCheck ) {
                final String previousNonce = req.getParameter ( "org.apache.catalina.filters.CSRF_NONCE" );
                if ( nonceCache == null || previousNonce == null || !nonceCache.contains ( previousNonce ) ) {
                    res.sendError ( this.getDenyStatus() );
                    return;
                }
            }
            if ( nonceCache == null ) {
                nonceCache = new LruCache<String> ( this.nonceCacheSize );
                if ( session == null ) {
                    session = req.getSession ( true );
                }
                session.setAttribute ( "org.apache.catalina.filters.CSRF_NONCE", ( Object ) nonceCache );
            }
            final String newNonce = this.generateNonce();
            nonceCache.add ( newNonce );
            wResponse = ( ServletResponse ) new CsrfResponseWrapper ( res, newNonce );
        } else {
            wResponse = response;
        }
        chain.doFilter ( request, wResponse );
    }
    protected static class CsrfResponseWrapper extends HttpServletResponseWrapper {
        private final String nonce;
        public CsrfResponseWrapper ( final HttpServletResponse response, final String nonce ) {
            super ( response );
            this.nonce = nonce;
        }
        @Deprecated
        public String encodeRedirectUrl ( final String url ) {
            return this.encodeRedirectURL ( url );
        }
        public String encodeRedirectURL ( final String url ) {
            return this.addNonce ( super.encodeRedirectURL ( url ) );
        }
        @Deprecated
        public String encodeUrl ( final String url ) {
            return this.encodeURL ( url );
        }
        public String encodeURL ( final String url ) {
            return this.addNonce ( super.encodeURL ( url ) );
        }
        private String addNonce ( final String url ) {
            if ( url == null || this.nonce == null ) {
                return url;
            }
            String path = url;
            String query = "";
            String anchor = "";
            final int pound = path.indexOf ( 35 );
            if ( pound >= 0 ) {
                anchor = path.substring ( pound );
                path = path.substring ( 0, pound );
            }
            final int question = path.indexOf ( 63 );
            if ( question >= 0 ) {
                query = path.substring ( question );
                path = path.substring ( 0, question );
            }
            final StringBuilder sb = new StringBuilder ( path );
            if ( query.length() > 0 ) {
                sb.append ( query );
                sb.append ( '&' );
            } else {
                sb.append ( '?' );
            }
            sb.append ( "org.apache.catalina.filters.CSRF_NONCE" );
            sb.append ( '=' );
            sb.append ( this.nonce );
            sb.append ( anchor );
            return sb.toString();
        }
    }
    protected static class LruCache<T> implements Serializable {
        private static final long serialVersionUID = 1L;
        private final Map<T, T> cache;
        public LruCache ( final int cacheSize ) {
            this.cache = new LinkedHashMap<T, T>() {
                private static final long serialVersionUID = 1L;
                @Override
                protected boolean removeEldestEntry ( final Map.Entry<T, T> eldest ) {
                    return this.size() > cacheSize;
                }
            };
        }
        public void add ( final T key ) {
            synchronized ( this.cache ) {
                this.cache.put ( key, null );
            }
        }
        public boolean contains ( final T key ) {
            synchronized ( this.cache ) {
                return this.cache.containsKey ( key );
            }
        }
    }
}
