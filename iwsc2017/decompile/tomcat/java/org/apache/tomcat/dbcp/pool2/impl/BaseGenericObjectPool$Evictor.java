package org.apache.tomcat.dbcp.pool2.impl;
import java.util.TimerTask;
class Evictor extends TimerTask {
    @Override
    public void run() {
        final ClassLoader savedClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            if ( BaseGenericObjectPool.access$000 ( BaseGenericObjectPool.this ) != null ) {
                final ClassLoader cl = ( ClassLoader ) BaseGenericObjectPool.access$000 ( BaseGenericObjectPool.this ).get();
                if ( cl == null ) {
                    this.cancel();
                    return;
                }
                Thread.currentThread().setContextClassLoader ( cl );
            }
            try {
                BaseGenericObjectPool.this.evict();
            } catch ( Exception e ) {
                BaseGenericObjectPool.this.swallowException ( e );
            } catch ( OutOfMemoryError oome ) {
                oome.printStackTrace ( System.err );
            }
            try {
                BaseGenericObjectPool.this.ensureMinIdle();
            } catch ( Exception e ) {
                BaseGenericObjectPool.this.swallowException ( e );
            }
        } finally {
            Thread.currentThread().setContextClassLoader ( savedClassLoader );
        }
    }
}
