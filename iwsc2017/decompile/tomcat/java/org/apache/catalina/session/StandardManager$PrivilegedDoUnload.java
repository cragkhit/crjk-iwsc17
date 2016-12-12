package org.apache.catalina.session;
import java.security.PrivilegedExceptionAction;
private class PrivilegedDoUnload implements PrivilegedExceptionAction<Void> {
    @Override
    public Void run() throws Exception {
        StandardManager.this.doUnload();
        return null;
    }
}
