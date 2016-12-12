package org.apache.catalina.session;
import java.security.PrivilegedExceptionAction;
private class PrivilegedStoreRemove implements PrivilegedExceptionAction<Void> {
    private String id;
    PrivilegedStoreRemove ( final String id ) {
        this.id = id;
    }
    @Override
    public Void run() throws Exception {
        PersistentManagerBase.this.store.remove ( this.id );
        return null;
    }
}
