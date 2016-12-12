package org.apache.catalina.filters;
import javax.servlet.WriteListener;
import java.util.Locale;
import java.io.Writer;
import javax.servlet.ServletOutputStream;
import java.io.PrintWriter;
import javax.servlet.http.HttpServletResponseWrapper;
import org.apache.juli.logging.LogFactory;
import java.util.List;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.Enumeration;
import javax.servlet.FilterConfig;
import java.util.Iterator;
import java.util.Calendar;
import java.util.Date;
import javax.servlet.ServletException;
import java.io.IOException;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.FilterChain;
import javax.servlet.ServletResponse;
import javax.servlet.ServletRequest;
import java.util.LinkedHashMap;
import java.util.Map;
import org.apache.juli.logging.Log;
import java.util.regex.Pattern;
public class ExpiresFilter extends FilterBase {
    private static final Pattern commaSeparatedValuesPattern;
    private static final String HEADER_CACHE_CONTROL = "Cache-Control";
    private static final String HEADER_EXPIRES = "Expires";
    private static final String HEADER_LAST_MODIFIED = "Last-Modified";
    private static final Log log;
    private static final String PARAMETER_EXPIRES_BY_TYPE = "ExpiresByType";
    private static final String PARAMETER_EXPIRES_DEFAULT = "ExpiresDefault";
    private static final String PARAMETER_EXPIRES_EXCLUDED_RESPONSE_STATUS_CODES = "ExpiresExcludedResponseStatusCodes";
    private ExpiresConfiguration defaultExpiresConfiguration;
    private int[] excludedResponseStatusCodes;
    private Map<String, ExpiresConfiguration> expiresConfigurationByContentType;
    public ExpiresFilter() {
        this.excludedResponseStatusCodes = new int[] { 304 };
        this.expiresConfigurationByContentType = new LinkedHashMap<String, ExpiresConfiguration>();
    }
    protected static int[] commaDelimitedListToIntArray ( final String commaDelimitedInts ) {
        final String[] intsAsStrings = commaDelimitedListToStringArray ( commaDelimitedInts );
        final int[] ints = new int[intsAsStrings.length];
        for ( int i = 0; i < intsAsStrings.length; ++i ) {
            final String intAsString = intsAsStrings[i];
            try {
                ints[i] = Integer.parseInt ( intAsString );
            } catch ( NumberFormatException e ) {
                throw new RuntimeException ( "Exception parsing number '" + i + "' (zero based) of comma delimited list '" + commaDelimitedInts + "'" );
            }
        }
        return ints;
    }
    protected static String[] commaDelimitedListToStringArray ( final String commaDelimitedStrings ) {
        return ( commaDelimitedStrings == null || commaDelimitedStrings.length() == 0 ) ? new String[0] : ExpiresFilter.commaSeparatedValuesPattern.split ( commaDelimitedStrings );
    }
    protected static boolean contains ( final String str, final String searchStr ) {
        return str != null && searchStr != null && str.indexOf ( searchStr ) >= 0;
    }
    protected static String intsToCommaDelimitedString ( final int[] ints ) {
        if ( ints == null ) {
            return "";
        }
        final StringBuilder result = new StringBuilder();
        for ( int i = 0; i < ints.length; ++i ) {
            result.append ( ints[i] );
            if ( i < ints.length - 1 ) {
                result.append ( ", " );
            }
        }
        return result.toString();
    }
    protected static boolean isEmpty ( final String str ) {
        return str == null || str.length() == 0;
    }
    protected static boolean isNotEmpty ( final String str ) {
        return !isEmpty ( str );
    }
    protected static boolean startsWithIgnoreCase ( final String string, final String prefix ) {
        if ( string == null || prefix == null ) {
            return string == null && prefix == null;
        }
        return prefix.length() <= string.length() && string.regionMatches ( true, 0, prefix, 0, prefix.length() );
    }
    protected static String substringBefore ( final String str, final String separator ) {
        if ( str == null || str.isEmpty() || separator == null ) {
            return null;
        }
        if ( separator.isEmpty() ) {
            return "";
        }
        final int separatorIndex = str.indexOf ( separator );
        if ( separatorIndex == -1 ) {
            return str;
        }
        return str.substring ( 0, separatorIndex );
    }
    public void doFilter ( final ServletRequest request, final ServletResponse response, final FilterChain chain ) throws IOException, ServletException {
        if ( request instanceof HttpServletRequest && response instanceof HttpServletResponse ) {
            final HttpServletRequest httpRequest = ( HttpServletRequest ) request;
            final HttpServletResponse httpResponse = ( HttpServletResponse ) response;
            if ( response.isCommitted() ) {
                if ( ExpiresFilter.log.isDebugEnabled() ) {
                    ExpiresFilter.log.debug ( ExpiresFilter.sm.getString ( "expiresFilter.responseAlreadyCommited", httpRequest.getRequestURL() ) );
                }
                chain.doFilter ( request, response );
            } else {
                final XHttpServletResponse xResponse = new XHttpServletResponse ( httpRequest, httpResponse );
                chain.doFilter ( request, ( ServletResponse ) xResponse );
                if ( !xResponse.isWriteResponseBodyStarted() ) {
                    this.onBeforeWriteResponseBody ( httpRequest, xResponse );
                }
            }
        } else {
            chain.doFilter ( request, response );
        }
    }
    public ExpiresConfiguration getDefaultExpiresConfiguration() {
        return this.defaultExpiresConfiguration;
    }
    public String getExcludedResponseStatusCodes() {
        return intsToCommaDelimitedString ( this.excludedResponseStatusCodes );
    }
    public int[] getExcludedResponseStatusCodesAsInts() {
        return this.excludedResponseStatusCodes;
    }
    protected Date getExpirationDate ( final XHttpServletResponse response ) {
        final String contentType = response.getContentType();
        ExpiresConfiguration configuration = this.expiresConfigurationByContentType.get ( contentType );
        if ( configuration != null ) {
            final Date result = this.getExpirationDate ( configuration, response );
            if ( ExpiresFilter.log.isDebugEnabled() ) {
                ExpiresFilter.log.debug ( ExpiresFilter.sm.getString ( "expiresFilter.useMatchingConfiguration", configuration, contentType, contentType, result ) );
            }
            return result;
        }
        if ( contains ( contentType, ";" ) ) {
            final String contentTypeWithoutCharset = substringBefore ( contentType, ";" ).trim();
            configuration = this.expiresConfigurationByContentType.get ( contentTypeWithoutCharset );
            if ( configuration != null ) {
                final Date result2 = this.getExpirationDate ( configuration, response );
                if ( ExpiresFilter.log.isDebugEnabled() ) {
                    ExpiresFilter.log.debug ( ExpiresFilter.sm.getString ( "expiresFilter.useMatchingConfiguration", configuration, contentTypeWithoutCharset, contentType, result2 ) );
                }
                return result2;
            }
        }
        if ( contains ( contentType, "/" ) ) {
            final String majorType = substringBefore ( contentType, "/" );
            configuration = this.expiresConfigurationByContentType.get ( majorType );
            if ( configuration != null ) {
                final Date result2 = this.getExpirationDate ( configuration, response );
                if ( ExpiresFilter.log.isDebugEnabled() ) {
                    ExpiresFilter.log.debug ( ExpiresFilter.sm.getString ( "expiresFilter.useMatchingConfiguration", configuration, majorType, contentType, result2 ) );
                }
                return result2;
            }
        }
        if ( this.defaultExpiresConfiguration != null ) {
            final Date result = this.getExpirationDate ( this.defaultExpiresConfiguration, response );
            if ( ExpiresFilter.log.isDebugEnabled() ) {
                ExpiresFilter.log.debug ( ExpiresFilter.sm.getString ( "expiresFilter.useDefaultConfiguration", this.defaultExpiresConfiguration, contentType, result ) );
            }
            return result;
        }
        if ( ExpiresFilter.log.isDebugEnabled() ) {
            ExpiresFilter.log.debug ( ExpiresFilter.sm.getString ( "expiresFilter.noExpirationConfiguredForContentType", contentType ) );
        }
        return null;
    }
    protected Date getExpirationDate ( final ExpiresConfiguration configuration, final XHttpServletResponse response ) {
        Calendar calendar = null;
        switch ( configuration.getStartingPoint() ) {
        case ACCESS_TIME: {
            calendar = Calendar.getInstance();
            break;
        }
        case LAST_MODIFICATION_TIME: {
            if ( response.isLastModifiedHeaderSet() ) {
                try {
                    final long lastModified = response.getLastModifiedHeader();
                    calendar = Calendar.getInstance();
                    calendar.setTimeInMillis ( lastModified );
                } catch ( NumberFormatException e ) {
                    calendar = Calendar.getInstance();
                }
                break;
            }
            calendar = Calendar.getInstance();
            break;
        }
        default: {
            throw new IllegalStateException ( ExpiresFilter.sm.getString ( "expiresFilter.unsupportedStartingPoint", configuration.getStartingPoint() ) );
        }
        }
        for ( final Duration duration : configuration.getDurations() ) {
            calendar.add ( duration.getUnit().getCalendardField(), duration.getAmount() );
        }
        return calendar.getTime();
    }
    public Map<String, ExpiresConfiguration> getExpiresConfigurationByContentType() {
        return this.expiresConfigurationByContentType;
    }
    @Override
    protected Log getLogger() {
        return ExpiresFilter.log;
    }
    @Override
    public void init ( final FilterConfig filterConfig ) throws ServletException {
        final Enumeration<String> names = ( Enumeration<String> ) filterConfig.getInitParameterNames();
        while ( names.hasMoreElements() ) {
            final String name = names.nextElement();
            final String value = filterConfig.getInitParameter ( name );
            try {
                if ( name.startsWith ( "ExpiresByType" ) ) {
                    final String contentType = name.substring ( "ExpiresByType".length() ).trim();
                    final ExpiresConfiguration expiresConfiguration = this.parseExpiresConfiguration ( value );
                    this.expiresConfigurationByContentType.put ( contentType, expiresConfiguration );
                } else if ( name.equalsIgnoreCase ( "ExpiresDefault" ) ) {
                    final ExpiresConfiguration expiresConfiguration2 = this.parseExpiresConfiguration ( value );
                    this.defaultExpiresConfiguration = expiresConfiguration2;
                } else if ( name.equalsIgnoreCase ( "ExpiresExcludedResponseStatusCodes" ) ) {
                    this.excludedResponseStatusCodes = commaDelimitedListToIntArray ( value );
                } else {
                    ExpiresFilter.log.warn ( ExpiresFilter.sm.getString ( "expiresFilter.unknownParameterIgnored", name, value ) );
                }
            } catch ( RuntimeException e ) {
                throw new ServletException ( ExpiresFilter.sm.getString ( "expiresFilter.exceptionProcessingParameter", name, value ), ( Throwable ) e );
            }
        }
        ExpiresFilter.log.debug ( ExpiresFilter.sm.getString ( "expiresFilter.filterInitialized", this.toString() ) );
    }
    protected boolean isEligibleToExpirationHeaderGeneration ( final HttpServletRequest request, final XHttpServletResponse response ) {
        final boolean expirationHeaderHasBeenSet = response.containsHeader ( "Expires" ) || contains ( response.getCacheControlHeader(), "max-age" );
        if ( expirationHeaderHasBeenSet ) {
            if ( ExpiresFilter.log.isDebugEnabled() ) {
                ExpiresFilter.log.debug ( ExpiresFilter.sm.getString ( "expiresFilter.expirationHeaderAlreadyDefined", request.getRequestURI(), response.getStatus(), response.getContentType() ) );
            }
            return false;
        }
        for ( final int skippedStatusCode : this.excludedResponseStatusCodes ) {
            if ( response.getStatus() == skippedStatusCode ) {
                if ( ExpiresFilter.log.isDebugEnabled() ) {
                    ExpiresFilter.log.debug ( ExpiresFilter.sm.getString ( "expiresFilter.skippedStatusCode", request.getRequestURI(), response.getStatus(), response.getContentType() ) );
                }
                return false;
            }
        }
        return true;
    }
    public void onBeforeWriteResponseBody ( final HttpServletRequest request, final XHttpServletResponse response ) {
        if ( !this.isEligibleToExpirationHeaderGeneration ( request, response ) ) {
            return;
        }
        final Date expirationDate = this.getExpirationDate ( response );
        if ( expirationDate == null ) {
            if ( ExpiresFilter.log.isDebugEnabled() ) {
                ExpiresFilter.log.debug ( ExpiresFilter.sm.getString ( "expiresFilter.noExpirationConfigured", request.getRequestURI(), response.getStatus(), response.getContentType() ) );
            }
        } else {
            if ( ExpiresFilter.log.isDebugEnabled() ) {
                ExpiresFilter.log.debug ( ExpiresFilter.sm.getString ( "expiresFilter.setExpirationDate", request.getRequestURI(), response.getStatus(), response.getContentType(), expirationDate ) );
            }
            final String maxAgeDirective = "max-age=" + ( expirationDate.getTime() - System.currentTimeMillis() ) / 1000L;
            final String cacheControlHeader = response.getCacheControlHeader();
            final String newCacheControlHeader = ( cacheControlHeader == null ) ? maxAgeDirective : ( cacheControlHeader + ", " + maxAgeDirective );
            response.setHeader ( "Cache-Control", newCacheControlHeader );
            response.setDateHeader ( "Expires", expirationDate.getTime() );
        }
    }
    protected ExpiresConfiguration parseExpiresConfiguration ( final String inputLine ) {
        final String line = inputLine.trim();
        StringTokenizer tokenizer = new StringTokenizer ( line, " " );
        String currentToken;
        try {
            currentToken = tokenizer.nextToken();
        } catch ( NoSuchElementException e ) {
            throw new IllegalStateException ( ExpiresFilter.sm.getString ( "expiresFilter.startingPointNotFound", line ) );
        }
        StartingPoint startingPoint;
        if ( "access".equalsIgnoreCase ( currentToken ) || "now".equalsIgnoreCase ( currentToken ) ) {
            startingPoint = StartingPoint.ACCESS_TIME;
        } else if ( "modification".equalsIgnoreCase ( currentToken ) ) {
            startingPoint = StartingPoint.LAST_MODIFICATION_TIME;
        } else if ( !tokenizer.hasMoreTokens() && startsWithIgnoreCase ( currentToken, "a" ) ) {
            startingPoint = StartingPoint.ACCESS_TIME;
            tokenizer = new StringTokenizer ( currentToken.substring ( 1 ) + " seconds", " " );
        } else {
            if ( tokenizer.hasMoreTokens() || !startsWithIgnoreCase ( currentToken, "m" ) ) {
                throw new IllegalStateException ( ExpiresFilter.sm.getString ( "expiresFilter.startingPointInvalid", currentToken, line ) );
            }
            startingPoint = StartingPoint.LAST_MODIFICATION_TIME;
            tokenizer = new StringTokenizer ( currentToken.substring ( 1 ) + " seconds", " " );
        }
        try {
            currentToken = tokenizer.nextToken();
        } catch ( NoSuchElementException e2 ) {
            throw new IllegalStateException ( ExpiresFilter.sm.getString ( "expiresFilter.noDurationFound", line ) );
        }
        if ( "plus".equalsIgnoreCase ( currentToken ) ) {
            try {
                currentToken = tokenizer.nextToken();
            } catch ( NoSuchElementException e2 ) {
                throw new IllegalStateException ( ExpiresFilter.sm.getString ( "expiresFilter.noDurationFound", line ) );
            }
        }
        final List<Duration> durations = new ArrayList<Duration>();
        while ( currentToken != null ) {
            int amount;
            try {
                amount = Integer.parseInt ( currentToken );
            } catch ( NumberFormatException e3 ) {
                throw new IllegalStateException ( ExpiresFilter.sm.getString ( "expiresFilter.invalidDurationNumber", currentToken, line ) );
            }
            try {
                currentToken = tokenizer.nextToken();
            } catch ( NoSuchElementException e4 ) {
                throw new IllegalStateException ( ExpiresFilter.sm.getString ( "expiresFilter.noDurationUnitAfterAmount", amount, line ) );
            }
            DurationUnit durationUnit;
            if ( "year".equalsIgnoreCase ( currentToken ) || "years".equalsIgnoreCase ( currentToken ) ) {
                durationUnit = DurationUnit.YEAR;
            } else if ( "month".equalsIgnoreCase ( currentToken ) || "months".equalsIgnoreCase ( currentToken ) ) {
                durationUnit = DurationUnit.MONTH;
            } else if ( "week".equalsIgnoreCase ( currentToken ) || "weeks".equalsIgnoreCase ( currentToken ) ) {
                durationUnit = DurationUnit.WEEK;
            } else if ( "day".equalsIgnoreCase ( currentToken ) || "days".equalsIgnoreCase ( currentToken ) ) {
                durationUnit = DurationUnit.DAY;
            } else if ( "hour".equalsIgnoreCase ( currentToken ) || "hours".equalsIgnoreCase ( currentToken ) ) {
                durationUnit = DurationUnit.HOUR;
            } else if ( "minute".equalsIgnoreCase ( currentToken ) || "minutes".equalsIgnoreCase ( currentToken ) ) {
                durationUnit = DurationUnit.MINUTE;
            } else {
                if ( !"second".equalsIgnoreCase ( currentToken ) && !"seconds".equalsIgnoreCase ( currentToken ) ) {
                    throw new IllegalStateException ( ExpiresFilter.sm.getString ( "expiresFilter.invalidDurationUnit", currentToken, line ) );
                }
                durationUnit = DurationUnit.SECOND;
            }
            final Duration duration = new Duration ( amount, durationUnit );
            durations.add ( duration );
            if ( tokenizer.hasMoreTokens() ) {
                currentToken = tokenizer.nextToken();
            } else {
                currentToken = null;
            }
        }
        return new ExpiresConfiguration ( startingPoint, durations );
    }
    public void setDefaultExpiresConfiguration ( final ExpiresConfiguration defaultExpiresConfiguration ) {
        this.defaultExpiresConfiguration = defaultExpiresConfiguration;
    }
    public void setExcludedResponseStatusCodes ( final int[] excludedResponseStatusCodes ) {
        this.excludedResponseStatusCodes = excludedResponseStatusCodes;
    }
    public void setExpiresConfigurationByContentType ( final Map<String, ExpiresConfiguration> expiresConfigurationByContentType ) {
        this.expiresConfigurationByContentType = expiresConfigurationByContentType;
    }
    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "[excludedResponseStatusCode=[" + intsToCommaDelimitedString ( this.excludedResponseStatusCodes ) + "], default=" + this.defaultExpiresConfiguration + ", byType=" + this.expiresConfigurationByContentType + "]";
    }
    static {
        commaSeparatedValuesPattern = Pattern.compile ( "\\s*,\\s*" );
        log = LogFactory.getLog ( ExpiresFilter.class );
    }
    protected static class Duration {
        protected final int amount;
        protected final DurationUnit unit;
        public Duration ( final int amount, final DurationUnit unit ) {
            this.amount = amount;
            this.unit = unit;
        }
        public int getAmount() {
            return this.amount;
        }
        public DurationUnit getUnit() {
            return this.unit;
        }
        @Override
        public String toString() {
            return this.amount + " " + this.unit;
        }
    }
    protected enum DurationUnit {
        DAY ( 6 ),
        HOUR ( 10 ),
        MINUTE ( 12 ),
        MONTH ( 2 ),
        SECOND ( 13 ),
        WEEK ( 3 ),
        YEAR ( 1 );
        private final int calendardField;
        private DurationUnit ( final int calendardField ) {
            this.calendardField = calendardField;
        }
        public int getCalendardField() {
            return this.calendardField;
        }
    }
    protected static class ExpiresConfiguration {
        private final List<Duration> durations;
        private final StartingPoint startingPoint;
        public ExpiresConfiguration ( final StartingPoint startingPoint, final List<Duration> durations ) {
            this.startingPoint = startingPoint;
            this.durations = durations;
        }
        public List<Duration> getDurations() {
            return this.durations;
        }
        public StartingPoint getStartingPoint() {
            return this.startingPoint;
        }
        @Override
        public String toString() {
            return "ExpiresConfiguration[startingPoint=" + this.startingPoint + ", duration=" + this.durations + "]";
        }
    }
    protected enum StartingPoint {
        ACCESS_TIME,
        LAST_MODIFICATION_TIME;
    }
    public class XHttpServletResponse extends HttpServletResponseWrapper {
        private String cacheControlHeader;
        private long lastModifiedHeader;
        private boolean lastModifiedHeaderSet;
        private PrintWriter printWriter;
        private final HttpServletRequest request;
        private ServletOutputStream servletOutputStream;
        private boolean writeResponseBodyStarted;
        public XHttpServletResponse ( final HttpServletRequest request, final HttpServletResponse response ) {
            super ( response );
            this.request = request;
        }
        public void addDateHeader ( final String name, final long date ) {
            super.addDateHeader ( name, date );
            if ( !this.lastModifiedHeaderSet ) {
                this.lastModifiedHeader = date;
                this.lastModifiedHeaderSet = true;
            }
        }
        public void addHeader ( final String name, final String value ) {
            super.addHeader ( name, value );
            if ( "Cache-Control".equalsIgnoreCase ( name ) && this.cacheControlHeader == null ) {
                this.cacheControlHeader = value;
            }
        }
        public String getCacheControlHeader() {
            return this.cacheControlHeader;
        }
        public long getLastModifiedHeader() {
            return this.lastModifiedHeader;
        }
        public ServletOutputStream getOutputStream() throws IOException {
            if ( this.servletOutputStream == null ) {
                this.servletOutputStream = new XServletOutputStream ( super.getOutputStream(), this.request, this );
            }
            return this.servletOutputStream;
        }
        public PrintWriter getWriter() throws IOException {
            if ( this.printWriter == null ) {
                this.printWriter = new XPrintWriter ( super.getWriter(), this.request, this );
            }
            return this.printWriter;
        }
        public boolean isLastModifiedHeaderSet() {
            return this.lastModifiedHeaderSet;
        }
        public boolean isWriteResponseBodyStarted() {
            return this.writeResponseBodyStarted;
        }
        public void reset() {
            super.reset();
            this.lastModifiedHeader = 0L;
            this.lastModifiedHeaderSet = false;
            this.cacheControlHeader = null;
        }
        public void setDateHeader ( final String name, final long date ) {
            super.setDateHeader ( name, date );
            if ( "Last-Modified".equalsIgnoreCase ( name ) ) {
                this.lastModifiedHeader = date;
                this.lastModifiedHeaderSet = true;
            }
        }
        public void setHeader ( final String name, final String value ) {
            super.setHeader ( name, value );
            if ( "Cache-Control".equalsIgnoreCase ( name ) ) {
                this.cacheControlHeader = value;
            }
        }
        public void setWriteResponseBodyStarted ( final boolean writeResponseBodyStarted ) {
            this.writeResponseBodyStarted = writeResponseBodyStarted;
        }
    }
    public class XPrintWriter extends PrintWriter {
        private final PrintWriter out;
        private final HttpServletRequest request;
        private final XHttpServletResponse response;
        public XPrintWriter ( final PrintWriter out, final HttpServletRequest request, final XHttpServletResponse response ) {
            super ( out );
            this.out = out;
            this.request = request;
            this.response = response;
        }
        @Override
        public PrintWriter append ( final char c ) {
            this.fireBeforeWriteResponseBodyEvent();
            return this.out.append ( c );
        }
        @Override
        public PrintWriter append ( final CharSequence csq ) {
            this.fireBeforeWriteResponseBodyEvent();
            return this.out.append ( csq );
        }
        @Override
        public PrintWriter append ( final CharSequence csq, final int start, final int end ) {
            this.fireBeforeWriteResponseBodyEvent();
            return this.out.append ( csq, start, end );
        }
        @Override
        public void close() {
            this.fireBeforeWriteResponseBodyEvent();
            this.out.close();
        }
        private void fireBeforeWriteResponseBodyEvent() {
            if ( !this.response.isWriteResponseBodyStarted() ) {
                this.response.setWriteResponseBodyStarted ( true );
                ExpiresFilter.this.onBeforeWriteResponseBody ( this.request, this.response );
            }
        }
        @Override
        public void flush() {
            this.fireBeforeWriteResponseBodyEvent();
            this.out.flush();
        }
        @Override
        public void print ( final boolean b ) {
            this.fireBeforeWriteResponseBodyEvent();
            this.out.print ( b );
        }
        @Override
        public void print ( final char c ) {
            this.fireBeforeWriteResponseBodyEvent();
            this.out.print ( c );
        }
        @Override
        public void print ( final char[] s ) {
            this.fireBeforeWriteResponseBodyEvent();
            this.out.print ( s );
        }
        @Override
        public void print ( final double d ) {
            this.fireBeforeWriteResponseBodyEvent();
            this.out.print ( d );
        }
        @Override
        public void print ( final float f ) {
            this.fireBeforeWriteResponseBodyEvent();
            this.out.print ( f );
        }
        @Override
        public void print ( final int i ) {
            this.fireBeforeWriteResponseBodyEvent();
            this.out.print ( i );
        }
        @Override
        public void print ( final long l ) {
            this.fireBeforeWriteResponseBodyEvent();
            this.out.print ( l );
        }
        @Override
        public void print ( final Object obj ) {
            this.fireBeforeWriteResponseBodyEvent();
            this.out.print ( obj );
        }
        @Override
        public void print ( final String s ) {
            this.fireBeforeWriteResponseBodyEvent();
            this.out.print ( s );
        }
        @Override
        public PrintWriter printf ( final Locale l, final String format, final Object... args ) {
            this.fireBeforeWriteResponseBodyEvent();
            return this.out.printf ( l, format, args );
        }
        @Override
        public PrintWriter printf ( final String format, final Object... args ) {
            this.fireBeforeWriteResponseBodyEvent();
            return this.out.printf ( format, args );
        }
        @Override
        public void println() {
            this.fireBeforeWriteResponseBodyEvent();
            this.out.println();
        }
        @Override
        public void println ( final boolean x ) {
            this.fireBeforeWriteResponseBodyEvent();
            this.out.println ( x );
        }
        @Override
        public void println ( final char x ) {
            this.fireBeforeWriteResponseBodyEvent();
            this.out.println ( x );
        }
        @Override
        public void println ( final char[] x ) {
            this.fireBeforeWriteResponseBodyEvent();
            this.out.println ( x );
        }
        @Override
        public void println ( final double x ) {
            this.fireBeforeWriteResponseBodyEvent();
            this.out.println ( x );
        }
        @Override
        public void println ( final float x ) {
            this.fireBeforeWriteResponseBodyEvent();
            this.out.println ( x );
        }
        @Override
        public void println ( final int x ) {
            this.fireBeforeWriteResponseBodyEvent();
            this.out.println ( x );
        }
        @Override
        public void println ( final long x ) {
            this.fireBeforeWriteResponseBodyEvent();
            this.out.println ( x );
        }
        @Override
        public void println ( final Object x ) {
            this.fireBeforeWriteResponseBodyEvent();
            this.out.println ( x );
        }
        @Override
        public void println ( final String x ) {
            this.fireBeforeWriteResponseBodyEvent();
            this.out.println ( x );
        }
        @Override
        public void write ( final char[] buf ) {
            this.fireBeforeWriteResponseBodyEvent();
            this.out.write ( buf );
        }
        @Override
        public void write ( final char[] buf, final int off, final int len ) {
            this.fireBeforeWriteResponseBodyEvent();
            this.out.write ( buf, off, len );
        }
        @Override
        public void write ( final int c ) {
            this.fireBeforeWriteResponseBodyEvent();
            this.out.write ( c );
        }
        @Override
        public void write ( final String s ) {
            this.fireBeforeWriteResponseBodyEvent();
            this.out.write ( s );
        }
        @Override
        public void write ( final String s, final int off, final int len ) {
            this.fireBeforeWriteResponseBodyEvent();
            this.out.write ( s, off, len );
        }
    }
    public class XServletOutputStream extends ServletOutputStream {
        private final HttpServletRequest request;
        private final XHttpServletResponse response;
        private final ServletOutputStream servletOutputStream;
        public XServletOutputStream ( final ServletOutputStream servletOutputStream, final HttpServletRequest request, final XHttpServletResponse response ) {
            this.servletOutputStream = servletOutputStream;
            this.response = response;
            this.request = request;
        }
        public void close() throws IOException {
            this.fireOnBeforeWriteResponseBodyEvent();
            this.servletOutputStream.close();
        }
        private void fireOnBeforeWriteResponseBodyEvent() {
            if ( !this.response.isWriteResponseBodyStarted() ) {
                this.response.setWriteResponseBodyStarted ( true );
                ExpiresFilter.this.onBeforeWriteResponseBody ( this.request, this.response );
            }
        }
        public void flush() throws IOException {
            this.fireOnBeforeWriteResponseBodyEvent();
            this.servletOutputStream.flush();
        }
        public void print ( final boolean b ) throws IOException {
            this.fireOnBeforeWriteResponseBodyEvent();
            this.servletOutputStream.print ( b );
        }
        public void print ( final char c ) throws IOException {
            this.fireOnBeforeWriteResponseBodyEvent();
            this.servletOutputStream.print ( c );
        }
        public void print ( final double d ) throws IOException {
            this.fireOnBeforeWriteResponseBodyEvent();
            this.servletOutputStream.print ( d );
        }
        public void print ( final float f ) throws IOException {
            this.fireOnBeforeWriteResponseBodyEvent();
            this.servletOutputStream.print ( f );
        }
        public void print ( final int i ) throws IOException {
            this.fireOnBeforeWriteResponseBodyEvent();
            this.servletOutputStream.print ( i );
        }
        public void print ( final long l ) throws IOException {
            this.fireOnBeforeWriteResponseBodyEvent();
            this.servletOutputStream.print ( l );
        }
        public void print ( final String s ) throws IOException {
            this.fireOnBeforeWriteResponseBodyEvent();
            this.servletOutputStream.print ( s );
        }
        public void println() throws IOException {
            this.fireOnBeforeWriteResponseBodyEvent();
            this.servletOutputStream.println();
        }
        public void println ( final boolean b ) throws IOException {
            this.fireOnBeforeWriteResponseBodyEvent();
            this.servletOutputStream.println ( b );
        }
        public void println ( final char c ) throws IOException {
            this.fireOnBeforeWriteResponseBodyEvent();
            this.servletOutputStream.println ( c );
        }
        public void println ( final double d ) throws IOException {
            this.fireOnBeforeWriteResponseBodyEvent();
            this.servletOutputStream.println ( d );
        }
        public void println ( final float f ) throws IOException {
            this.fireOnBeforeWriteResponseBodyEvent();
            this.servletOutputStream.println ( f );
        }
        public void println ( final int i ) throws IOException {
            this.fireOnBeforeWriteResponseBodyEvent();
            this.servletOutputStream.println ( i );
        }
        public void println ( final long l ) throws IOException {
            this.fireOnBeforeWriteResponseBodyEvent();
            this.servletOutputStream.println ( l );
        }
        public void println ( final String s ) throws IOException {
            this.fireOnBeforeWriteResponseBodyEvent();
            this.servletOutputStream.println ( s );
        }
        public void write ( final byte[] b ) throws IOException {
            this.fireOnBeforeWriteResponseBodyEvent();
            this.servletOutputStream.write ( b );
        }
        public void write ( final byte[] b, final int off, final int len ) throws IOException {
            this.fireOnBeforeWriteResponseBodyEvent();
            this.servletOutputStream.write ( b, off, len );
        }
        public void write ( final int b ) throws IOException {
            this.fireOnBeforeWriteResponseBodyEvent();
            this.servletOutputStream.write ( b );
        }
        public boolean isReady() {
            return false;
        }
        public void setWriteListener ( final WriteListener listener ) {
        }
    }
}
