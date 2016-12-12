package org.apache.tomcat.util.http;
import java.nio.charset.Charset;
import javax.servlet.http.Cookie;
public interface CookieProcessor {
    void parseCookieHeader ( MimeHeaders p0, ServerCookies p1 );
    String generateHeader ( Cookie p0 );
    Charset getCharset();
}
