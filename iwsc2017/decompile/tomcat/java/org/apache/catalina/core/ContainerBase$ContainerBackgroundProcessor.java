package org.apache.catalina.core;
import org.apache.catalina.Loader;
import org.apache.tomcat.util.ExceptionUtils;
import org.apache.catalina.Context;
import org.apache.catalina.Container;
protected class ContainerBackgroundProcessor implements Runnable {
    @Override
    public void run() {
        Throwable t = null;
        final String unexpectedDeathMessage = ContainerBase.sm.getString ( "containerBase.backgroundProcess.unexpectedThreadDeath", Thread.currentThread().getName() );
        try {
            while ( !ContainerBase.access$100 ( ContainerBase.this ) ) {
                try {
                    Thread.sleep ( ContainerBase.this.backgroundProcessorDelay * 1000L );
                } catch ( InterruptedException ex ) {}
                if ( !ContainerBase.access$100 ( ContainerBase.this ) ) {
                    this.processChildren ( ContainerBase.this );
                }
            }
        } catch ( RuntimeException ) {}
        catch ( Error e ) {
            t = e;
            throw e;
        } finally {
            if ( !ContainerBase.access$100 ( ContainerBase.this ) ) {
                ContainerBase.access$200().error ( unexpectedDeathMessage, t );
            }
        }
    }
    protected void processChildren ( final Container container ) {
        ClassLoader originalClassLoader = null;
        try {
            if ( container instanceof Context ) {
                final Loader loader = ( ( Context ) container ).getLoader();
                if ( loader == null ) {
                    return;
                }
                originalClassLoader = ( ( Context ) container ).bind ( false, null );
            }
            container.backgroundProcess();
            final Container[] children = container.findChildren();
            for ( int i = 0; i < children.length; ++i ) {
                if ( children[i].getBackgroundProcessorDelay() <= 0 ) {
                    this.processChildren ( children[i] );
                }
            }
        } catch ( Throwable t ) {
            ExceptionUtils.handleThrowable ( t );
            ContainerBase.access$200().error ( "Exception invoking periodic operation: ", t );
        } finally {
            if ( container instanceof Context ) {
                ( ( Context ) container ).unbind ( false, originalClassLoader );
            }
        }
    }
}
