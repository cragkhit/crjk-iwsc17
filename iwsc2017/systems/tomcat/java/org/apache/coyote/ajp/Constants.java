package org.apache.coyote.ajp;
import java.util.Hashtable;
public final class Constants {
    public static final int DEFAULT_CONNECTION_TIMEOUT = -1;
    public static final byte JK_AJP13_FORWARD_REQUEST   = 2;
    public static final byte JK_AJP13_SHUTDOWN          = 7;
    public static final byte JK_AJP13_PING_REQUEST      = 8;
    public static final byte JK_AJP13_CPING_REQUEST     = 10;
    public static final byte JK_AJP13_SEND_BODY_CHUNK   = 3;
    public static final byte JK_AJP13_SEND_HEADERS      = 4;
    public static final byte JK_AJP13_END_RESPONSE      = 5;
    public static final byte JK_AJP13_GET_BODY_CHUNK    = 6;
    public static final byte JK_AJP13_CPONG_REPLY       = 9;
    public static final int SC_RESP_CONTENT_TYPE        = 0xA001;
    public static final int SC_RESP_CONTENT_LANGUAGE    = 0xA002;
    public static final int SC_RESP_CONTENT_LENGTH      = 0xA003;
    public static final int SC_RESP_DATE                = 0xA004;
    public static final int SC_RESP_LAST_MODIFIED       = 0xA005;
    public static final int SC_RESP_LOCATION            = 0xA006;
    public static final int SC_RESP_SET_COOKIE          = 0xA007;
    public static final int SC_RESP_SET_COOKIE2         = 0xA008;
    public static final int SC_RESP_SERVLET_ENGINE      = 0xA009;
    public static final int SC_RESP_STATUS              = 0xA00A;
    public static final int SC_RESP_WWW_AUTHENTICATE    = 0xA00B;
    public static final int SC_RESP_AJP13_MAX           = 11;
    public static final byte SC_A_CONTEXT       = 1;
    public static final byte SC_A_SERVLET_PATH  = 2;
    public static final byte SC_A_REMOTE_USER   = 3;
    public static final byte SC_A_AUTH_TYPE     = 4;
    public static final byte SC_A_QUERY_STRING  = 5;
    public static final byte SC_A_JVM_ROUTE     = 6;
    public static final byte SC_A_SSL_CERT      = 7;
    public static final byte SC_A_SSL_CIPHER    = 8;
    public static final byte SC_A_SSL_SESSION   = 9;
    public static final byte SC_A_SSL_KEY_SIZE  = 11;
    public static final byte SC_A_SECRET        = 12;
    public static final byte SC_A_STORED_METHOD = 13;
    public static final byte SC_A_REQ_ATTRIBUTE = 10;
    public static final String SC_A_REQ_LOCAL_ADDR  = "AJP_LOCAL_ADDR";
    public static final String SC_A_REQ_REMOTE_PORT = "AJP_REMOTE_PORT";
    public static final String SC_A_SSL_PROTOCOL    = "AJP_SSL_PROTOCOL";
    public static final byte SC_A_ARE_DONE      = ( byte ) 0xFF;
    public static final int MAX_PACKET_SIZE = 8192;
    public static final int H_SIZE = 4;
    public static final int  READ_HEAD_LEN = 6;
    public static final int  SEND_HEAD_LEN = 8;
    public static final int  MAX_READ_SIZE = MAX_PACKET_SIZE - READ_HEAD_LEN;
    public static final int  MAX_SEND_SIZE = MAX_PACKET_SIZE - SEND_HEAD_LEN;
    private static final String [] methodTransArray = {
        "OPTIONS",
        "GET",
        "HEAD",
        "POST",
        "PUT",
        "DELETE",
        "TRACE",
        "PROPFIND",
        "PROPPATCH",
        "MKCOL",
        "COPY",
        "MOVE",
        "LOCK",
        "UNLOCK",
        "ACL",
        "REPORT",
        "VERSION-CONTROL",
        "CHECKIN",
        "CHECKOUT",
        "UNCHECKOUT",
        "SEARCH",
        "MKWORKSPACE",
        "UPDATE",
        "LABEL",
        "MERGE",
        "BASELINE-CONTROL",
        "MKACTIVITY"
    };
    public static final String getMethodForCode ( final int code ) {
        return methodTransArray[code];
    }
    public static final int SC_M_JK_STORED = ( byte ) 0xFF;
    public static final int SC_REQ_ACCEPT          = 1;
    public static final int SC_REQ_ACCEPT_CHARSET  = 2;
    public static final int SC_REQ_ACCEPT_ENCODING = 3;
    public static final int SC_REQ_ACCEPT_LANGUAGE = 4;
    public static final int SC_REQ_AUTHORIZATION   = 5;
    public static final int SC_REQ_CONNECTION      = 6;
    public static final int SC_REQ_CONTENT_TYPE    = 7;
    public static final int SC_REQ_CONTENT_LENGTH  = 8;
    public static final int SC_REQ_COOKIE          = 9;
    public static final int SC_REQ_COOKIE2         = 10;
    public static final int SC_REQ_HOST            = 11;
    public static final int SC_REQ_PRAGMA          = 12;
    public static final int SC_REQ_REFERER         = 13;
    public static final int SC_REQ_USER_AGENT      = 14;
    private static final String [] headerTransArray = {
        "accept",
        "accept-charset",
        "accept-encoding",
        "accept-language",
        "authorization",
        "connection",
        "content-type",
        "content-length",
        "cookie",
        "cookie2",
        "host",
        "pragma",
        "referer",
        "user-agent"
    };
    public static final String getHeaderForCode ( final int code ) {
        return headerTransArray[code];
    }
    private static final String [] responseTransArray = {
        "Content-Type",
        "Content-Language",
        "Content-Length",
        "Date",
        "Last-Modified",
        "Location",
        "Set-Cookie",
        "Set-Cookie2",
        "Servlet-Engine",
        "Status",
        "WWW-Authenticate"
    };
    public static final String getResponseHeaderForCode ( final int code ) {
        return responseTransArray[code];
    }
    private static final Hashtable<String, Integer>  responseTransHash =
        new Hashtable<> ( 20 );
    static {
        try {
            int i;
            for ( i = 0; i < SC_RESP_AJP13_MAX; i++ ) {
                responseTransHash.put ( getResponseHeaderForCode ( i ),
                                        Integer.valueOf ( 0xA001 + i ) );
            }
        } catch ( Exception e ) {
        }
    }
    public static final int getResponseAjpIndex ( String header ) {
        Integer i = responseTransHash.get ( header );
        if ( i == null ) {
            return 0;
        } else {
            return i.intValue();
        }
    }
}
