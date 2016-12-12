package org.apache.tomcat.websocket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.websocket.Extension;
public class Constants {
    public static final byte OPCODE_CONTINUATION = 0x00;
    public static final byte OPCODE_TEXT = 0x01;
    public static final byte OPCODE_BINARY = 0x02;
    public static final byte OPCODE_CLOSE = 0x08;
    public static final byte OPCODE_PING = 0x09;
    public static final byte OPCODE_PONG = 0x0A;
    static final byte INTERNAL_OPCODE_FLUSH = 0x18;
    static final int DEFAULT_BUFFER_SIZE = Integer.getInteger (
            "org.apache.tomcat.websocket.DEFAULT_BUFFER_SIZE", 8 * 1024 )
                                           .intValue();
    public static final String SSL_PROTOCOLS_PROPERTY =
        "org.apache.tomcat.websocket.SSL_PROTOCOLS";
    public static final String SSL_TRUSTSTORE_PROPERTY =
        "org.apache.tomcat.websocket.SSL_TRUSTSTORE";
    public static final String SSL_TRUSTSTORE_PWD_PROPERTY =
        "org.apache.tomcat.websocket.SSL_TRUSTSTORE_PWD";
    public static final String SSL_TRUSTSTORE_PWD_DEFAULT = "changeit";
    public static final String SSL_CONTEXT_PROPERTY =
        "org.apache.tomcat.websocket.SSL_CONTEXT";
    public static final String IO_TIMEOUT_MS_PROPERTY =
        "org.apache.tomcat.websocket.IO_TIMEOUT_MS";
    public static final long IO_TIMEOUT_MS_DEFAULT = 5000;
    public static final String HOST_HEADER_NAME = "Host";
    public static final String UPGRADE_HEADER_NAME = "Upgrade";
    public static final String UPGRADE_HEADER_VALUE = "websocket";
    public static final String ORIGIN_HEADER_NAME = "Origin";
    public static final String CONNECTION_HEADER_NAME = "Connection";
    public static final String CONNECTION_HEADER_VALUE = "upgrade";
    public static final String WS_VERSION_HEADER_NAME = "Sec-WebSocket-Version";
    public static final String WS_VERSION_HEADER_VALUE = "13";
    public static final String WS_KEY_HEADER_NAME = "Sec-WebSocket-Key";
    public static final String WS_PROTOCOL_HEADER_NAME = "Sec-WebSocket-Protocol";
    public static final String WS_EXTENSIONS_HEADER_NAME = "Sec-WebSocket-Extensions";
    static final String DEFAULT_ORIGIN_HEADER_VALUE =
        System.getProperty ( "org.apache.tomcat.websocket.DEFAULT_ORIGIN_HEADER_VALUE" );
    public static final String BLOCKING_SEND_TIMEOUT_PROPERTY =
        "org.apache.tomcat.websocket.BLOCKING_SEND_TIMEOUT";
    public static final long DEFAULT_BLOCKING_SEND_TIMEOUT = 20 * 1000;
    static final int DEFAULT_PROCESS_PERIOD = Integer.getInteger (
                "org.apache.tomcat.websocket.DEFAULT_PROCESS_PERIOD", 10 )
            .intValue();
    static final boolean DISABLE_BUILTIN_EXTENSIONS =
        Boolean.getBoolean ( "org.apache.tomcat.websocket.DISABLE_BUILTIN_EXTENSIONS" );
    static final boolean ALLOW_UNSUPPORTED_EXTENSIONS =
        Boolean.getBoolean ( "org.apache.tomcat.websocket.ALLOW_UNSUPPORTED_EXTENSIONS" );
    static final boolean STREAMS_DROP_EMPTY_MESSAGES =
        Boolean.getBoolean ( "org.apache.tomcat.websocket.STREAMS_DROP_EMPTY_MESSAGES" );
    public static final boolean STRICT_SPEC_COMPLIANCE =
        Boolean.getBoolean (
            "org.apache.tomcat.websocket.STRICT_SPEC_COMPLIANCE" );
    public static final List<Extension> INSTALLED_EXTENSIONS;
    static {
        if ( DISABLE_BUILTIN_EXTENSIONS ) {
            INSTALLED_EXTENSIONS = Collections.unmodifiableList ( new ArrayList<Extension>() );
        } else {
            List<Extension> installed = new ArrayList<> ( 1 );
            installed.add ( new WsExtension ( "permessage-deflate" ) );
            INSTALLED_EXTENSIONS = Collections.unmodifiableList ( installed );
        }
    }
    private Constants() {
    }
}
