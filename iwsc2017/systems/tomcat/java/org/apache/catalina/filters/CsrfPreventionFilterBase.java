package org.apache.catalina.filters;
import java.security.SecureRandom;
import java.util.Random;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
public abstract class CsrfPreventionFilterBase extends FilterBase {
    private static final Log log = LogFactory.getLog ( CsrfPreventionFilterBase.class );
    private String randomClass = SecureRandom.class.getName();
    private Random randomSource;
    private int denyStatus = HttpServletResponse.SC_FORBIDDEN;
    @Override
    protected Log getLogger() {
        return log;
    }
    public int getDenyStatus() {
        return denyStatus;
    }
    public void setDenyStatus ( int denyStatus ) {
        this.denyStatus = denyStatus;
    }
    public void setRandomClass ( String randomClass ) {
        this.randomClass = randomClass;
    }
    @Override
    public void init ( FilterConfig filterConfig ) throws ServletException {
        super.init ( filterConfig );
        try {
            Class<?> clazz = Class.forName ( randomClass );
            randomSource = ( Random ) clazz.newInstance();
        } catch ( ClassNotFoundException | InstantiationException | IllegalAccessException e ) {
            ServletException se = new ServletException ( sm.getString (
                        "csrfPrevention.invalidRandomClass", randomClass ), e );
            throw se;
        }
    }
    @Override
    protected boolean isConfigProblemFatal() {
        return true;
    }
    protected String generateNonce() {
        byte random[] = new byte[16];
        StringBuilder buffer = new StringBuilder();
        randomSource.nextBytes ( random );
        for ( int j = 0; j < random.length; j++ ) {
            byte b1 = ( byte ) ( ( random[j] & 0xf0 ) >> 4 );
            byte b2 = ( byte ) ( random[j] & 0x0f );
            if ( b1 < 10 ) {
                buffer.append ( ( char ) ( '0' + b1 ) );
            } else {
                buffer.append ( ( char ) ( 'A' + ( b1 - 10 ) ) );
            }
            if ( b2 < 10 ) {
                buffer.append ( ( char ) ( '0' + b2 ) );
            } else {
                buffer.append ( ( char ) ( 'A' + ( b2 - 10 ) ) );
            }
        }
        return buffer.toString();
    }
    protected String getRequestedPath ( HttpServletRequest request ) {
        String path = request.getServletPath();
        if ( request.getPathInfo() != null ) {
            path = path + request.getPathInfo();
        }
        return path;
    }
}
