package org.apache.catalina.connector;
import javax.servlet.http.Cookie;
import java.security.PrivilegedAction;
class Response$1 implements PrivilegedAction<String> {
    final   Cookie val$cookie;
    @Override
    public String run() {
        return Response.this.getContext().getCookieProcessor().generateHeader ( this.val$cookie );
    }
}
