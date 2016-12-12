package org.apache.catalina.valves;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.TimeZone;
import java.util.Locale;
import java.text.SimpleDateFormat;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import javax.servlet.http.HttpSession;
import java.util.Iterator;
import javax.servlet.http.Cookie;
import org.apache.juli.logging.LogFactory;
import java.net.InetAddress;
import org.apache.catalina.connector.Response;
import org.apache.catalina.connector.Request;
import java.util.Date;
import java.io.CharArrayWriter;
import java.util.List;
import java.io.IOException;
import java.util.ArrayList;
import org.apache.catalina.util.ServerInfo;
import org.apache.tomcat.util.ExceptionUtils;
import org.apache.juli.logging.Log;
public class ExtendedAccessLogValve extends AccessLogValve {
    private static final Log log;
    protected static final String extendedAccessLogInfo = "org.apache.catalina.valves.ExtendedAccessLogValve/2.1";
    static String wrap ( final Object value ) {
        if ( value == null || "-".equals ( value ) ) {
            return "-";
        }
        String svalue;
        try {
            svalue = value.toString();
        } catch ( Throwable e ) {
            ExceptionUtils.handleThrowable ( e );
            return "-";
        }
        final StringBuilder buffer = new StringBuilder ( svalue.length() + 2 );
        buffer.append ( '\"' );
        int i = 0;
        while ( i < svalue.length() ) {
            final int j = svalue.indexOf ( 34, i );
            if ( j == -1 ) {
                buffer.append ( svalue.substring ( i ) );
                i = svalue.length();
            } else {
                buffer.append ( svalue.substring ( i, j + 1 ) );
                buffer.append ( '\"' );
                i = j + 1;
            }
        }
        buffer.append ( '\"' );
        return buffer.toString();
    }
    @Override
    protected synchronized void open() {
        super.open();
        if ( this.currentLogFile.length() == 0L ) {
            this.writer.println ( "#Fields: " + this.pattern );
            this.writer.println ( "#Version: 2.0" );
            this.writer.println ( "#Software: " + ServerInfo.getServerInfo() );
        }
    }
    @Override
    protected AccessLogElement[] createLogElements() {
        if ( ExtendedAccessLogValve.log.isDebugEnabled() ) {
            ExtendedAccessLogValve.log.debug ( "decodePattern, pattern =" + this.pattern );
        }
        final List<AccessLogElement> list = new ArrayList<AccessLogElement>();
        final PatternTokenizer tokenizer = new PatternTokenizer ( this.pattern );
        try {
            tokenizer.getWhiteSpaces();
            if ( tokenizer.isEnded() ) {
                ExtendedAccessLogValve.log.info ( "pattern was just empty or whitespace" );
                return null;
            }
            for ( String token = tokenizer.getToken(); token != null; token = tokenizer.getToken() ) {
                if ( ExtendedAccessLogValve.log.isDebugEnabled() ) {
                    ExtendedAccessLogValve.log.debug ( "token = " + token );
                }
                final AccessLogElement element = this.getLogElement ( token, tokenizer );
                if ( element == null ) {
                    break;
                }
                list.add ( element );
                final String whiteSpaces = tokenizer.getWhiteSpaces();
                if ( whiteSpaces.length() > 0 ) {
                    list.add ( new StringElement ( whiteSpaces ) );
                }
                if ( tokenizer.isEnded() ) {
                    break;
                }
            }
            if ( ExtendedAccessLogValve.log.isDebugEnabled() ) {
                ExtendedAccessLogValve.log.debug ( "finished decoding with element size of: " + list.size() );
            }
            return list.toArray ( new AccessLogElement[0] );
        } catch ( IOException e ) {
            ExtendedAccessLogValve.log.error ( "parse error", e );
            return null;
        }
    }
    protected AccessLogElement getLogElement ( final String token, final PatternTokenizer tokenizer ) throws IOException {
        if ( "date".equals ( token ) ) {
            return new DateElement();
        }
        if ( "time".equals ( token ) ) {
            if ( !tokenizer.hasSubToken() ) {
                return new TimeElement();
            }
            final String nextToken = tokenizer.getToken();
            if ( "taken".equals ( nextToken ) ) {
                return new ElapsedTimeElement ( false );
            }
        } else {
            if ( "bytes".equals ( token ) ) {
                return new ByteSentElement ( true );
            }
            if ( "cached".equals ( token ) ) {
                return new StringElement ( "-" );
            }
            if ( "c".equals ( token ) ) {
                final String nextToken = tokenizer.getToken();
                if ( "ip".equals ( nextToken ) ) {
                    return new RemoteAddrElement ( this );
                }
                if ( "dns".equals ( nextToken ) ) {
                    return new HostElement ( this );
                }
            } else if ( "s".equals ( token ) ) {
                final String nextToken = tokenizer.getToken();
                if ( "ip".equals ( nextToken ) ) {
                    return new LocalAddrElement();
                }
                if ( "dns".equals ( nextToken ) ) {
                    return new AccessLogElement() {
                        @Override
                        public void addElement ( final CharArrayWriter buf, final Date date, final Request request, final Response response, final long time ) {
                            String value;
                            try {
                                value = InetAddress.getLocalHost().getHostName();
                            } catch ( Throwable e ) {
                                ExceptionUtils.handleThrowable ( e );
                                value = "localhost";
                            }
                            buf.append ( value );
                        }
                    };
                }
            } else {
                if ( "cs".equals ( token ) ) {
                    return this.getClientToServerElement ( tokenizer );
                }
                if ( "sc".equals ( token ) ) {
                    return this.getServerToClientElement ( tokenizer );
                }
                if ( "sr".equals ( token ) || "rs".equals ( token ) ) {
                    return this.getProxyElement ( tokenizer );
                }
                if ( "x".equals ( token ) ) {
                    return this.getXParameterElement ( tokenizer );
                }
            }
        }
        ExtendedAccessLogValve.log.error ( "unable to decode with rest of chars starting: " + token );
        return null;
    }
    protected AccessLogElement getClientToServerElement ( final PatternTokenizer tokenizer ) throws IOException {
        if ( tokenizer.hasSubToken() ) {
            String token = tokenizer.getToken();
            if ( "method".equals ( token ) ) {
                return new MethodElement();
            }
            if ( "uri".equals ( token ) ) {
                if ( !tokenizer.hasSubToken() ) {
                    return new AccessLogElement() {
                        @Override
                        public void addElement ( final CharArrayWriter buf, final Date date, final Request request, final Response response, final long time ) {
                            final String query = request.getQueryString();
                            if ( query == null ) {
                                buf.append ( request.getRequestURI() );
                            } else {
                                buf.append ( request.getRequestURI() );
                                buf.append ( '?' );
                                buf.append ( request.getQueryString() );
                            }
                        }
                    };
                }
                token = tokenizer.getToken();
                if ( "stem".equals ( token ) ) {
                    return new RequestURIElement();
                }
                if ( "query".equals ( token ) ) {
                    return new AccessLogElement() {
                        @Override
                        public void addElement ( final CharArrayWriter buf, final Date date, final Request request, final Response response, final long time ) {
                            final String query = request.getQueryString();
                            if ( query != null ) {
                                buf.append ( query );
                            } else {
                                buf.append ( '-' );
                            }
                        }
                    };
                }
            }
        } else if ( tokenizer.hasParameter() ) {
            final String parameter = tokenizer.getParameter();
            if ( parameter == null ) {
                ExtendedAccessLogValve.log.error ( "No closing ) found for in decode" );
                return null;
            }
            return new RequestHeaderElement ( parameter );
        }
        ExtendedAccessLogValve.log.error ( "The next characters couldn't be decoded: " + tokenizer.getRemains() );
        return null;
    }
    protected AccessLogElement getServerToClientElement ( final PatternTokenizer tokenizer ) throws IOException {
        if ( tokenizer.hasSubToken() ) {
            final String token = tokenizer.getToken();
            if ( "status".equals ( token ) ) {
                return new HttpStatusCodeElement();
            }
            if ( "comment".equals ( token ) ) {
                return new StringElement ( "?" );
            }
        } else if ( tokenizer.hasParameter() ) {
            final String parameter = tokenizer.getParameter();
            if ( parameter == null ) {
                ExtendedAccessLogValve.log.error ( "No closing ) found for in decode" );
                return null;
            }
            return new ResponseHeaderElement ( parameter );
        }
        ExtendedAccessLogValve.log.error ( "The next characters couldn't be decoded: " + tokenizer.getRemains() );
        return null;
    }
    protected AccessLogElement getProxyElement ( final PatternTokenizer tokenizer ) throws IOException {
        final String token = null;
        if ( tokenizer.hasSubToken() ) {
            tokenizer.getToken();
            return new StringElement ( "-" );
        }
        if ( tokenizer.hasParameter() ) {
            tokenizer.getParameter();
            return new StringElement ( "-" );
        }
        ExtendedAccessLogValve.log.error ( "The next characters couldn't be decoded: " + token );
        return null;
    }
    protected AccessLogElement getXParameterElement ( final PatternTokenizer tokenizer ) throws IOException {
        if ( !tokenizer.hasSubToken() ) {
            ExtendedAccessLogValve.log.error ( "x param in wrong format. Needs to be 'x-#(...)' read the docs!" );
            return null;
        }
        final String token = tokenizer.getToken();
        if ( "threadname".equals ( token ) ) {
            return new ThreadNameElement();
        }
        if ( !tokenizer.hasParameter() ) {
            ExtendedAccessLogValve.log.error ( "x param in wrong format. Needs to be 'x-#(...)' read the docs!" );
            return null;
        }
        final String parameter = tokenizer.getParameter();
        if ( parameter == null ) {
            ExtendedAccessLogValve.log.error ( "No closing ) found for in decode" );
            return null;
        }
        if ( "A".equals ( token ) ) {
            return new ServletContextElement ( parameter );
        }
        if ( "C".equals ( token ) ) {
            return new CookieElement ( parameter );
        }
        if ( "R".equals ( token ) ) {
            return new RequestAttributeElement ( parameter );
        }
        if ( "S".equals ( token ) ) {
            return new SessionAttributeElement ( parameter );
        }
        if ( "H".equals ( token ) ) {
            return this.getServletRequestElement ( parameter );
        }
        if ( "P".equals ( token ) ) {
            return new RequestParameterElement ( parameter );
        }
        if ( "O".equals ( token ) ) {
            return new ResponseAllHeaderElement ( parameter );
        }
        ExtendedAccessLogValve.log.error ( "x param for servlet request, couldn't decode value: " + token );
        return null;
    }
    protected AccessLogElement getServletRequestElement ( final String parameter ) {
        if ( "authType".equals ( parameter ) ) {
            return new AccessLogElement() {
                @Override
                public void addElement ( final CharArrayWriter buf, final Date date, final Request request, final Response response, final long time ) {
                    buf.append ( ExtendedAccessLogValve.wrap ( request.getAuthType() ) );
                }
            };
        }
        if ( "remoteUser".equals ( parameter ) ) {
            return new AccessLogElement() {
                @Override
                public void addElement ( final CharArrayWriter buf, final Date date, final Request request, final Response response, final long time ) {
                    buf.append ( ExtendedAccessLogValve.wrap ( request.getRemoteUser() ) );
                }
            };
        }
        if ( "requestedSessionId".equals ( parameter ) ) {
            return new AccessLogElement() {
                @Override
                public void addElement ( final CharArrayWriter buf, final Date date, final Request request, final Response response, final long time ) {
                    buf.append ( ExtendedAccessLogValve.wrap ( request.getRequestedSessionId() ) );
                }
            };
        }
        if ( "requestedSessionIdFromCookie".equals ( parameter ) ) {
            return new AccessLogElement() {
                @Override
                public void addElement ( final CharArrayWriter buf, final Date date, final Request request, final Response response, final long time ) {
                    buf.append ( ExtendedAccessLogValve.wrap ( "" + request.isRequestedSessionIdFromCookie() ) );
                }
            };
        }
        if ( "requestedSessionIdValid".equals ( parameter ) ) {
            return new AccessLogElement() {
                @Override
                public void addElement ( final CharArrayWriter buf, final Date date, final Request request, final Response response, final long time ) {
                    buf.append ( ExtendedAccessLogValve.wrap ( "" + request.isRequestedSessionIdValid() ) );
                }
            };
        }
        if ( "contentLength".equals ( parameter ) ) {
            return new AccessLogElement() {
                @Override
                public void addElement ( final CharArrayWriter buf, final Date date, final Request request, final Response response, final long time ) {
                    buf.append ( ExtendedAccessLogValve.wrap ( "" + request.getContentLengthLong() ) );
                }
            };
        }
        if ( "characterEncoding".equals ( parameter ) ) {
            return new AccessLogElement() {
                @Override
                public void addElement ( final CharArrayWriter buf, final Date date, final Request request, final Response response, final long time ) {
                    buf.append ( ExtendedAccessLogValve.wrap ( request.getCharacterEncoding() ) );
                }
            };
        }
        if ( "locale".equals ( parameter ) ) {
            return new AccessLogElement() {
                @Override
                public void addElement ( final CharArrayWriter buf, final Date date, final Request request, final Response response, final long time ) {
                    buf.append ( ExtendedAccessLogValve.wrap ( request.getLocale() ) );
                }
            };
        }
        if ( "protocol".equals ( parameter ) ) {
            return new AccessLogElement() {
                @Override
                public void addElement ( final CharArrayWriter buf, final Date date, final Request request, final Response response, final long time ) {
                    buf.append ( ExtendedAccessLogValve.wrap ( request.getProtocol() ) );
                }
            };
        }
        if ( "scheme".equals ( parameter ) ) {
            return new AccessLogElement() {
                @Override
                public void addElement ( final CharArrayWriter buf, final Date date, final Request request, final Response response, final long time ) {
                    buf.append ( request.getScheme() );
                }
            };
        }
        if ( "secure".equals ( parameter ) ) {
            return new AccessLogElement() {
                @Override
                public void addElement ( final CharArrayWriter buf, final Date date, final Request request, final Response response, final long time ) {
                    buf.append ( ExtendedAccessLogValve.wrap ( "" + request.isSecure() ) );
                }
            };
        }
        ExtendedAccessLogValve.log.error ( "x param for servlet request, couldn't decode value: " + parameter );
        return null;
    }
    static {
        log = LogFactory.getLog ( ExtendedAccessLogValve.class );
    }
    protected static class DateElement implements AccessLogElement {
        private static final long INTERVAL = 86400000L;
        private static final ThreadLocal<ElementTimestampStruct> currentDate;
        @Override
        public void addElement ( final CharArrayWriter buf, final Date date, final Request request, final Response response, final long time ) {
            final ElementTimestampStruct eds = DateElement.currentDate.get();
            final long millis = eds.currentTimestamp.getTime();
            if ( date.getTime() > millis + 86400000L - 1L || date.getTime() < millis ) {
                eds.currentTimestamp.setTime ( date.getTime() - date.getTime() % 86400000L );
                eds.currentTimestampString = eds.currentTimestampFormat.format ( eds.currentTimestamp );
            }
            buf.append ( eds.currentTimestampString );
        }
        static {
            currentDate = new ThreadLocal<ElementTimestampStruct>() {
                @Override
                protected ElementTimestampStruct initialValue() {
                    return new ElementTimestampStruct ( "yyyy-MM-dd" );
                }
            };
        }
    }
    protected static class TimeElement implements AccessLogElement {
        private static final long INTERVAL = 1000L;
        private static final ThreadLocal<ElementTimestampStruct> currentTime;
        @Override
        public void addElement ( final CharArrayWriter buf, final Date date, final Request request, final Response response, final long time ) {
            final ElementTimestampStruct eds = TimeElement.currentTime.get();
            final long millis = eds.currentTimestamp.getTime();
            if ( date.getTime() > millis + 1000L - 1L || date.getTime() < millis ) {
                eds.currentTimestamp.setTime ( date.getTime() - date.getTime() % 1000L );
                eds.currentTimestampString = eds.currentTimestampFormat.format ( eds.currentTimestamp );
            }
            buf.append ( eds.currentTimestampString );
        }
        static {
            currentTime = new ThreadLocal<ElementTimestampStruct>() {
                @Override
                protected ElementTimestampStruct initialValue() {
                    return new ElementTimestampStruct ( "HH:mm:ss" );
                }
            };
        }
    }
    protected static class RequestHeaderElement implements AccessLogElement {
        private final String header;
        public RequestHeaderElement ( final String header ) {
            this.header = header;
        }
        @Override
        public void addElement ( final CharArrayWriter buf, final Date date, final Request request, final Response response, final long time ) {
            buf.append ( ExtendedAccessLogValve.wrap ( request.getHeader ( this.header ) ) );
        }
    }
    protected static class ResponseHeaderElement implements AccessLogElement {
        private final String header;
        public ResponseHeaderElement ( final String header ) {
            this.header = header;
        }
        @Override
        public void addElement ( final CharArrayWriter buf, final Date date, final Request request, final Response response, final long time ) {
            buf.append ( ExtendedAccessLogValve.wrap ( response.getHeader ( this.header ) ) );
        }
    }
    protected static class ServletContextElement implements AccessLogElement {
        private final String attribute;
        public ServletContextElement ( final String attribute ) {
            this.attribute = attribute;
        }
        @Override
        public void addElement ( final CharArrayWriter buf, final Date date, final Request request, final Response response, final long time ) {
            buf.append ( ExtendedAccessLogValve.wrap ( request.getContext().getServletContext().getAttribute ( this.attribute ) ) );
        }
    }
    protected static class CookieElement implements AccessLogElement {
        private final String name;
        public CookieElement ( final String name ) {
            this.name = name;
        }
        @Override
        public void addElement ( final CharArrayWriter buf, final Date date, final Request request, final Response response, final long time ) {
            final Cookie[] c = request.getCookies();
            for ( int i = 0; c != null && i < c.length; ++i ) {
                if ( this.name.equals ( c[i].getName() ) ) {
                    buf.append ( ExtendedAccessLogValve.wrap ( c[i].getValue() ) );
                }
            }
        }
    }
    protected static class ResponseAllHeaderElement implements AccessLogElement {
        private final String header;
        public ResponseAllHeaderElement ( final String header ) {
            this.header = header;
        }
        @Override
        public void addElement ( final CharArrayWriter buf, final Date date, final Request request, final Response response, final long time ) {
            if ( null != response ) {
                final Iterator<String> iter = response.getHeaders ( this.header ).iterator();
                if ( iter.hasNext() ) {
                    final StringBuilder buffer = new StringBuilder();
                    boolean first = true;
                    while ( iter.hasNext() ) {
                        if ( first ) {
                            first = false;
                        } else {
                            buffer.append ( "," );
                        }
                        buffer.append ( iter.next() );
                    }
                    buf.append ( ExtendedAccessLogValve.wrap ( buffer.toString() ) );
                }
                return;
            }
            buf.append ( "-" );
        }
    }
    protected static class RequestAttributeElement implements AccessLogElement {
        private final String attribute;
        public RequestAttributeElement ( final String attribute ) {
            this.attribute = attribute;
        }
        @Override
        public void addElement ( final CharArrayWriter buf, final Date date, final Request request, final Response response, final long time ) {
            buf.append ( ExtendedAccessLogValve.wrap ( request.getAttribute ( this.attribute ) ) );
        }
    }
    protected static class SessionAttributeElement implements AccessLogElement {
        private final String attribute;
        public SessionAttributeElement ( final String attribute ) {
            this.attribute = attribute;
        }
        @Override
        public void addElement ( final CharArrayWriter buf, final Date date, final Request request, final Response response, final long time ) {
            HttpSession session = null;
            if ( request != null ) {
                session = request.getSession ( false );
                if ( session != null ) {
                    buf.append ( ExtendedAccessLogValve.wrap ( session.getAttribute ( this.attribute ) ) );
                }
            }
        }
    }
    protected static class RequestParameterElement implements AccessLogElement {
        private final String parameter;
        public RequestParameterElement ( final String parameter ) {
            this.parameter = parameter;
        }
        private String urlEncode ( final String value ) {
            if ( null == value || value.length() == 0 ) {
                return null;
            }
            try {
                return URLEncoder.encode ( value, "UTF-8" );
            } catch ( UnsupportedEncodingException e ) {
                return null;
            }
        }
        @Override
        public void addElement ( final CharArrayWriter buf, final Date date, final Request request, final Response response, final long time ) {
            buf.append ( ExtendedAccessLogValve.wrap ( this.urlEncode ( request.getParameter ( this.parameter ) ) ) );
        }
    }
    protected static class PatternTokenizer {
        private final StringReader sr;
        private StringBuilder buf;
        private boolean ended;
        private boolean subToken;
        private boolean parameter;
        public PatternTokenizer ( final String str ) {
            this.buf = new StringBuilder();
            this.ended = false;
            this.sr = new StringReader ( str );
        }
        public boolean hasSubToken() {
            return this.subToken;
        }
        public boolean hasParameter() {
            return this.parameter;
        }
        public String getToken() throws IOException {
            if ( this.ended ) {
                return null;
            }
            String result = null;
            this.subToken = false;
            this.parameter = false;
            for ( int c = this.sr.read(); c != -1; c = this.sr.read() ) {
                switch ( c ) {
                case 32: {
                    result = this.buf.toString();
                    ( this.buf = new StringBuilder() ).append ( ( char ) c );
                    return result;
                }
                case 45: {
                    result = this.buf.toString();
                    this.buf = new StringBuilder();
                    this.subToken = true;
                    return result;
                }
                case 40: {
                    result = this.buf.toString();
                    this.buf = new StringBuilder();
                    this.parameter = true;
                    return result;
                }
                case 41: {
                    result = this.buf.toString();
                    this.buf = new StringBuilder();
                    break;
                }
                default: {
                    this.buf.append ( ( char ) c );
                    break;
                }
                }
            }
            this.ended = true;
            if ( this.buf.length() != 0 ) {
                return this.buf.toString();
            }
            return null;
        }
        public String getParameter() throws IOException {
            if ( !this.parameter ) {
                return null;
            }
            this.parameter = false;
            for ( int c = this.sr.read(); c != -1; c = this.sr.read() ) {
                if ( c == 41 ) {
                    final String result = this.buf.toString();
                    this.buf = new StringBuilder();
                    return result;
                }
                this.buf.append ( ( char ) c );
            }
            return null;
        }
        public String getWhiteSpaces() throws IOException {
            if ( this.isEnded() ) {
                return "";
            }
            final StringBuilder whiteSpaces = new StringBuilder();
            if ( this.buf.length() > 0 ) {
                whiteSpaces.append ( ( CharSequence ) this.buf );
                this.buf = new StringBuilder();
            }
            int c;
            for ( c = this.sr.read(); Character.isWhitespace ( ( char ) c ); c = this.sr.read() ) {
                whiteSpaces.append ( ( char ) c );
            }
            if ( c == -1 ) {
                this.ended = true;
            } else {
                this.buf.append ( ( char ) c );
            }
            return whiteSpaces.toString();
        }
        public boolean isEnded() {
            return this.ended;
        }
        public String getRemains() throws IOException {
            final StringBuilder remains = new StringBuilder();
            for ( int c = this.sr.read(); c != -1; c = this.sr.read() ) {
                remains.append ( ( char ) c );
            }
            return remains.toString();
        }
    }
    private static class ElementTimestampStruct {
        private final Date currentTimestamp;
        private final SimpleDateFormat currentTimestampFormat;
        private String currentTimestampString;
        ElementTimestampStruct ( final String format ) {
            this.currentTimestamp = new Date ( 0L );
            ( this.currentTimestampFormat = new SimpleDateFormat ( format, Locale.US ) ).setTimeZone ( TimeZone.getTimeZone ( "GMT" ) );
        }
    }
}
