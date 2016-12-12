package org.apache.coyote.http2;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.coyote.Adapter;
import org.apache.coyote.Processor;
import org.apache.coyote.Request;
import org.apache.coyote.UpgradeProtocol;
import org.apache.coyote.UpgradeToken;
import org.apache.coyote.http11.upgrade.InternalHttpUpgradeHandler;
import org.apache.coyote.http11.upgrade.UpgradeProcessorInternal;
import org.apache.tomcat.util.net.SocketWrapperBase;
public class Http2Protocol implements UpgradeProtocol {
    static final long DEFAULT_READ_TIMEOUT = 10000;
    static final long DEFAULT_KEEP_ALIVE_TIMEOUT = -1;
    static final long DEFAULT_WRITE_TIMEOUT = 10000;
    static final long DEFAULT_MAX_CONCURRENT_STREAMS = 200;
    static final int DEFAULT_MAX_CONCURRENT_STREAM_EXECUTION = 200;
    static final int DEFAULT_INITIAL_WINDOW_SIZE = ( 1 << 16 ) - 1;
    private static final String HTTP_UPGRADE_NAME = "h2c";
    private static final String ALPN_NAME = "h2";
    private static final byte[] ALPN_IDENTIFIER = ALPN_NAME.getBytes ( StandardCharsets.UTF_8 );
    private long readTimeout = DEFAULT_READ_TIMEOUT;
    private long keepAliveTimeout = DEFAULT_KEEP_ALIVE_TIMEOUT;
    private long writeTimeout = DEFAULT_WRITE_TIMEOUT;
    private long maxConcurrentStreams = DEFAULT_MAX_CONCURRENT_STREAMS;
    private int maxConcurrentStreamExecution = DEFAULT_MAX_CONCURRENT_STREAM_EXECUTION;
    private int initialWindowSize = DEFAULT_INITIAL_WINDOW_SIZE;
    private Set<String> allowedTrailerHeaders =
        Collections.newSetFromMap ( new ConcurrentHashMap<String, Boolean>() );
    private int maxHeaderCount = Constants.DEFAULT_MAX_HEADER_COUNT;
    private int maxHeaderSize = Constants.DEFAULT_MAX_HEADER_SIZE;
    private int maxTrailerCount = Constants.DEFAULT_MAX_TRAILER_COUNT;
    private int maxTrailerSize = Constants.DEFAULT_MAX_TRAILER_SIZE;
    @Override
    public String getHttpUpgradeName ( boolean isSSLEnabled ) {
        if ( isSSLEnabled ) {
            return null;
        } else {
            return HTTP_UPGRADE_NAME;
        }
    }
    @Override
    public byte[] getAlpnIdentifier() {
        return ALPN_IDENTIFIER;
    }
    @Override
    public String getAlpnName() {
        return ALPN_NAME;
    }
    @Override
    public Processor getProcessor ( SocketWrapperBase<?> socketWrapper, Adapter adapter ) {
        UpgradeProcessorInternal processor = new UpgradeProcessorInternal ( socketWrapper,
                new UpgradeToken ( getInternalUpgradeHandler ( adapter, null ), null, null ) );
        return processor;
    }
    @Override
    public InternalHttpUpgradeHandler getInternalUpgradeHandler ( Adapter adapter,
            Request coyoteRequest ) {
        Http2UpgradeHandler result = new Http2UpgradeHandler ( adapter, coyoteRequest );
        result.setReadTimeout ( getReadTimeout() );
        result.setKeepAliveTimeout ( getKeepAliveTimeout() );
        result.setWriteTimeout ( getWriteTimeout() );
        result.setMaxConcurrentStreams ( getMaxConcurrentStreams() );
        result.setMaxConcurrentStreamExecution ( getMaxConcurrentStreamExecution() );
        result.setInitialWindowSize ( getInitialWindowSize() );
        result.setAllowedTrailerHeaders ( allowedTrailerHeaders );
        result.setMaxHeaderCount ( getMaxHeaderCount() );
        result.setMaxHeaderSize ( getMaxHeaderSize() );
        result.setMaxTrailerCount ( getMaxTrailerCount() );
        result.setMaxTrailerSize ( getMaxTrailerSize() );
        return result;
    }
    @Override
    public boolean accept ( Request request ) {
        Enumeration<String> settings = request.getMimeHeaders().values ( "HTTP2-Settings" );
        int count = 0;
        while ( settings.hasMoreElements() ) {
            count++;
            settings.nextElement();
        }
        if ( count != 1 ) {
            return false;
        }
        Enumeration<String> connection = request.getMimeHeaders().values ( "Connection" );
        boolean found = false;
        while ( connection.hasMoreElements() && !found ) {
            found = connection.nextElement().contains ( "HTTP2-Settings" );
        }
        return found;
    }
    public long getReadTimeout() {
        return readTimeout;
    }
    public void setReadTimeout ( long readTimeout ) {
        this.readTimeout = readTimeout;
    }
    public long getKeepAliveTimeout() {
        return keepAliveTimeout;
    }
    public void setKeepAliveTimeout ( long keepAliveTimeout ) {
        this.keepAliveTimeout = keepAliveTimeout;
    }
    public long getWriteTimeout() {
        return writeTimeout;
    }
    public void setWriteTimeout ( long writeTimeout ) {
        this.writeTimeout = writeTimeout;
    }
    public long getMaxConcurrentStreams() {
        return maxConcurrentStreams;
    }
    public void setMaxConcurrentStreams ( long maxConcurrentStreams ) {
        this.maxConcurrentStreams = maxConcurrentStreams;
    }
    public int getMaxConcurrentStreamExecution() {
        return maxConcurrentStreamExecution;
    }
    public void setMaxConcurrentStreamExecution ( int maxConcurrentStreamExecution ) {
        this.maxConcurrentStreamExecution = maxConcurrentStreamExecution;
    }
    public int getInitialWindowSize() {
        return initialWindowSize;
    }
    public void setInitialWindowSize ( int initialWindowSize ) {
        this.initialWindowSize = initialWindowSize;
    }
    public void setAllowedTrailerHeaders ( String commaSeparatedHeaders ) {
        Set<String> toRemove = new HashSet<>();
        toRemove.addAll ( allowedTrailerHeaders );
        if ( commaSeparatedHeaders != null ) {
            String[] headers = commaSeparatedHeaders.split ( "," );
            for ( String header : headers ) {
                String trimmedHeader = header.trim().toLowerCase ( Locale.ENGLISH );
                if ( toRemove.contains ( trimmedHeader ) ) {
                    toRemove.remove ( trimmedHeader );
                } else {
                    allowedTrailerHeaders.add ( trimmedHeader );
                }
            }
            allowedTrailerHeaders.removeAll ( toRemove );
        }
    }
    public String getAllowedTrailerHeaders() {
        List<String> copy = new ArrayList<> ( allowedTrailerHeaders.size() );
        copy.addAll ( allowedTrailerHeaders );
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for ( String header : copy ) {
            if ( first ) {
                first = false;
            } else {
                result.append ( ',' );
            }
            result.append ( header );
        }
        return result.toString();
    }
    public void setMaxHeaderCount ( int maxHeaderCount ) {
        this.maxHeaderCount = maxHeaderCount;
    }
    public int getMaxHeaderCount() {
        return maxHeaderCount;
    }
    public void setMaxHeaderSize ( int maxHeaderSize ) {
        this.maxHeaderSize = maxHeaderSize;
    }
    public int getMaxHeaderSize() {
        return maxHeaderSize;
    }
    public void setMaxTrailerCount ( int maxTrailerCount ) {
        this.maxTrailerCount = maxTrailerCount;
    }
    public int getMaxTrailerCount() {
        return maxTrailerCount;
    }
    public void setMaxTrailerSize ( int maxTrailerSize ) {
        this.maxTrailerSize = maxTrailerSize;
    }
    public int getMaxTrailerSize() {
        return maxTrailerSize;
    }
}
