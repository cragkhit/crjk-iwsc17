package org.apache.coyote.http2;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Collection;
import java.util.HashSet;
import java.util.Enumeration;
import org.apache.coyote.http11.upgrade.InternalHttpUpgradeHandler;
import org.apache.coyote.http11.upgrade.UpgradeProcessorInternal;
import org.apache.tomcat.InstanceManager;
import org.apache.tomcat.ContextBind;
import javax.servlet.http.HttpUpgradeHandler;
import org.apache.coyote.UpgradeToken;
import org.apache.coyote.Request;
import org.apache.coyote.Processor;
import org.apache.coyote.Adapter;
import org.apache.tomcat.util.net.SocketWrapperBase;
import java.util.Map;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Set;
import org.apache.coyote.UpgradeProtocol;
public class Http2Protocol implements UpgradeProtocol {
    static final long DEFAULT_READ_TIMEOUT = 10000L;
    static final long DEFAULT_KEEP_ALIVE_TIMEOUT = -1L;
    static final long DEFAULT_WRITE_TIMEOUT = 10000L;
    static final long DEFAULT_MAX_CONCURRENT_STREAMS = 200L;
    static final int DEFAULT_MAX_CONCURRENT_STREAM_EXECUTION = 200;
    static final int DEFAULT_INITIAL_WINDOW_SIZE = 65535;
    private static final String HTTP_UPGRADE_NAME = "h2c";
    private static final String ALPN_NAME = "h2";
    private static final byte[] ALPN_IDENTIFIER;
    private long readTimeout;
    private long keepAliveTimeout;
    private long writeTimeout;
    private long maxConcurrentStreams;
    private int maxConcurrentStreamExecution;
    private int initialWindowSize;
    private Set<String> allowedTrailerHeaders;
    private int maxHeaderCount;
    private int maxHeaderSize;
    private int maxTrailerCount;
    private int maxTrailerSize;
    public Http2Protocol() {
        this.readTimeout = 10000L;
        this.keepAliveTimeout = -1L;
        this.writeTimeout = 10000L;
        this.maxConcurrentStreams = 200L;
        this.maxConcurrentStreamExecution = 200;
        this.initialWindowSize = 65535;
        this.allowedTrailerHeaders = Collections.newSetFromMap ( new ConcurrentHashMap<String, Boolean>() );
        this.maxHeaderCount = 100;
        this.maxHeaderSize = 8192;
        this.maxTrailerCount = 100;
        this.maxTrailerSize = 8192;
    }
    @Override
    public String getHttpUpgradeName ( final boolean isSSLEnabled ) {
        if ( isSSLEnabled ) {
            return null;
        }
        return "h2c";
    }
    @Override
    public byte[] getAlpnIdentifier() {
        return Http2Protocol.ALPN_IDENTIFIER;
    }
    @Override
    public String getAlpnName() {
        return "h2";
    }
    @Override
    public Processor getProcessor ( final SocketWrapperBase<?> socketWrapper, final Adapter adapter ) {
        final UpgradeProcessorInternal processor = new UpgradeProcessorInternal ( socketWrapper, new UpgradeToken ( ( HttpUpgradeHandler ) this.getInternalUpgradeHandler ( adapter, null ), null, null ) );
        return processor;
    }
    @Override
    public InternalHttpUpgradeHandler getInternalUpgradeHandler ( final Adapter adapter, final Request coyoteRequest ) {
        final Http2UpgradeHandler result = new Http2UpgradeHandler ( adapter, coyoteRequest );
        result.setReadTimeout ( this.getReadTimeout() );
        result.setKeepAliveTimeout ( this.getKeepAliveTimeout() );
        result.setWriteTimeout ( this.getWriteTimeout() );
        result.setMaxConcurrentStreams ( this.getMaxConcurrentStreams() );
        result.setMaxConcurrentStreamExecution ( this.getMaxConcurrentStreamExecution() );
        result.setInitialWindowSize ( this.getInitialWindowSize() );
        result.setAllowedTrailerHeaders ( this.allowedTrailerHeaders );
        result.setMaxHeaderCount ( this.getMaxHeaderCount() );
        result.setMaxHeaderSize ( this.getMaxHeaderSize() );
        result.setMaxTrailerCount ( this.getMaxTrailerCount() );
        result.setMaxTrailerSize ( this.getMaxTrailerSize() );
        return result;
    }
    @Override
    public boolean accept ( final Request request ) {
        final Enumeration<String> settings = request.getMimeHeaders().values ( "HTTP2-Settings" );
        int count = 0;
        while ( settings.hasMoreElements() ) {
            ++count;
            settings.nextElement();
        }
        if ( count != 1 ) {
            return false;
        }
        Enumeration<String> connection;
        boolean found;
        for ( connection = request.getMimeHeaders().values ( "Connection" ), found = false; connection.hasMoreElements() && !found; found = connection.nextElement().contains ( "HTTP2-Settings" ) ) {}
        return found;
    }
    public long getReadTimeout() {
        return this.readTimeout;
    }
    public void setReadTimeout ( final long readTimeout ) {
        this.readTimeout = readTimeout;
    }
    public long getKeepAliveTimeout() {
        return this.keepAliveTimeout;
    }
    public void setKeepAliveTimeout ( final long keepAliveTimeout ) {
        this.keepAliveTimeout = keepAliveTimeout;
    }
    public long getWriteTimeout() {
        return this.writeTimeout;
    }
    public void setWriteTimeout ( final long writeTimeout ) {
        this.writeTimeout = writeTimeout;
    }
    public long getMaxConcurrentStreams() {
        return this.maxConcurrentStreams;
    }
    public void setMaxConcurrentStreams ( final long maxConcurrentStreams ) {
        this.maxConcurrentStreams = maxConcurrentStreams;
    }
    public int getMaxConcurrentStreamExecution() {
        return this.maxConcurrentStreamExecution;
    }
    public void setMaxConcurrentStreamExecution ( final int maxConcurrentStreamExecution ) {
        this.maxConcurrentStreamExecution = maxConcurrentStreamExecution;
    }
    public int getInitialWindowSize() {
        return this.initialWindowSize;
    }
    public void setInitialWindowSize ( final int initialWindowSize ) {
        this.initialWindowSize = initialWindowSize;
    }
    public void setAllowedTrailerHeaders ( final String commaSeparatedHeaders ) {
        final Set<String> toRemove = new HashSet<String>();
        toRemove.addAll ( this.allowedTrailerHeaders );
        if ( commaSeparatedHeaders != null ) {
            final String[] split;
            final String[] headers = split = commaSeparatedHeaders.split ( "," );
            for ( final String header : split ) {
                final String trimmedHeader = header.trim().toLowerCase ( Locale.ENGLISH );
                if ( toRemove.contains ( trimmedHeader ) ) {
                    toRemove.remove ( trimmedHeader );
                } else {
                    this.allowedTrailerHeaders.add ( trimmedHeader );
                }
            }
            this.allowedTrailerHeaders.removeAll ( toRemove );
        }
    }
    public String getAllowedTrailerHeaders() {
        final List<String> copy = new ArrayList<String> ( this.allowedTrailerHeaders.size() );
        copy.addAll ( this.allowedTrailerHeaders );
        final StringBuilder result = new StringBuilder();
        boolean first = true;
        for ( final String header : copy ) {
            if ( first ) {
                first = false;
            } else {
                result.append ( ',' );
            }
            result.append ( header );
        }
        return result.toString();
    }
    public void setMaxHeaderCount ( final int maxHeaderCount ) {
        this.maxHeaderCount = maxHeaderCount;
    }
    public int getMaxHeaderCount() {
        return this.maxHeaderCount;
    }
    public void setMaxHeaderSize ( final int maxHeaderSize ) {
        this.maxHeaderSize = maxHeaderSize;
    }
    public int getMaxHeaderSize() {
        return this.maxHeaderSize;
    }
    public void setMaxTrailerCount ( final int maxTrailerCount ) {
        this.maxTrailerCount = maxTrailerCount;
    }
    public int getMaxTrailerCount() {
        return this.maxTrailerCount;
    }
    public void setMaxTrailerSize ( final int maxTrailerSize ) {
        this.maxTrailerSize = maxTrailerSize;
    }
    public int getMaxTrailerSize() {
        return this.maxTrailerSize;
    }
    static {
        ALPN_IDENTIFIER = "h2".getBytes ( StandardCharsets.UTF_8 );
    }
}
