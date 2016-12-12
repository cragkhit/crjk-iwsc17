package org.apache.catalina.valves;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import javax.servlet.http.HttpSession;
import java.util.Iterator;
import javax.servlet.http.Cookie;
import java.util.Enumeration;
import org.apache.catalina.Session;
import org.apache.tomcat.util.ExceptionUtils;
import java.net.InetAddress;
import org.apache.coyote.RequestInfo;
import java.util.TimeZone;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import org.apache.juli.logging.LogFactory;
import java.util.List;
import java.util.ArrayList;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.LifecycleState;
import javax.servlet.ServletException;
import java.io.IOException;
import org.apache.catalina.connector.Response;
import org.apache.catalina.connector.Request;
import java.io.CharArrayWriter;
import org.apache.tomcat.util.collections.SynchronizedStack;
import java.util.Locale;
import java.util.Date;
import org.apache.juli.logging.Log;
import org.apache.catalina.AccessLog;
public abstract class AbstractAccessLogValve extends ValveBase implements AccessLog {
    private static final Log log;
    protected boolean enabled;
    protected String pattern;
    private static final int globalCacheSize = 300;
    private static final int localCacheSize = 60;
    private static final DateFormatCache globalDateCache;
    private static final ThreadLocal<DateFormatCache> localDateCache;
    private static final ThreadLocal<Date> localDate;
    protected String condition;
    protected String conditionIf;
    protected String localeName;
    protected Locale locale;
    protected AccessLogElement[] logElements;
    protected boolean requestAttributesEnabled;
    private SynchronizedStack<CharArrayWriter> charArrayWriters;
    private int maxLogMessageBufferSize;
    public AbstractAccessLogValve() {
        super ( true );
        this.enabled = true;
        this.pattern = null;
        this.condition = null;
        this.conditionIf = null;
        this.localeName = Locale.getDefault().toString();
        this.locale = Locale.getDefault();
        this.logElements = null;
        this.requestAttributesEnabled = false;
        this.charArrayWriters = new SynchronizedStack<CharArrayWriter>();
        this.maxLogMessageBufferSize = 256;
    }
    @Override
    public void setRequestAttributesEnabled ( final boolean requestAttributesEnabled ) {
        this.requestAttributesEnabled = requestAttributesEnabled;
    }
    @Override
    public boolean getRequestAttributesEnabled() {
        return this.requestAttributesEnabled;
    }
    public boolean getEnabled() {
        return this.enabled;
    }
    public void setEnabled ( final boolean enabled ) {
        this.enabled = enabled;
    }
    public String getPattern() {
        return this.pattern;
    }
    public void setPattern ( final String pattern ) {
        if ( pattern == null ) {
            this.pattern = "";
        } else if ( pattern.equals ( "common" ) ) {
            this.pattern = "%h %l %u %t \"%r\" %s %b";
        } else if ( pattern.equals ( "combined" ) ) {
            this.pattern = "%h %l %u %t \"%r\" %s %b \"%{Referer}i\" \"%{User-Agent}i\"";
        } else {
            this.pattern = pattern;
        }
        this.logElements = this.createLogElements();
    }
    public String getCondition() {
        return this.condition;
    }
    public void setCondition ( final String condition ) {
        this.condition = condition;
    }
    public String getConditionUnless() {
        return this.getCondition();
    }
    public void setConditionUnless ( final String condition ) {
        this.setCondition ( condition );
    }
    public String getConditionIf() {
        return this.conditionIf;
    }
    public void setConditionIf ( final String condition ) {
        this.conditionIf = condition;
    }
    public String getLocale() {
        return this.localeName;
    }
    public void setLocale ( final String localeName ) {
        this.localeName = localeName;
        this.locale = findLocale ( localeName, this.locale );
    }
    @Override
    public void invoke ( final Request request, final Response response ) throws IOException, ServletException {
        this.getNext().invoke ( request, response );
    }
    @Override
    public void log ( final Request request, final Response response, final long time ) {
        if ( !this.getState().isAvailable() || !this.getEnabled() || this.logElements == null || ( this.condition != null && null != request.getRequest().getAttribute ( this.condition ) ) || ( this.conditionIf != null && null == request.getRequest().getAttribute ( this.conditionIf ) ) ) {
            return;
        }
        final long start = request.getCoyoteRequest().getStartTime();
        final Date date = getDate ( start + time );
        CharArrayWriter result = this.charArrayWriters.pop();
        if ( result == null ) {
            result = new CharArrayWriter ( 128 );
        }
        for ( int i = 0; i < this.logElements.length; ++i ) {
            this.logElements[i].addElement ( result, date, request, response, time );
        }
        this.log ( result );
        if ( result.size() <= this.maxLogMessageBufferSize ) {
            result.reset();
            this.charArrayWriters.push ( result );
        }
    }
    protected abstract void log ( final CharArrayWriter p0 );
    private static Date getDate ( final long systime ) {
        final Date date = AbstractAccessLogValve.localDate.get();
        date.setTime ( systime );
        return date;
    }
    protected static Locale findLocale ( final String name, final Locale fallback ) {
        if ( name == null || name.isEmpty() ) {
            return Locale.getDefault();
        }
        for ( final Locale l : Locale.getAvailableLocales() ) {
            if ( name.equals ( l.toString() ) ) {
                return l;
            }
        }
        AbstractAccessLogValve.log.error ( AbstractAccessLogValve.sm.getString ( "accessLogValve.invalidLocale", name ) );
        return fallback;
    }
    @Override
    protected synchronized void startInternal() throws LifecycleException {
        this.setState ( LifecycleState.STARTING );
    }
    @Override
    protected synchronized void stopInternal() throws LifecycleException {
        this.setState ( LifecycleState.STOPPING );
    }
    protected AccessLogElement[] createLogElements() {
        final List<AccessLogElement> list = new ArrayList<AccessLogElement>();
        boolean replace = false;
        StringBuilder buf = new StringBuilder();
        for ( int i = 0; i < this.pattern.length(); ++i ) {
            final char ch = this.pattern.charAt ( i );
            if ( replace ) {
                if ( '{' == ch ) {
                    final StringBuilder name = new StringBuilder();
                    int j;
                    for ( j = i + 1; j < this.pattern.length() && '}' != this.pattern.charAt ( j ); ++j ) {
                        name.append ( this.pattern.charAt ( j ) );
                    }
                    if ( j + 1 < this.pattern.length() ) {
                        ++j;
                        list.add ( this.createAccessLogElement ( name.toString(), this.pattern.charAt ( j ) ) );
                        i = j;
                    } else {
                        list.add ( this.createAccessLogElement ( ch ) );
                    }
                } else {
                    list.add ( this.createAccessLogElement ( ch ) );
                }
                replace = false;
            } else if ( ch == '%' ) {
                replace = true;
                list.add ( new StringElement ( buf.toString() ) );
                buf = new StringBuilder();
            } else {
                buf.append ( ch );
            }
        }
        if ( buf.length() > 0 ) {
            list.add ( new StringElement ( buf.toString() ) );
        }
        return list.toArray ( new AccessLogElement[0] );
    }
    protected AccessLogElement createAccessLogElement ( final String name, final char pattern ) {
        switch ( pattern ) {
        case 'i': {
            return new HeaderElement ( name );
        }
        case 'c': {
            return new CookieElement ( name );
        }
        case 'o': {
            return new ResponseHeaderElement ( name );
        }
        case 'p': {
            return new PortElement ( name );
        }
        case 'r': {
            return new RequestAttributeElement ( name );
        }
        case 's': {
            return new SessionAttributeElement ( name );
        }
        case 't': {
            return new DateAndTimeElement ( name );
        }
        default: {
            return new StringElement ( "???" );
        }
        }
    }
    protected AccessLogElement createAccessLogElement ( final char pattern ) {
        switch ( pattern ) {
        case 'a': {
            return new RemoteAddrElement();
        }
        case 'A': {
            return new LocalAddrElement();
        }
        case 'b': {
            return new ByteSentElement ( true );
        }
        case 'B': {
            return new ByteSentElement ( false );
        }
        case 'D': {
            return new ElapsedTimeElement ( true );
        }
        case 'F': {
            return new FirstByteTimeElement();
        }
        case 'h': {
            return new HostElement();
        }
        case 'H': {
            return new ProtocolElement();
        }
        case 'l': {
            return new LogicalUserNameElement();
        }
        case 'm': {
            return new MethodElement();
        }
        case 'p': {
            return new PortElement();
        }
        case 'q': {
            return new QueryElement();
        }
        case 'r': {
            return new RequestElement();
        }
        case 's': {
            return new HttpStatusCodeElement();
        }
        case 'S': {
            return new SessionIdElement();
        }
        case 't': {
            return new DateAndTimeElement();
        }
        case 'T': {
            return new ElapsedTimeElement ( false );
        }
        case 'u': {
            return new UserElement();
        }
        case 'U': {
            return new RequestURIElement();
        }
        case 'v': {
            return new LocalServerNameElement();
        }
        case 'I': {
            return new ThreadNameElement();
        }
        default: {
            return new StringElement ( "???" + pattern + "???" );
        }
        }
    }
    static {
        log = LogFactory.getLog ( AbstractAccessLogValve.class );
        globalDateCache = new DateFormatCache ( 300, Locale.getDefault(), null );
        localDateCache = new ThreadLocal<DateFormatCache>() {
            @Override
            protected DateFormatCache initialValue() {
                return new DateFormatCache ( 60, Locale.getDefault(), AbstractAccessLogValve.globalDateCache );
            }
        };
        localDate = new ThreadLocal<Date>() {
            @Override
            protected Date initialValue() {
                return new Date();
            }
        };
    }
    private enum FormatType {
        CLF,
        SEC,
        MSEC,
        MSEC_FRAC,
        SDF;
    }
    private enum PortType {
        LOCAL,
        REMOTE;
    }
    protected static class DateFormatCache {
        private int cacheSize;
        private final Locale cacheDefaultLocale;
        private final DateFormatCache parent;
        protected final Cache cLFCache;
        private final HashMap<String, Cache> formatCache;
        protected DateFormatCache ( final int size, final Locale loc, final DateFormatCache parent ) {
            this.cacheSize = 0;
            this.formatCache = new HashMap<String, Cache>();
            this.cacheSize = size;
            this.cacheDefaultLocale = loc;
            this.parent = parent;
            Cache parentCache = null;
            if ( parent != null ) {
                synchronized ( parent ) {
                    parentCache = parent.getCache ( null, null );
                }
            }
            this.cLFCache = new Cache ( parentCache );
        }
        private Cache getCache ( final String format, final Locale loc ) {
            Cache cache;
            if ( format == null ) {
                cache = this.cLFCache;
            } else {
                cache = this.formatCache.get ( format );
                if ( cache == null ) {
                    Cache parentCache = null;
                    if ( this.parent != null ) {
                        synchronized ( this.parent ) {
                            parentCache = this.parent.getCache ( format, loc );
                        }
                    }
                    cache = new Cache ( format, loc, parentCache );
                    this.formatCache.put ( format, cache );
                }
            }
            return cache;
        }
        public String getFormat ( final long time ) {
            return this.cLFCache.getFormatInternal ( time );
        }
        public String getFormat ( final String format, final Locale loc, final long time ) {
            return this.getCache ( format, loc ).getFormatInternal ( time );
        }
        protected class Cache {
            private static final String cLFFormat = "dd/MMM/yyyy:HH:mm:ss Z";
            private long previousSeconds;
            private String previousFormat;
            private long first;
            private long last;
            private int offset;
            private final Date currentDate;
            protected final String[] cache;
            private SimpleDateFormat formatter;
            private boolean isCLF;
            private Cache parent;
            private Cache ( final DateFormatCache this$0, final Cache parent ) {
                this ( this$0, null, parent );
            }
            private Cache ( final DateFormatCache this$0, final String format, final Cache parent ) {
                this ( this$0, format, null, parent );
            }
            private Cache ( String format, Locale loc, final Cache parent ) {
                this.previousSeconds = Long.MIN_VALUE;
                this.previousFormat = "";
                this.first = Long.MIN_VALUE;
                this.last = Long.MIN_VALUE;
                this.offset = 0;
                this.currentDate = new Date();
                this.isCLF = false;
                this.parent = null;
                this.cache = new String[DateFormatCache.this.cacheSize];
                for ( int i = 0; i < DateFormatCache.this.cacheSize; ++i ) {
                    this.cache[i] = null;
                }
                if ( loc == null ) {
                    loc = DateFormatCache.this.cacheDefaultLocale;
                }
                if ( format == null ) {
                    this.isCLF = true;
                    format = "dd/MMM/yyyy:HH:mm:ss Z";
                    this.formatter = new SimpleDateFormat ( format, Locale.US );
                } else {
                    this.formatter = new SimpleDateFormat ( format, loc );
                }
                this.formatter.setTimeZone ( TimeZone.getDefault() );
                this.parent = parent;
            }
            private String getFormatInternal ( final long time ) {
                final long seconds = time / 1000L;
                if ( seconds == this.previousSeconds ) {
                    return this.previousFormat;
                }
                this.previousSeconds = seconds;
                int index = ( this.offset + ( int ) ( seconds - this.first ) ) % DateFormatCache.this.cacheSize;
                if ( index < 0 ) {
                    index += DateFormatCache.this.cacheSize;
                }
                if ( seconds >= this.first && seconds <= this.last ) {
                    if ( this.cache[index] != null ) {
                        return this.previousFormat = this.cache[index];
                    }
                } else if ( seconds >= this.last + DateFormatCache.this.cacheSize || seconds <= this.first - DateFormatCache.this.cacheSize ) {
                    this.first = seconds;
                    this.last = this.first + DateFormatCache.this.cacheSize - 1L;
                    index = 0;
                    this.offset = 0;
                    for ( int i = 1; i < DateFormatCache.this.cacheSize; ++i ) {
                        this.cache[i] = null;
                    }
                } else if ( seconds > this.last ) {
                    for ( int i = 1; i < seconds - this.last; ++i ) {
                        this.cache[ ( index + DateFormatCache.this.cacheSize - i ) % DateFormatCache.this.cacheSize] = null;
                    }
                    this.first = seconds - ( DateFormatCache.this.cacheSize - 1 );
                    this.last = seconds;
                    this.offset = ( index + 1 ) % DateFormatCache.this.cacheSize;
                } else if ( seconds < this.first ) {
                    for ( int i = 1; i < this.first - seconds; ++i ) {
                        this.cache[ ( index + i ) % DateFormatCache.this.cacheSize] = null;
                    }
                    this.first = seconds;
                    this.last = seconds + ( DateFormatCache.this.cacheSize - 1 );
                    this.offset = index;
                }
                if ( this.parent != null ) {
                    synchronized ( this.parent ) {
                        this.previousFormat = this.parent.getFormatInternal ( time );
                    }
                } else {
                    this.currentDate.setTime ( time );
                    this.previousFormat = this.formatter.format ( this.currentDate );
                    if ( this.isCLF ) {
                        final StringBuilder current = new StringBuilder ( 32 );
                        current.append ( '[' );
                        current.append ( this.previousFormat );
                        current.append ( ']' );
                        this.previousFormat = current.toString();
                    }
                }
                this.cache[index] = this.previousFormat;
                return this.previousFormat;
            }
        }
    }
    protected static class ThreadNameElement implements AccessLogElement {
        @Override
        public void addElement ( final CharArrayWriter buf, final Date date, final Request request, final Response response, final long time ) {
            final RequestInfo info = request.getCoyoteRequest().getRequestProcessor();
            if ( info != null ) {
                buf.append ( info.getWorkerThreadName() );
            } else {
                buf.append ( "-" );
            }
        }
    }
    protected static class LocalAddrElement implements AccessLogElement {
        private static final String LOCAL_ADDR_VALUE;
        @Override
        public void addElement ( final CharArrayWriter buf, final Date date, final Request request, final Response response, final long time ) {
            buf.append ( LocalAddrElement.LOCAL_ADDR_VALUE );
        }
        static {
            String init;
            try {
                init = InetAddress.getLocalHost().getHostAddress();
            } catch ( Throwable e ) {
                ExceptionUtils.handleThrowable ( e );
                init = "127.0.0.1";
            }
            LOCAL_ADDR_VALUE = init;
        }
    }
    protected class RemoteAddrElement implements AccessLogElement {
        @Override
        public void addElement ( final CharArrayWriter buf, final Date date, final Request request, final Response response, final long time ) {
            if ( AbstractAccessLogValve.this.requestAttributesEnabled ) {
                final Object addr = request.getAttribute ( "org.apache.catalina.AccessLog.RemoteAddr" );
                if ( addr == null ) {
                    buf.append ( request.getRemoteAddr() );
                } else {
                    buf.append ( addr.toString() );
                }
            } else {
                buf.append ( request.getRemoteAddr() );
            }
        }
    }
    protected class HostElement implements AccessLogElement {
        @Override
        public void addElement ( final CharArrayWriter buf, final Date date, final Request request, final Response response, final long time ) {
            String value = null;
            if ( AbstractAccessLogValve.this.requestAttributesEnabled ) {
                final Object host = request.getAttribute ( "org.apache.catalina.AccessLog.RemoteHost" );
                if ( host != null ) {
                    value = host.toString();
                }
            }
            if ( value == null || value.length() == 0 ) {
                value = request.getRemoteHost();
            }
            if ( value == null || value.length() == 0 ) {
                value = "-";
            }
            buf.append ( value );
        }
    }
    protected static class LogicalUserNameElement implements AccessLogElement {
        @Override
        public void addElement ( final CharArrayWriter buf, final Date date, final Request request, final Response response, final long time ) {
            buf.append ( '-' );
        }
    }
    protected class ProtocolElement implements AccessLogElement {
        @Override
        public void addElement ( final CharArrayWriter buf, final Date date, final Request request, final Response response, final long time ) {
            if ( AbstractAccessLogValve.this.requestAttributesEnabled ) {
                final Object proto = request.getAttribute ( "org.apache.catalina.AccessLog.Protocol" );
                if ( proto == null ) {
                    buf.append ( request.getProtocol() );
                } else {
                    buf.append ( proto.toString() );
                }
            } else {
                buf.append ( request.getProtocol() );
            }
        }
    }
    protected static class UserElement implements AccessLogElement {
        @Override
        public void addElement ( final CharArrayWriter buf, final Date date, final Request request, final Response response, final long time ) {
            if ( request != null ) {
                final String value = request.getRemoteUser();
                if ( value != null ) {
                    buf.append ( value );
                } else {
                    buf.append ( '-' );
                }
            } else {
                buf.append ( '-' );
            }
        }
    }
    protected class DateAndTimeElement implements AccessLogElement {
        private static final String requestStartPrefix = "begin";
        private static final String responseEndPrefix = "end";
        private static final String prefixSeparator = ":";
        private static final String secFormat = "sec";
        private static final String msecFormat = "msec";
        private static final String msecFractionFormat = "msec_frac";
        private static final String msecPattern = "{#}";
        private static final String trippleMsecPattern = "{#}{#}{#}";
        private final String format;
        private final boolean usesBegin;
        private final FormatType type;
        private boolean usesMsecs;
        protected DateAndTimeElement ( final AbstractAccessLogValve this$0 ) {
            this ( this$0, null );
        }
        private String tidyFormat ( final String format ) {
            boolean escape = false;
            final StringBuilder result = new StringBuilder();
            for ( int len = format.length(), i = 0; i < len; ++i ) {
                final char x = format.charAt ( i );
                if ( escape || x != 'S' ) {
                    result.append ( x );
                } else {
                    result.append ( "{#}" );
                    this.usesMsecs = true;
                }
                if ( x == '\'' ) {
                    escape = !escape;
                }
            }
            return result.toString();
        }
        protected DateAndTimeElement ( final String header ) {
            this.usesMsecs = false;
            String format = header;
            boolean usesBegin = false;
            FormatType type = FormatType.CLF;
            if ( format != null ) {
                if ( format.equals ( "begin" ) ) {
                    usesBegin = true;
                    format = "";
                } else if ( format.startsWith ( "begin:" ) ) {
                    usesBegin = true;
                    format = format.substring ( 6 );
                } else if ( format.equals ( "end" ) ) {
                    usesBegin = false;
                    format = "";
                } else if ( format.startsWith ( "end:" ) ) {
                    usesBegin = false;
                    format = format.substring ( 4 );
                }
                if ( format.length() == 0 ) {
                    type = FormatType.CLF;
                } else if ( format.equals ( "sec" ) ) {
                    type = FormatType.SEC;
                } else if ( format.equals ( "msec" ) ) {
                    type = FormatType.MSEC;
                } else if ( format.equals ( "msec_frac" ) ) {
                    type = FormatType.MSEC_FRAC;
                } else {
                    type = FormatType.SDF;
                    format = this.tidyFormat ( format );
                }
            }
            this.format = format;
            this.usesBegin = usesBegin;
            this.type = type;
        }
        @Override
        public void addElement ( final CharArrayWriter buf, final Date date, final Request request, final Response response, final long time ) {
            long timestamp = date.getTime();
            if ( this.usesBegin ) {
                timestamp -= time;
            }
            switch ( this.type ) {
            case CLF: {
                buf.append ( AbstractAccessLogValve.localDateCache.get().getFormat ( timestamp ) );
                break;
            }
            case SEC: {
                buf.append ( Long.toString ( timestamp / 1000L ) );
                break;
            }
            case MSEC: {
                buf.append ( Long.toString ( timestamp ) );
                break;
            }
            case MSEC_FRAC: {
                final long frac = timestamp % 1000L;
                if ( frac < 100L ) {
                    if ( frac < 10L ) {
                        buf.append ( '0' );
                        buf.append ( '0' );
                    } else {
                        buf.append ( '0' );
                    }
                }
                buf.append ( Long.toString ( frac ) );
                break;
            }
            case SDF: {
                String temp = AbstractAccessLogValve.localDateCache.get().getFormat ( this.format, AbstractAccessLogValve.this.locale, timestamp );
                if ( this.usesMsecs ) {
                    final long frac = timestamp % 1000L;
                    final StringBuilder trippleMsec = new StringBuilder ( 4 );
                    if ( frac < 100L ) {
                        if ( frac < 10L ) {
                            trippleMsec.append ( '0' );
                            trippleMsec.append ( '0' );
                        } else {
                            trippleMsec.append ( '0' );
                        }
                    }
                    trippleMsec.append ( frac );
                    temp = temp.replace ( "{#}{#}{#}", trippleMsec );
                    temp = temp.replace ( "{#}", Long.toString ( frac ) );
                }
                buf.append ( temp );
                break;
            }
            }
        }
    }
    protected static class RequestElement implements AccessLogElement {
        @Override
        public void addElement ( final CharArrayWriter buf, final Date date, final Request request, final Response response, final long time ) {
            if ( request != null ) {
                final String method = request.getMethod();
                if ( method == null ) {
                    buf.append ( '-' );
                } else {
                    buf.append ( request.getMethod() );
                    buf.append ( ' ' );
                    buf.append ( request.getRequestURI() );
                    if ( request.getQueryString() != null ) {
                        buf.append ( '?' );
                        buf.append ( request.getQueryString() );
                    }
                    buf.append ( ' ' );
                    buf.append ( request.getProtocol() );
                }
            } else {
                buf.append ( '-' );
            }
        }
    }
    protected static class HttpStatusCodeElement implements AccessLogElement {
        @Override
        public void addElement ( final CharArrayWriter buf, final Date date, final Request request, final Response response, final long time ) {
            if ( response != null ) {
                final int status = response.getStatus();
                if ( 100 <= status && status < 1000 ) {
                    buf.append ( ( char ) ( 48 + status / 100 ) ).append ( ( char ) ( 48 + status / 10 % 10 ) ).append ( ( char ) ( 48 + status % 10 ) );
                } else {
                    buf.append ( Integer.toString ( status ) );
                }
            } else {
                buf.append ( '-' );
            }
        }
    }
    protected class PortElement implements AccessLogElement {
        private static final String localPort = "local";
        private static final String remotePort = "remote";
        private final PortType portType;
        public PortElement() {
            this.portType = PortType.LOCAL;
        }
        public PortElement ( final String type ) {
            switch ( type ) {
            case "remote": {
                this.portType = PortType.REMOTE;
                break;
            }
            case "local": {
                this.portType = PortType.LOCAL;
                break;
            }
            default: {
                AbstractAccessLogValve.log.error ( ValveBase.sm.getString ( "accessLogValve.invalidPortType", type ) );
                this.portType = PortType.LOCAL;
                break;
            }
            }
        }
        @Override
        public void addElement ( final CharArrayWriter buf, final Date date, final Request request, final Response response, final long time ) {
            if ( AbstractAccessLogValve.this.requestAttributesEnabled && this.portType == PortType.LOCAL ) {
                final Object port = request.getAttribute ( "org.apache.catalina.AccessLog.ServerPort" );
                if ( port == null ) {
                    buf.append ( Integer.toString ( request.getServerPort() ) );
                } else {
                    buf.append ( port.toString() );
                }
            } else if ( this.portType == PortType.LOCAL ) {
                buf.append ( Integer.toString ( request.getServerPort() ) );
            } else {
                buf.append ( Integer.toString ( request.getRemotePort() ) );
            }
        }
    }
    protected static class ByteSentElement implements AccessLogElement {
        private final boolean conversion;
        public ByteSentElement ( final boolean conversion ) {
            this.conversion = conversion;
        }
        @Override
        public void addElement ( final CharArrayWriter buf, final Date date, final Request request, final Response response, final long time ) {
            long length = response.getBytesWritten ( false );
            if ( length <= 0L ) {
                final Object start = request.getAttribute ( "org.apache.tomcat.sendfile.start" );
                if ( start instanceof Long ) {
                    final Object end = request.getAttribute ( "org.apache.tomcat.sendfile.end" );
                    if ( end instanceof Long ) {
                        length = ( long ) end - ( long ) start;
                    }
                }
            }
            if ( length <= 0L && this.conversion ) {
                buf.append ( '-' );
            } else {
                buf.append ( Long.toString ( length ) );
            }
        }
    }
    protected static class MethodElement implements AccessLogElement {
        @Override
        public void addElement ( final CharArrayWriter buf, final Date date, final Request request, final Response response, final long time ) {
            if ( request != null ) {
                buf.append ( request.getMethod() );
            }
        }
    }
    protected static class ElapsedTimeElement implements AccessLogElement {
        private final boolean millis;
        public ElapsedTimeElement ( final boolean millis ) {
            this.millis = millis;
        }
        @Override
        public void addElement ( final CharArrayWriter buf, final Date date, final Request request, final Response response, final long time ) {
            if ( this.millis ) {
                buf.append ( Long.toString ( time ) );
            } else {
                buf.append ( Long.toString ( time / 1000L ) );
                buf.append ( '.' );
                int remains = ( int ) ( time % 1000L );
                buf.append ( Long.toString ( remains / 100 ) );
                remains %= 100;
                buf.append ( Long.toString ( remains / 10 ) );
                buf.append ( Long.toString ( remains % 10 ) );
            }
        }
    }
    protected static class FirstByteTimeElement implements AccessLogElement {
        @Override
        public void addElement ( final CharArrayWriter buf, final Date date, final Request request, final Response response, final long time ) {
            final long commitTime = response.getCoyoteResponse().getCommitTime();
            if ( commitTime == -1L ) {
                buf.append ( '-' );
            } else {
                final long delta = commitTime - request.getCoyoteRequest().getStartTime();
                buf.append ( Long.toString ( delta ) );
            }
        }
    }
    protected static class QueryElement implements AccessLogElement {
        @Override
        public void addElement ( final CharArrayWriter buf, final Date date, final Request request, final Response response, final long time ) {
            String query = null;
            if ( request != null ) {
                query = request.getQueryString();
            }
            if ( query != null ) {
                buf.append ( '?' );
                buf.append ( query );
            }
        }
    }
    protected static class SessionIdElement implements AccessLogElement {
        @Override
        public void addElement ( final CharArrayWriter buf, final Date date, final Request request, final Response response, final long time ) {
            if ( request == null ) {
                buf.append ( '-' );
            } else {
                final Session session = request.getSessionInternal ( false );
                if ( session == null ) {
                    buf.append ( '-' );
                } else {
                    buf.append ( session.getIdInternal() );
                }
            }
        }
    }
    protected static class RequestURIElement implements AccessLogElement {
        @Override
        public void addElement ( final CharArrayWriter buf, final Date date, final Request request, final Response response, final long time ) {
            if ( request != null ) {
                buf.append ( request.getRequestURI() );
            } else {
                buf.append ( '-' );
            }
        }
    }
    protected static class LocalServerNameElement implements AccessLogElement {
        @Override
        public void addElement ( final CharArrayWriter buf, final Date date, final Request request, final Response response, final long time ) {
            buf.append ( request.getServerName() );
        }
    }
    protected static class StringElement implements AccessLogElement {
        private final String str;
        public StringElement ( final String str ) {
            this.str = str;
        }
        @Override
        public void addElement ( final CharArrayWriter buf, final Date date, final Request request, final Response response, final long time ) {
            buf.append ( this.str );
        }
    }
    protected static class HeaderElement implements AccessLogElement {
        private final String header;
        public HeaderElement ( final String header ) {
            this.header = header;
        }
        @Override
        public void addElement ( final CharArrayWriter buf, final Date date, final Request request, final Response response, final long time ) {
            final Enumeration<String> iter = request.getHeaders ( this.header );
            if ( iter.hasMoreElements() ) {
                buf.append ( iter.nextElement() );
                while ( iter.hasMoreElements() ) {
                    buf.append ( ',' ).append ( iter.nextElement() );
                }
                return;
            }
            buf.append ( '-' );
        }
    }
    protected static class CookieElement implements AccessLogElement {
        private final String header;
        public CookieElement ( final String header ) {
            this.header = header;
        }
        @Override
        public void addElement ( final CharArrayWriter buf, final Date date, final Request request, final Response response, final long time ) {
            String value = "-";
            final Cookie[] c = request.getCookies();
            if ( c != null ) {
                for ( int i = 0; i < c.length; ++i ) {
                    if ( this.header.equals ( c[i].getName() ) ) {
                        value = c[i].getValue();
                        break;
                    }
                }
            }
            buf.append ( value );
        }
    }
    protected static class ResponseHeaderElement implements AccessLogElement {
        private final String header;
        public ResponseHeaderElement ( final String header ) {
            this.header = header;
        }
        @Override
        public void addElement ( final CharArrayWriter buf, final Date date, final Request request, final Response response, final long time ) {
            if ( null != response ) {
                final Iterator<String> iter = response.getHeaders ( this.header ).iterator();
                if ( iter.hasNext() ) {
                    buf.append ( iter.next() );
                    while ( iter.hasNext() ) {
                        buf.append ( ',' ).append ( iter.next() );
                    }
                    return;
                }
            }
            buf.append ( '-' );
        }
    }
    protected static class RequestAttributeElement implements AccessLogElement {
        private final String header;
        public RequestAttributeElement ( final String header ) {
            this.header = header;
        }
        @Override
        public void addElement ( final CharArrayWriter buf, final Date date, final Request request, final Response response, final long time ) {
            Object value = null;
            if ( request != null ) {
                value = request.getAttribute ( this.header );
            } else {
                value = "??";
            }
            if ( value != null ) {
                if ( value instanceof String ) {
                    buf.append ( ( CharSequence ) value );
                } else {
                    buf.append ( value.toString() );
                }
            } else {
                buf.append ( '-' );
            }
        }
    }
    protected static class SessionAttributeElement implements AccessLogElement {
        private final String header;
        public SessionAttributeElement ( final String header ) {
            this.header = header;
        }
        @Override
        public void addElement ( final CharArrayWriter buf, final Date date, final Request request, final Response response, final long time ) {
            Object value = null;
            if ( null != request ) {
                final HttpSession sess = request.getSession ( false );
                if ( null != sess ) {
                    value = sess.getAttribute ( this.header );
                }
            } else {
                value = "??";
            }
            if ( value != null ) {
                if ( value instanceof String ) {
                    buf.append ( ( CharSequence ) value );
                } else {
                    buf.append ( value.toString() );
                }
            } else {
                buf.append ( '-' );
            }
        }
    }
    protected interface AccessLogElement {
        void addElement ( CharArrayWriter p0, Date p1, Request p2, Response p3, long p4 );
    }
}
