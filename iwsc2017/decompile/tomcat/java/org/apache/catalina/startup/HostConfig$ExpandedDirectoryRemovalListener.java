package org.apache.catalina.startup;
import org.apache.catalina.Context;
import org.apache.catalina.LifecycleEvent;
import java.io.File;
import org.apache.catalina.LifecycleListener;
private static class ExpandedDirectoryRemovalListener implements LifecycleListener {
    private final File toDelete;
    private final String newDocBase;
    public ExpandedDirectoryRemovalListener ( final File toDelete, final String newDocBase ) {
        this.toDelete = toDelete;
        this.newDocBase = newDocBase;
    }
    @Override
    public void lifecycleEvent ( final LifecycleEvent event ) {
        if ( "after_stop".equals ( event.getType() ) ) {
            final Context context = ( Context ) event.getLifecycle();
            ExpandWar.delete ( this.toDelete );
            context.setDocBase ( this.newDocBase );
            context.removeLifecycleListener ( this );
        }
    }
}
