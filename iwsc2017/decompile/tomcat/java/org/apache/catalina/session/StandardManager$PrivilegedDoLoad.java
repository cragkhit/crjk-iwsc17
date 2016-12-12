package org.apache.catalina.session;
import java.security.PrivilegedExceptionAction;
private class PrivilegedDoLoad implements PrivilegedExceptionAction<Void> {
    @Override
    public Void run() throws Exception {
        StandardManager.this.doLoad();
        return null;
    }
}
