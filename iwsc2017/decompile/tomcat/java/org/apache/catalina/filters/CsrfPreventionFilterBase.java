package org.apache.catalina.filters;
import org.apache.juli.logging.LogFactory;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.ServletException;
import javax.servlet.FilterConfig;
import java.security.SecureRandom;
import java.util.Random;
import org.apache.juli.logging.Log;
public abstract class CsrfPreventionFilterBase extends FilterBase {
    private static final Log log;
    private String randomClass;
    private Random randomSource;
    private int denyStatus;
    public CsrfPreventionFilterBase() {
        this.randomClass = SecureRandom.class.getName();
        this.denyStatus = 403;
    }
    @Override
    protected Log getLogger() {
        return CsrfPreventionFilterBase.log;
    }
    public int getDenyStatus() {
        return this.denyStatus;
    }
    public void setDenyStatus ( final int denyStatus ) {
        this.denyStatus = denyStatus;
    }
    public void setRandomClass ( final String randomClass ) {
        this.randomClass = randomClass;
    }
    @Override
    public void init ( final FilterConfig filterConfig ) throws ServletException {
        super.init ( filterConfig );
        try {
            final Class<?> clazz = Class.forName ( this.randomClass );
            this.randomSource = ( Random ) clazz.newInstance();
        } catch ( ClassNotFoundException | InstantiationException | IllegalAccessException e ) {
            final ServletException se = new ServletException ( CsrfPreventionFilterBase.sm.getString ( "csrfPrevention.invalidRandomClass", this.randomClass ), ( Throwable ) e );
            throw se;
        }
    }
    @Override
    protected boolean isConfigProblemFatal() {
        return true;
    }
    protected String generateNonce() {
        final byte[] random = new byte[16];
        final StringBuilder buffer = new StringBuilder();
        this.randomSource.nextBytes ( random );
        for ( int j = 0; j < random.length; ++j ) {
            final byte b1 = ( byte ) ( ( random[j] & 0xF0 ) >> 4 );
            final byte b2 = ( byte ) ( random[j] & 0xF );
            if ( b1 < 10 ) {
                buffer.append ( ( char ) ( 48 + b1 ) );
            } else {
                buffer.append ( ( char ) ( 65 + ( b1 - 10 ) ) );
            }
            if ( b2 < 10 ) {
                buffer.append ( ( char ) ( 48 + b2 ) );
            } else {
                buffer.append ( ( char ) ( 65 + ( b2 - 10 ) ) );
            }
        }
        return buffer.toString();
    }
    protected String getRequestedPath ( final HttpServletRequest request ) {
        String path = request.getServletPath();
        if ( request.getPathInfo() != null ) {
            path += request.getPathInfo();
        }
        return path;
    }
    static {
        log = LogFactory.getLog ( CsrfPreventionFilterBase.class );
    }
}
