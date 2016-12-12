package org.apache.tomcat.websocket.server;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;
import javax.servlet.ServletContainerInitializer;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.HandlesTypes;
import javax.websocket.ContainerProvider;
import javax.websocket.DeploymentException;
import javax.websocket.Endpoint;
import javax.websocket.server.ServerApplicationConfig;
import javax.websocket.server.ServerEndpoint;
import javax.websocket.server.ServerEndpointConfig;
@HandlesTypes ( {ServerEndpoint.class, ServerApplicationConfig.class,
                 Endpoint.class
                } )
public class WsSci implements ServletContainerInitializer {
    @Override
    public void onStartup ( Set<Class<?>> clazzes, ServletContext ctx )
    throws ServletException {
        WsServerContainer sc = init ( ctx, true );
        if ( clazzes == null || clazzes.size() == 0 ) {
            return;
        }
        Set<ServerApplicationConfig> serverApplicationConfigs = new HashSet<>();
        Set<Class<? extends Endpoint>> scannedEndpointClazzes = new HashSet<>();
        Set<Class<?>> scannedPojoEndpoints = new HashSet<>();
        try {
            String wsPackage = ContainerProvider.class.getName();
            wsPackage = wsPackage.substring ( 0, wsPackage.lastIndexOf ( '.' ) + 1 );
            for ( Class<?> clazz : clazzes ) {
                int modifiers = clazz.getModifiers();
                if ( !Modifier.isPublic ( modifiers ) ||
                        Modifier.isAbstract ( modifiers ) ) {
                    continue;
                }
                if ( clazz.getName().startsWith ( wsPackage ) ) {
                    continue;
                }
                if ( ServerApplicationConfig.class.isAssignableFrom ( clazz ) ) {
                    serverApplicationConfigs.add (
                        ( ServerApplicationConfig ) clazz.newInstance() );
                }
                if ( Endpoint.class.isAssignableFrom ( clazz ) ) {
                    @SuppressWarnings ( "unchecked" )
                    Class<? extends Endpoint> endpoint =
                        ( Class<? extends Endpoint> ) clazz;
                    scannedEndpointClazzes.add ( endpoint );
                }
                if ( clazz.isAnnotationPresent ( ServerEndpoint.class ) ) {
                    scannedPojoEndpoints.add ( clazz );
                }
            }
        } catch ( InstantiationException | IllegalAccessException e ) {
            throw new ServletException ( e );
        }
        Set<ServerEndpointConfig> filteredEndpointConfigs = new HashSet<>();
        Set<Class<?>> filteredPojoEndpoints = new HashSet<>();
        if ( serverApplicationConfigs.isEmpty() ) {
            filteredPojoEndpoints.addAll ( scannedPojoEndpoints );
        } else {
            for ( ServerApplicationConfig config : serverApplicationConfigs ) {
                Set<ServerEndpointConfig> configFilteredEndpoints =
                    config.getEndpointConfigs ( scannedEndpointClazzes );
                if ( configFilteredEndpoints != null ) {
                    filteredEndpointConfigs.addAll ( configFilteredEndpoints );
                }
                Set<Class<?>> configFilteredPojos =
                    config.getAnnotatedEndpointClasses (
                        scannedPojoEndpoints );
                if ( configFilteredPojos != null ) {
                    filteredPojoEndpoints.addAll ( configFilteredPojos );
                }
            }
        }
        try {
            for ( ServerEndpointConfig config : filteredEndpointConfigs ) {
                sc.addEndpoint ( config );
            }
            for ( Class<?> clazz : filteredPojoEndpoints ) {
                sc.addEndpoint ( clazz );
            }
        } catch ( DeploymentException e ) {
            throw new ServletException ( e );
        }
    }
    static WsServerContainer init ( ServletContext servletContext,
                                    boolean initBySciMechanism ) {
        WsServerContainer sc = new WsServerContainer ( servletContext );
        servletContext.setAttribute (
            Constants.SERVER_CONTAINER_SERVLET_CONTEXT_ATTRIBUTE, sc );
        servletContext.addListener ( new WsSessionListener ( sc ) );
        if ( initBySciMechanism ) {
            servletContext.addListener ( new WsContextListener() );
        }
        return sc;
    }
}
