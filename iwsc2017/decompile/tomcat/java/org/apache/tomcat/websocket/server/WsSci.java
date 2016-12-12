package org.apache.tomcat.websocket.server;
import java.util.EventListener;
import java.util.Iterator;
import javax.websocket.DeploymentException;
import java.util.Collection;
import javax.websocket.server.ServerEndpointConfig;
import javax.servlet.ServletException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Modifier;
import javax.websocket.ContainerProvider;
import java.util.HashSet;
import javax.servlet.ServletContext;
import java.util.Set;
import javax.websocket.Endpoint;
import javax.websocket.server.ServerApplicationConfig;
import javax.websocket.server.ServerEndpoint;
import javax.servlet.annotation.HandlesTypes;
import javax.servlet.ServletContainerInitializer;
@HandlesTypes ( { ServerEndpoint.class, ServerApplicationConfig.class, Endpoint.class } )
public class WsSci implements ServletContainerInitializer {
    public void onStartup ( final Set<Class<?>> clazzes, final ServletContext ctx ) throws ServletException {
        final WsServerContainer sc = init ( ctx, true );
        if ( clazzes == null || clazzes.size() == 0 ) {
            return;
        }
        final Set<ServerApplicationConfig> serverApplicationConfigs = new HashSet<ServerApplicationConfig>();
        final Set<Class<? extends Endpoint>> scannedEndpointClazzes = new HashSet<Class<? extends Endpoint>>();
        final Set<Class<?>> scannedPojoEndpoints = new HashSet<Class<?>>();
        try {
            String wsPackage = ContainerProvider.class.getName();
            wsPackage = wsPackage.substring ( 0, wsPackage.lastIndexOf ( 46 ) + 1 );
            for ( final Class<?> clazz : clazzes ) {
                final int modifiers = clazz.getModifiers();
                if ( Modifier.isPublic ( modifiers ) ) {
                    if ( Modifier.isAbstract ( modifiers ) ) {
                        continue;
                    }
                    if ( clazz.getName().startsWith ( wsPackage ) ) {
                        continue;
                    }
                    if ( ServerApplicationConfig.class.isAssignableFrom ( clazz ) ) {
                        serverApplicationConfigs.add ( ( ServerApplicationConfig ) clazz.newInstance() );
                    }
                    if ( Endpoint.class.isAssignableFrom ( clazz ) ) {
                        final Class<? extends Endpoint> endpoint = ( Class<? extends Endpoint> ) clazz;
                        scannedEndpointClazzes.add ( endpoint );
                    }
                    if ( !clazz.isAnnotationPresent ( ( Class<? extends Annotation> ) ServerEndpoint.class ) ) {
                        continue;
                    }
                    scannedPojoEndpoints.add ( clazz );
                }
            }
        } catch ( InstantiationException | IllegalAccessException e ) {
            throw new ServletException ( ( Throwable ) e );
        }
        final Set<ServerEndpointConfig> filteredEndpointConfigs = new HashSet<ServerEndpointConfig>();
        final Set<Class<?>> filteredPojoEndpoints = new HashSet<Class<?>>();
        if ( serverApplicationConfigs.isEmpty() ) {
            filteredPojoEndpoints.addAll ( scannedPojoEndpoints );
        } else {
            for ( final ServerApplicationConfig config : serverApplicationConfigs ) {
                final Set<ServerEndpointConfig> configFilteredEndpoints = ( Set<ServerEndpointConfig> ) config.getEndpointConfigs ( ( Set ) scannedEndpointClazzes );
                if ( configFilteredEndpoints != null ) {
                    filteredEndpointConfigs.addAll ( configFilteredEndpoints );
                }
                final Set<Class<?>> configFilteredPojos = ( Set<Class<?>> ) config.getAnnotatedEndpointClasses ( ( Set ) scannedPojoEndpoints );
                if ( configFilteredPojos != null ) {
                    filteredPojoEndpoints.addAll ( configFilteredPojos );
                }
            }
        }
        try {
            for ( final ServerEndpointConfig config2 : filteredEndpointConfigs ) {
                sc.addEndpoint ( config2 );
            }
            for ( final Class<?> clazz2 : filteredPojoEndpoints ) {
                sc.addEndpoint ( clazz2 );
            }
        } catch ( DeploymentException e2 ) {
            throw new ServletException ( ( Throwable ) e2 );
        }
    }
    static WsServerContainer init ( final ServletContext servletContext, final boolean initBySciMechanism ) {
        final WsServerContainer sc = new WsServerContainer ( servletContext );
        servletContext.setAttribute ( "javax.websocket.server.ServerContainer", ( Object ) sc );
        servletContext.addListener ( ( EventListener ) new WsSessionListener ( sc ) );
        if ( initBySciMechanism ) {
            servletContext.addListener ( ( EventListener ) new WsContextListener() );
        }
        return sc;
    }
}
