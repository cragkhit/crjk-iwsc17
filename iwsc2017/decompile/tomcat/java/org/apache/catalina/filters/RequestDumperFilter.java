package org.apache.catalina.filters;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.apache.juli.logging.LogFactory;
import javax.servlet.ServletException;
import java.io.IOException;
import java.util.Iterator;
import java.util.Enumeration;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.FilterChain;
import javax.servlet.ServletResponse;
import javax.servlet.ServletRequest;
import org.apache.juli.logging.Log;
import javax.servlet.GenericFilter;
public class RequestDumperFilter extends GenericFilter {
    private static final long serialVersionUID = 1L;
    private static final String NON_HTTP_REQ_MSG = "Not available. Non-http request.";
    private static final String NON_HTTP_RES_MSG = "Not available. Non-http response.";
    private static final ThreadLocal<Timestamp> timestamp;
    private static final Log log;
    public void doFilter ( final ServletRequest request, final ServletResponse response, final FilterChain chain ) throws IOException, ServletException {
        HttpServletRequest hRequest = null;
        HttpServletResponse hResponse = null;
        if ( request instanceof HttpServletRequest ) {
            hRequest = ( HttpServletRequest ) request;
        }
        if ( response instanceof HttpServletResponse ) {
            hResponse = ( HttpServletResponse ) response;
        }
        this.doLog ( "START TIME        ", this.getTimestamp() );
        if ( hRequest == null ) {
            this.doLog ( "        requestURI", "Not available. Non-http request." );
            this.doLog ( "          authType", "Not available. Non-http request." );
        } else {
            this.doLog ( "        requestURI", hRequest.getRequestURI() );
            this.doLog ( "          authType", hRequest.getAuthType() );
        }
        this.doLog ( " characterEncoding", request.getCharacterEncoding() );
        this.doLog ( "     contentLength", Long.toString ( request.getContentLengthLong() ) );
        this.doLog ( "       contentType", request.getContentType() );
        if ( hRequest == null ) {
            this.doLog ( "       contextPath", "Not available. Non-http request." );
            this.doLog ( "            cookie", "Not available. Non-http request." );
            this.doLog ( "            header", "Not available. Non-http request." );
        } else {
            this.doLog ( "       contextPath", hRequest.getContextPath() );
            final Cookie[] cookies = hRequest.getCookies();
            if ( cookies != null ) {
                for ( int i = 0; i < cookies.length; ++i ) {
                    this.doLog ( "            cookie", cookies[i].getName() + "=" + cookies[i].getValue() );
                }
            }
            final Enumeration<String> hnames = ( Enumeration<String> ) hRequest.getHeaderNames();
            while ( hnames.hasMoreElements() ) {
                final String hname = hnames.nextElement();
                final Enumeration<String> hvalues = ( Enumeration<String> ) hRequest.getHeaders ( hname );
                while ( hvalues.hasMoreElements() ) {
                    final String hvalue = hvalues.nextElement();
                    this.doLog ( "            header", hname + "=" + hvalue );
                }
            }
        }
        this.doLog ( "            locale", request.getLocale().toString() );
        if ( hRequest == null ) {
            this.doLog ( "            method", "Not available. Non-http request." );
        } else {
            this.doLog ( "            method", hRequest.getMethod() );
        }
        final Enumeration<String> pnames = ( Enumeration<String> ) request.getParameterNames();
        while ( pnames.hasMoreElements() ) {
            final String pname = pnames.nextElement();
            final String[] pvalues = request.getParameterValues ( pname );
            final StringBuilder result = new StringBuilder ( pname );
            result.append ( '=' );
            for ( int j = 0; j < pvalues.length; ++j ) {
                if ( j > 0 ) {
                    result.append ( ", " );
                }
                result.append ( pvalues[j] );
            }
            this.doLog ( "         parameter", result.toString() );
        }
        if ( hRequest == null ) {
            this.doLog ( "          pathInfo", "Not available. Non-http request." );
        } else {
            this.doLog ( "          pathInfo", hRequest.getPathInfo() );
        }
        this.doLog ( "          protocol", request.getProtocol() );
        if ( hRequest == null ) {
            this.doLog ( "       queryString", "Not available. Non-http request." );
        } else {
            this.doLog ( "       queryString", hRequest.getQueryString() );
        }
        this.doLog ( "        remoteAddr", request.getRemoteAddr() );
        this.doLog ( "        remoteHost", request.getRemoteHost() );
        if ( hRequest == null ) {
            this.doLog ( "        remoteUser", "Not available. Non-http request." );
            this.doLog ( "requestedSessionId", "Not available. Non-http request." );
        } else {
            this.doLog ( "        remoteUser", hRequest.getRemoteUser() );
            this.doLog ( "requestedSessionId", hRequest.getRequestedSessionId() );
        }
        this.doLog ( "            scheme", request.getScheme() );
        this.doLog ( "        serverName", request.getServerName() );
        this.doLog ( "        serverPort", Integer.toString ( request.getServerPort() ) );
        if ( hRequest == null ) {
            this.doLog ( "       servletPath", "Not available. Non-http request." );
        } else {
            this.doLog ( "       servletPath", hRequest.getServletPath() );
        }
        this.doLog ( "          isSecure", Boolean.valueOf ( request.isSecure() ).toString() );
        this.doLog ( "------------------", "--------------------------------------------" );
        chain.doFilter ( request, response );
        this.doLog ( "------------------", "--------------------------------------------" );
        if ( hRequest == null ) {
            this.doLog ( "          authType", "Not available. Non-http request." );
        } else {
            this.doLog ( "          authType", hRequest.getAuthType() );
        }
        this.doLog ( "       contentType", response.getContentType() );
        if ( hResponse == null ) {
            this.doLog ( "            header", "Not available. Non-http response." );
        } else {
            final Iterable<String> rhnames = ( Iterable<String> ) hResponse.getHeaderNames();
            for ( final String rhname : rhnames ) {
                final Iterable<String> rhvalues = ( Iterable<String> ) hResponse.getHeaders ( rhname );
                for ( final String rhvalue : rhvalues ) {
                    this.doLog ( "            header", rhname + "=" + rhvalue );
                }
            }
        }
        if ( hRequest == null ) {
            this.doLog ( "        remoteUser", "Not available. Non-http request." );
        } else {
            this.doLog ( "        remoteUser", hRequest.getRemoteUser() );
        }
        if ( hResponse == null ) {
            this.doLog ( "        remoteUser", "Not available. Non-http response." );
        } else {
            this.doLog ( "            status", Integer.toString ( hResponse.getStatus() ) );
        }
        this.doLog ( "END TIME          ", this.getTimestamp() );
        this.doLog ( "==================", "============================================" );
    }
    private void doLog ( final String attribute, final String value ) {
        final StringBuilder sb = new StringBuilder ( 80 );
        sb.append ( Thread.currentThread().getName() );
        sb.append ( ' ' );
        sb.append ( attribute );
        sb.append ( '=' );
        sb.append ( value );
        RequestDumperFilter.log.info ( sb.toString() );
    }
    private String getTimestamp() {
        final Timestamp ts = RequestDumperFilter.timestamp.get();
        final long currentTime = System.currentTimeMillis();
        if ( ts.date.getTime() + 999L < currentTime ) {
            ts.date.setTime ( currentTime - currentTime % 1000L );
            ts.update();
        }
        return ts.dateString;
    }
    static {
        timestamp = new ThreadLocal<Timestamp>() {
            @Override
            protected Timestamp initialValue() {
                return new Timestamp();
            }
        };
        log = LogFactory.getLog ( RequestDumperFilter.class );
    }
    private static final class Timestamp {
        private final Date date;
        private final SimpleDateFormat format;
        private String dateString;
        private Timestamp() {
            this.date = new Date ( 0L );
            this.format = new SimpleDateFormat ( "dd-MMM-yyyy HH:mm:ss" );
            this.dateString = this.format.format ( this.date );
        }
        private void update() {
            this.dateString = this.format.format ( this.date );
        }
    }
}
