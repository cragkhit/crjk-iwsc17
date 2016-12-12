// 
// Decompiled by Procyon v0.5.29
// 

package websocket;

import java.util.Iterator;
import websocket.drawboard.DrawboardEndpoint;
import websocket.echo.EchoEndpoint;
import java.util.HashSet;
import javax.websocket.server.ServerEndpointConfig;
import javax.websocket.Endpoint;
import java.util.Set;
import javax.websocket.server.ServerApplicationConfig;

public class ExamplesConfig implements ServerApplicationConfig
{
    public Set<ServerEndpointConfig> getEndpointConfigs(final Set<Class<? extends Endpoint>> scanned) {
        final Set<ServerEndpointConfig> result = new HashSet<ServerEndpointConfig>();
        if (scanned.contains(EchoEndpoint.class)) {
            result.add(ServerEndpointConfig.Builder.create((Class)EchoEndpoint.class, "/websocket/echoProgrammatic").build());
        }
        if (scanned.contains(DrawboardEndpoint.class)) {
            result.add(ServerEndpointConfig.Builder.create((Class)DrawboardEndpoint.class, "/websocket/drawboard").build());
        }
        return result;
    }
    
    public Set<Class<?>> getAnnotatedEndpointClasses(final Set<Class<?>> scanned) {
        final Set<Class<?>> results = new HashSet<Class<?>>();
        for (final Class<?> clazz : scanned) {
            if (clazz.getPackage().getName().startsWith("websocket.")) {
                results.add(clazz);
            }
        }
        return results;
    }
}
