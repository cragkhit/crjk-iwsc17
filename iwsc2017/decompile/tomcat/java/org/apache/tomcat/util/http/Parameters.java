package org.apache.tomcat.util.http;
import java.nio.charset.StandardCharsets;
import org.apache.juli.logging.LogFactory;
import java.util.Iterator;
import java.io.UnsupportedEncodingException;
import org.apache.tomcat.util.buf.B2CConverter;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.nio.charset.Charset;
import org.apache.tomcat.util.buf.ByteChunk;
import org.apache.tomcat.util.buf.UDecoder;
import org.apache.tomcat.util.buf.MessageBytes;
import java.util.ArrayList;
import java.util.Map;
import org.apache.tomcat.util.res.StringManager;
import org.apache.tomcat.util.log.UserDataHelper;
import org.apache.juli.logging.Log;
public final class Parameters {
    private static final Log log;
    private static final UserDataHelper userDataLog;
    private static final UserDataHelper maxParamCountLog;
    private static final StringManager sm;
    private final Map<String, ArrayList<String>> paramHashValues;
    private boolean didQueryParameters;
    private MessageBytes queryMB;
    private UDecoder urlDec;
    private final MessageBytes decodedQuery;
    private String encoding;
    private String queryStringEncoding;
    private int limit;
    private int parameterCount;
    private FailReason parseFailedReason;
    private final ByteChunk tmpName;
    private final ByteChunk tmpValue;
    private final ByteChunk origName;
    private final ByteChunk origValue;
    public static final String DEFAULT_ENCODING = "ISO-8859-1";
    private static final Charset DEFAULT_CHARSET;
    public Parameters() {
        this.paramHashValues = new LinkedHashMap<String, ArrayList<String>>();
        this.didQueryParameters = false;
        this.decodedQuery = MessageBytes.newInstance();
        this.encoding = null;
        this.queryStringEncoding = null;
        this.limit = -1;
        this.parameterCount = 0;
        this.parseFailedReason = null;
        this.tmpName = new ByteChunk();
        this.tmpValue = new ByteChunk();
        this.origName = new ByteChunk();
        this.origValue = new ByteChunk();
    }
    public void setQuery ( final MessageBytes queryMB ) {
        this.queryMB = queryMB;
    }
    public void setLimit ( final int limit ) {
        this.limit = limit;
    }
    public String getEncoding() {
        return this.encoding;
    }
    public void setEncoding ( final String s ) {
        this.encoding = s;
        if ( Parameters.log.isDebugEnabled() ) {
            Parameters.log.debug ( "Set encoding to " + s );
        }
    }
    public void setQueryStringEncoding ( final String s ) {
        this.queryStringEncoding = s;
        if ( Parameters.log.isDebugEnabled() ) {
            Parameters.log.debug ( "Set query string encoding to " + s );
        }
    }
    public boolean isParseFailed() {
        return this.parseFailedReason != null;
    }
    public FailReason getParseFailedReason() {
        return this.parseFailedReason;
    }
    public void setParseFailedReason ( final FailReason failReason ) {
        if ( this.parseFailedReason == null ) {
            this.parseFailedReason = failReason;
        }
    }
    public void recycle() {
        this.parameterCount = 0;
        this.paramHashValues.clear();
        this.didQueryParameters = false;
        this.encoding = null;
        this.decodedQuery.recycle();
        this.parseFailedReason = null;
    }
    public String[] getParameterValues ( final String name ) {
        this.handleQueryParameters();
        final ArrayList<String> values = this.paramHashValues.get ( name );
        if ( values == null ) {
            return null;
        }
        return values.toArray ( new String[values.size()] );
    }
    public Enumeration<String> getParameterNames() {
        this.handleQueryParameters();
        return Collections.enumeration ( this.paramHashValues.keySet() );
    }
    public String getParameter ( final String name ) {
        this.handleQueryParameters();
        final ArrayList<String> values = this.paramHashValues.get ( name );
        if ( values == null ) {
            return null;
        }
        if ( values.size() == 0 ) {
            return "";
        }
        return values.get ( 0 );
    }
    public void handleQueryParameters() {
        if ( this.didQueryParameters ) {
            return;
        }
        this.didQueryParameters = true;
        if ( this.queryMB == null || this.queryMB.isNull() ) {
            return;
        }
        if ( Parameters.log.isDebugEnabled() ) {
            Parameters.log.debug ( "Decoding query " + this.decodedQuery + " " + this.queryStringEncoding );
        }
        try {
            this.decodedQuery.duplicate ( this.queryMB );
        } catch ( IOException e ) {
            e.printStackTrace();
        }
        this.processParameters ( this.decodedQuery, this.queryStringEncoding );
    }
    public void addParameter ( final String key, final String value ) throws IllegalStateException {
        if ( key == null ) {
            return;
        }
        ++this.parameterCount;
        if ( this.limit > -1 && this.parameterCount > this.limit ) {
            this.setParseFailedReason ( FailReason.TOO_MANY_PARAMETERS );
            throw new IllegalStateException ( Parameters.sm.getString ( "parameters.maxCountFail", this.limit ) );
        }
        ArrayList<String> values = this.paramHashValues.get ( key );
        if ( values == null ) {
            values = new ArrayList<String> ( 1 );
            this.paramHashValues.put ( key, values );
        }
        values.add ( value );
    }
    public void setURLDecoder ( final UDecoder u ) {
        this.urlDec = u;
    }
    public void processParameters ( final byte[] bytes, final int start, final int len ) {
        this.processParameters ( bytes, start, len, this.getCharset ( this.encoding ) );
    }
    private void processParameters ( final byte[] bytes, final int start, final int len, final Charset charset ) {
        if ( Parameters.log.isDebugEnabled() ) {
            Parameters.log.debug ( Parameters.sm.getString ( "parameters.bytes", new String ( bytes, start, len, Parameters.DEFAULT_CHARSET ) ) );
        }
        int decodeFailCount = 0;
        int pos = start;
        final int end = start + len;
        while ( pos < end ) {
            final int nameStart = pos;
            int nameEnd = -1;
            int valueStart = -1;
            int valueEnd = -1;
            boolean parsingName = true;
            boolean decodeName = false;
            boolean decodeValue = false;
            boolean parameterComplete = false;
            do {
                switch ( bytes[pos] ) {
                case 61: {
                    if ( parsingName ) {
                        nameEnd = pos;
                        parsingName = false;
                        valueStart = ++pos;
                        continue;
                    }
                    ++pos;
                    continue;
                }
                case 38: {
                    if ( parsingName ) {
                        nameEnd = pos;
                    } else {
                        valueEnd = pos;
                    }
                    parameterComplete = true;
                    ++pos;
                    continue;
                }
                case 37:
                case 43: {
                    if ( parsingName ) {
                        decodeName = true;
                    } else {
                        decodeValue = true;
                    }
                    ++pos;
                    continue;
                }
                default: {
                    ++pos;
                    continue;
                }
                }
            } while ( !parameterComplete && pos < end );
            if ( pos == end ) {
                if ( nameEnd == -1 ) {
                    nameEnd = pos;
                } else if ( valueStart > -1 && valueEnd == -1 ) {
                    valueEnd = pos;
                }
            }
            if ( Parameters.log.isDebugEnabled() && valueStart == -1 ) {
                Parameters.log.debug ( Parameters.sm.getString ( "parameters.noequal", nameStart, nameEnd, new String ( bytes, nameStart, nameEnd - nameStart, Parameters.DEFAULT_CHARSET ) ) );
            }
            if ( nameEnd <= nameStart ) {
                if ( valueStart == -1 ) {
                    if ( !Parameters.log.isDebugEnabled() ) {
                        continue;
                    }
                    Parameters.log.debug ( Parameters.sm.getString ( "parameters.emptyChunk" ) );
                } else {
                    final UserDataHelper.Mode logMode = Parameters.userDataLog.getNextMode();
                    if ( logMode != null ) {
                        String extract;
                        if ( valueEnd > nameStart ) {
                            extract = new String ( bytes, nameStart, valueEnd - nameStart, Parameters.DEFAULT_CHARSET );
                        } else {
                            extract = "";
                        }
                        String message = Parameters.sm.getString ( "parameters.invalidChunk", nameStart, valueEnd, extract );
                        switch ( logMode ) {
                        case INFO_THEN_DEBUG: {
                            message += Parameters.sm.getString ( "parameters.fallToDebug" );
                        }
                        case INFO: {
                            Parameters.log.info ( message );
                            break;
                        }
                        case DEBUG: {
                            Parameters.log.debug ( message );
                            break;
                        }
                        }
                    }
                    this.setParseFailedReason ( FailReason.NO_NAME );
                }
            } else {
                this.tmpName.setBytes ( bytes, nameStart, nameEnd - nameStart );
                if ( valueStart >= 0 ) {
                    this.tmpValue.setBytes ( bytes, valueStart, valueEnd - valueStart );
                } else {
                    this.tmpValue.setBytes ( bytes, 0, 0 );
                }
                if ( Parameters.log.isDebugEnabled() ) {
                    try {
                        this.origName.append ( bytes, nameStart, nameEnd - nameStart );
                        if ( valueStart >= 0 ) {
                            this.origValue.append ( bytes, valueStart, valueEnd - valueStart );
                        } else {
                            this.origValue.append ( bytes, 0, 0 );
                        }
                    } catch ( IOException ioe ) {
                        Parameters.log.error ( Parameters.sm.getString ( "parameters.copyFail" ), ioe );
                    }
                }
                try {
                    if ( decodeName ) {
                        this.urlDecode ( this.tmpName );
                    }
                    this.tmpName.setCharset ( charset );
                    final String name = this.tmpName.toString();
                    String value;
                    if ( valueStart >= 0 ) {
                        if ( decodeValue ) {
                            this.urlDecode ( this.tmpValue );
                        }
                        this.tmpValue.setCharset ( charset );
                        value = this.tmpValue.toString();
                    } else {
                        value = "";
                    }
                    try {
                        this.addParameter ( name, value );
                    } catch ( IllegalStateException ise ) {
                        final UserDataHelper.Mode logMode2 = Parameters.maxParamCountLog.getNextMode();
                        if ( logMode2 != null ) {
                            String message2 = ise.getMessage();
                            switch ( logMode2 ) {
                            case INFO_THEN_DEBUG: {
                                message2 += Parameters.sm.getString ( "parameters.maxCountFail.fallToDebug" );
                            }
                            case INFO: {
                                Parameters.log.info ( message2 );
                                break;
                            }
                            case DEBUG: {
                                Parameters.log.debug ( message2 );
                                break;
                            }
                            }
                        }
                        break;
                    }
                } catch ( IOException e ) {
                    this.setParseFailedReason ( FailReason.URL_DECODING );
                    if ( ++decodeFailCount == 1 || Parameters.log.isDebugEnabled() ) {
                        if ( Parameters.log.isDebugEnabled() ) {
                            Parameters.log.debug ( Parameters.sm.getString ( "parameters.decodeFail.debug", this.origName.toString(), this.origValue.toString() ), e );
                        } else if ( Parameters.log.isInfoEnabled() ) {
                            final UserDataHelper.Mode logMode3 = Parameters.userDataLog.getNextMode();
                            if ( logMode3 != null ) {
                                String message = Parameters.sm.getString ( "parameters.decodeFail.info", this.tmpName.toString(), this.tmpValue.toString() );
                                switch ( logMode3 ) {
                                case DEBUG: {
                                    Parameters.log.debug ( message );
                                    break;
                                }
                                case INFO_THEN_DEBUG: {
                                    message += Parameters.sm.getString ( "parameters.fallToDebug" );
                                }
                                case INFO: {
                                    Parameters.log.info ( message );
                                    break;
                                }
                                }
                            }
                        }
                    }
                }
                this.tmpName.recycle();
                this.tmpValue.recycle();
                if ( !Parameters.log.isDebugEnabled() ) {
                    continue;
                }
                this.origName.recycle();
                this.origValue.recycle();
            }
        }
        if ( decodeFailCount > 1 && !Parameters.log.isDebugEnabled() ) {
            final UserDataHelper.Mode logMode4 = Parameters.userDataLog.getNextMode();
            if ( logMode4 != null ) {
                String message3 = Parameters.sm.getString ( "parameters.multipleDecodingFail", decodeFailCount );
                switch ( logMode4 ) {
                case INFO_THEN_DEBUG: {
                    message3 += Parameters.sm.getString ( "parameters.fallToDebug" );
                }
                case INFO: {
                    Parameters.log.info ( message3 );
                    break;
                }
                case DEBUG: {
                    Parameters.log.debug ( message3 );
                    break;
                }
                }
            }
        }
    }
    private void urlDecode ( final ByteChunk bc ) throws IOException {
        if ( this.urlDec == null ) {
            this.urlDec = new UDecoder();
        }
        this.urlDec.convert ( bc, true );
    }
    public void processParameters ( final MessageBytes data, final String encoding ) {
        if ( data == null || data.isNull() || data.getLength() <= 0 ) {
            return;
        }
        if ( data.getType() != 2 ) {
            data.toBytes();
        }
        final ByteChunk bc = data.getByteChunk();
        this.processParameters ( bc.getBytes(), bc.getOffset(), bc.getLength(), this.getCharset ( encoding ) );
    }
    private Charset getCharset ( final String encoding ) {
        if ( encoding == null ) {
            return Parameters.DEFAULT_CHARSET;
        }
        try {
            return B2CConverter.getCharset ( encoding );
        } catch ( UnsupportedEncodingException e ) {
            return Parameters.DEFAULT_CHARSET;
        }
    }
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        for ( final Map.Entry<String, ArrayList<String>> e : this.paramHashValues.entrySet() ) {
            sb.append ( e.getKey() ).append ( '=' );
            final ArrayList<String> values = e.getValue();
            for ( final String value : values ) {
                sb.append ( value ).append ( ',' );
            }
            sb.append ( '\n' );
        }
        return sb.toString();
    }
    static {
        log = LogFactory.getLog ( Parameters.class );
        userDataLog = new UserDataHelper ( Parameters.log );
        maxParamCountLog = new UserDataHelper ( Parameters.log );
        sm = StringManager.getManager ( "org.apache.tomcat.util.http" );
        DEFAULT_CHARSET = StandardCharsets.ISO_8859_1;
    }
    public enum FailReason {
        CLIENT_DISCONNECT,
        MULTIPART_CONFIG_INVALID,
        INVALID_CONTENT_TYPE,
        IO_ERROR,
        NO_NAME,
        POST_TOO_LARGE,
        REQUEST_BODY_INCOMPLETE,
        TOO_MANY_PARAMETERS,
        UNKNOWN,
        URL_DECODING;
    }
}
