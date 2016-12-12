package org.apache.catalina.session;
import org.apache.catalina.Session;
import java.security.PrivilegedExceptionAction;
private class PrivilegedStoreLoad implements PrivilegedExceptionAction<Session> {
    private String id;
    PrivilegedStoreLoad ( final String id ) {
        this.id = id;
    }
    @Override
    public Session run() throws Exception {
        return PersistentManagerBase.this.store.load ( this.id );
    }
}
