package org.apache.catalina.core;
import org.apache.catalina.Context;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleListener;
private class MemoryLeakTrackingListener implements LifecycleListener {
    @Override
    public void lifecycleEvent ( final LifecycleEvent event ) {
        if ( event.getType().equals ( "after_start" ) && event.getSource() instanceof Context ) {
            final Context context = ( Context ) event.getSource();
            StandardHost.access$100 ( StandardHost.this ).put ( context.getLoader().getClassLoader(), context.getServletContext().getContextPath() );
        }
    }
}
