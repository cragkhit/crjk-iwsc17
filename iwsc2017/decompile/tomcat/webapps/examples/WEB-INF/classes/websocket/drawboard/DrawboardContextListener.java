// 
// Decompiled by Procyon v0.5.29
// 

package websocket.drawboard;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public final class DrawboardContextListener implements ServletContextListener
{
    public void contextDestroyed(final ServletContextEvent sce) {
        final Room room = DrawboardEndpoint.getRoom(false);
        if (room != null) {
            room.shutdown();
        }
    }
}
