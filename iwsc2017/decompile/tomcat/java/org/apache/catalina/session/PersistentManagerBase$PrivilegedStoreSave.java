package org.apache.catalina.session;
import org.apache.catalina.Session;
import java.security.PrivilegedExceptionAction;
private class PrivilegedStoreSave implements PrivilegedExceptionAction<Void> {
    private Session session;
    PrivilegedStoreSave ( final Session session ) {
        this.session = session;
    }
    @Override
    public Void run() throws Exception {
        PersistentManagerBase.this.store.save ( this.session );
        return null;
    }
}
