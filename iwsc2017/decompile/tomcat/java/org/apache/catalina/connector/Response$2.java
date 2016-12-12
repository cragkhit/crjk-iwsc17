package org.apache.catalina.connector;
import org.apache.catalina.Session;
import java.security.PrivilegedAction;
class Response$2 implements PrivilegedAction<Boolean> {
    final   Request val$hreq;
    final   Session val$session;
    final   String val$location;
    @Override
    public Boolean run() {
        return Response.access$000 ( Response.this, this.val$hreq, this.val$session, this.val$location );
    }
}
