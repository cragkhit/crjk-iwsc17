package org.apache.tomcat.util.http;
import org.apache.tomcat.util.res.StringManager;
public class ServerCookies {
    private static final StringManager sm = StringManager.getManager ( ServerCookies.class );
    private ServerCookie[] serverCookies;
    private int cookieCount = 0;
    private int limit = 200;
    public ServerCookies ( int initialSize ) {
        serverCookies = new ServerCookie[initialSize];
    }
    public ServerCookie addCookie() {
        if ( limit > -1 && cookieCount >= limit ) {
            throw new IllegalArgumentException (
                sm.getString ( "cookies.maxCountFail", Integer.valueOf ( limit ) ) );
        }
        if ( cookieCount >= serverCookies.length ) {
            int newSize = Math.min ( 2 * cookieCount, limit );
            ServerCookie scookiesTmp[] = new ServerCookie[newSize];
            System.arraycopy ( serverCookies, 0, scookiesTmp, 0, cookieCount );
            serverCookies = scookiesTmp;
        }
        ServerCookie c = serverCookies[cookieCount];
        if ( c == null ) {
            c = new ServerCookie();
            serverCookies[cookieCount] = c;
        }
        cookieCount++;
        return c;
    }
    public ServerCookie getCookie ( int idx ) {
        return serverCookies[idx];
    }
    public int getCookieCount() {
        return cookieCount;
    }
    public void setLimit ( int limit ) {
        this.limit = limit;
        if ( limit > -1 && serverCookies.length > limit && cookieCount <= limit ) {
            ServerCookie scookiesTmp[] = new ServerCookie[limit];
            System.arraycopy ( serverCookies, 0, scookiesTmp, 0, cookieCount );
            serverCookies = scookiesTmp;
        }
    }
    public void recycle() {
        for ( int i = 0; i < cookieCount; i++ ) {
            serverCookies[i].recycle();
        }
        cookieCount = 0;
    }
}
