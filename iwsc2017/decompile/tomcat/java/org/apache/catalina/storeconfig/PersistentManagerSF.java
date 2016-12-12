package org.apache.catalina.storeconfig;
import org.apache.catalina.SessionIdGenerator;
import org.apache.catalina.Store;
import org.apache.catalina.session.PersistentManager;
import java.io.PrintWriter;
public class PersistentManagerSF extends StoreFactoryBase {
    @Override
    public void storeChildren ( final PrintWriter aWriter, final int indent, final Object aManager, final StoreDescription parentDesc ) throws Exception {
        if ( aManager instanceof PersistentManager ) {
            final PersistentManager manager = ( PersistentManager ) aManager;
            final Store store = manager.getStore();
            this.storeElement ( aWriter, indent, store );
            final SessionIdGenerator sessionIdGenerator = manager.getSessionIdGenerator();
            if ( sessionIdGenerator != null ) {
                this.storeElement ( aWriter, indent, sessionIdGenerator );
            }
        }
    }
}
