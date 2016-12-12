package org.apache.catalina.session;
import java.security.PrivilegedExceptionAction;
private class PrivilegedStoreClear implements PrivilegedExceptionAction<Void> {
    @Override
    public Void run() throws Exception {
        PersistentManagerBase.this.store.clear();
        return null;
    }
}
