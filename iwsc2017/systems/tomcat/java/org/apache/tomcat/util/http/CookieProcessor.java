package org.apache.tomcat.util.http;
import java.nio.charset.Charset;
import javax.servlet.http.Cookie;
public interface CookieProcessor {
    void parseCookieHeader ( MimeHeaders headers, ServerCookies serverCookies );
    String generateHeader ( Cookie cookie );
    Charset getCharset();
}
