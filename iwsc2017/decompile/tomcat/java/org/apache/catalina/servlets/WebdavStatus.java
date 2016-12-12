package org.apache.catalina.servlets;
import java.util.Hashtable;
class WebdavStatus {
    private static final Hashtable<Integer, String> mapStatusCodes;
    public static final int SC_OK = 200;
    public static final int SC_CREATED = 201;
    public static final int SC_ACCEPTED = 202;
    public static final int SC_NO_CONTENT = 204;
    public static final int SC_MOVED_PERMANENTLY = 301;
    public static final int SC_MOVED_TEMPORARILY = 302;
    public static final int SC_NOT_MODIFIED = 304;
    public static final int SC_BAD_REQUEST = 400;
    public static final int SC_UNAUTHORIZED = 401;
    public static final int SC_FORBIDDEN = 403;
    public static final int SC_NOT_FOUND = 404;
    public static final int SC_INTERNAL_SERVER_ERROR = 500;
    public static final int SC_NOT_IMPLEMENTED = 501;
    public static final int SC_BAD_GATEWAY = 502;
    public static final int SC_SERVICE_UNAVAILABLE = 503;
    public static final int SC_CONTINUE = 100;
    public static final int SC_METHOD_NOT_ALLOWED = 405;
    public static final int SC_CONFLICT = 409;
    public static final int SC_PRECONDITION_FAILED = 412;
    public static final int SC_REQUEST_TOO_LONG = 413;
    public static final int SC_UNSUPPORTED_MEDIA_TYPE = 415;
    public static final int SC_MULTI_STATUS = 207;
    public static final int SC_UNPROCESSABLE_ENTITY = 418;
    public static final int SC_INSUFFICIENT_SPACE_ON_RESOURCE = 419;
    public static final int SC_METHOD_FAILURE = 420;
    public static final int SC_LOCKED = 423;
    public static String getStatusText ( final int nHttpStatusCode ) {
        final Integer intKey = nHttpStatusCode;
        if ( !WebdavStatus.mapStatusCodes.containsKey ( intKey ) ) {
            return "";
        }
        return WebdavStatus.mapStatusCodes.get ( intKey );
    }
    private static void addStatusCodeMap ( final int nKey, final String strVal ) {
        WebdavStatus.mapStatusCodes.put ( nKey, strVal );
    }
    static {
        mapStatusCodes = new Hashtable<Integer, String>();
        addStatusCodeMap ( 200, "OK" );
        addStatusCodeMap ( 201, "Created" );
        addStatusCodeMap ( 202, "Accepted" );
        addStatusCodeMap ( 204, "No Content" );
        addStatusCodeMap ( 301, "Moved Permanently" );
        addStatusCodeMap ( 302, "Moved Temporarily" );
        addStatusCodeMap ( 304, "Not Modified" );
        addStatusCodeMap ( 400, "Bad Request" );
        addStatusCodeMap ( 401, "Unauthorized" );
        addStatusCodeMap ( 403, "Forbidden" );
        addStatusCodeMap ( 404, "Not Found" );
        addStatusCodeMap ( 500, "Internal Server Error" );
        addStatusCodeMap ( 501, "Not Implemented" );
        addStatusCodeMap ( 502, "Bad Gateway" );
        addStatusCodeMap ( 503, "Service Unavailable" );
        addStatusCodeMap ( 100, "Continue" );
        addStatusCodeMap ( 405, "Method Not Allowed" );
        addStatusCodeMap ( 409, "Conflict" );
        addStatusCodeMap ( 412, "Precondition Failed" );
        addStatusCodeMap ( 413, "Request Too Long" );
        addStatusCodeMap ( 415, "Unsupported Media Type" );
        addStatusCodeMap ( 207, "Multi-Status" );
        addStatusCodeMap ( 418, "Unprocessable Entity" );
        addStatusCodeMap ( 419, "Insufficient Space On Resource" );
        addStatusCodeMap ( 420, "Method Failure" );
        addStatusCodeMap ( 423, "Locked" );
    }
}
