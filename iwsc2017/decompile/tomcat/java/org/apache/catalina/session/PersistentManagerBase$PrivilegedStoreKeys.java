package org.apache.catalina.session;
import java.security.PrivilegedExceptionAction;
private class PrivilegedStoreKeys implements PrivilegedExceptionAction<String[]> {
    @Override
    public String[] run() throws Exception {
        return PersistentManagerBase.this.store.keys();
    }
}
