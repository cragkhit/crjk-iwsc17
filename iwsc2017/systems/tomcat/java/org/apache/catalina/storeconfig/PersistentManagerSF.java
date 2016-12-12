package org.apache.catalina.storeconfig;
import java.io.PrintWriter;
import org.apache.catalina.SessionIdGenerator;
import org.apache.catalina.Store;
import org.apache.catalina.session.PersistentManager;
public class PersistentManagerSF extends StoreFactoryBase {
    @Override
    public void storeChildren ( PrintWriter aWriter, int indent, Object aManager,
                                StoreDescription parentDesc ) throws Exception {
        if ( aManager instanceof PersistentManager ) {
            PersistentManager manager = ( PersistentManager ) aManager;
            Store store = manager.getStore();
            storeElement ( aWriter, indent, store );
            SessionIdGenerator sessionIdGenerator = manager.getSessionIdGenerator();
            if ( sessionIdGenerator != null ) {
                storeElement ( aWriter, indent, sessionIdGenerator );
            }
        }
    }
}
