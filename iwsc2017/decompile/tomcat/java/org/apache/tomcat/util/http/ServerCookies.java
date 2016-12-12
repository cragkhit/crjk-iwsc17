package org.apache.tomcat.util.http;
import org.apache.tomcat.util.res.StringManager;
public class ServerCookies {
    private static final StringManager sm;
    private ServerCookie[] serverCookies;
    private int cookieCount;
    private int limit;
    public ServerCookies ( final int initialSize ) {
        this.cookieCount = 0;
        this.limit = 200;
        this.serverCookies = new ServerCookie[initialSize];
    }
    public ServerCookie addCookie() {
        if ( this.limit > -1 && this.cookieCount >= this.limit ) {
            throw new IllegalArgumentException ( ServerCookies.sm.getString ( "cookies.maxCountFail", this.limit ) );
        }
        if ( this.cookieCount >= this.serverCookies.length ) {
            final int newSize = Math.min ( 2 * this.cookieCount, this.limit );
            final ServerCookie[] scookiesTmp = new ServerCookie[newSize];
            System.arraycopy ( this.serverCookies, 0, scookiesTmp, 0, this.cookieCount );
            this.serverCookies = scookiesTmp;
        }
        ServerCookie c = this.serverCookies[this.cookieCount];
        if ( c == null ) {
            c = new ServerCookie();
            this.serverCookies[this.cookieCount] = c;
        }
        ++this.cookieCount;
        return c;
    }
    public ServerCookie getCookie ( final int idx ) {
        return this.serverCookies[idx];
    }
    public int getCookieCount() {
        return this.cookieCount;
    }
    public void setLimit ( final int limit ) {
        this.limit = limit;
        if ( limit > -1 && this.serverCookies.length > limit && this.cookieCount <= limit ) {
            final ServerCookie[] scookiesTmp = new ServerCookie[limit];
            System.arraycopy ( this.serverCookies, 0, scookiesTmp, 0, this.cookieCount );
            this.serverCookies = scookiesTmp;
        }
    }
    public void recycle() {
        for ( int i = 0; i < this.cookieCount; ++i ) {
            this.serverCookies[i].recycle();
        }
        this.cookieCount = 0;
    }
    static {
        sm = StringManager.getManager ( ServerCookies.class );
    }
}
