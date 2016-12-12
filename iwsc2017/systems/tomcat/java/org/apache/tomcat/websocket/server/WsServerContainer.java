package org.apache.tomcat.websocket.server;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.websocket.CloseReason;
import javax.websocket.CloseReason.CloseCodes;
import javax.websocket.DeploymentException;
import javax.websocket.Encoder;
import javax.websocket.Endpoint;
import javax.websocket.server.ServerContainer;
import javax.websocket.server.ServerEndpoint;
import javax.websocket.server.ServerEndpointConfig;
import javax.websocket.server.ServerEndpointConfig.Configurator;
import org.apache.tomcat.InstanceManager;
import org.apache.tomcat.util.res.StringManager;
import org.apache.tomcat.websocket.WsSession;
import org.apache.tomcat.websocket.WsWebSocketContainer;
import org.apache.tomcat.websocket.pojo.PojoMethodMapping;
public class WsServerContainer extends WsWebSocketContainer
    implements ServerContainer {
    private static final StringManager sm = StringManager.getManager ( WsServerContainer.class );
    private static final CloseReason AUTHENTICATED_HTTP_SESSION_CLOSED =
        new CloseReason ( CloseCodes.VIOLATED_POLICY,
                          "This connection was established under an authenticated " +
                          "HTTP session that has ended." );
    private final WsWriteTimeout wsWriteTimeout = new WsWriteTimeout();
    private final ServletContext servletContext;
    private final Map<String, ServerEndpointConfig> configExactMatchMap =
        new ConcurrentHashMap<>();
    private final Map<Integer, SortedSet<TemplatePathMatch>> configTemplateMatchMap =
        new ConcurrentHashMap<>();
    private volatile boolean enforceNoAddAfterHandshake =
        org.apache.tomcat.websocket.Constants.STRICT_SPEC_COMPLIANCE;
    private volatile boolean addAllowed = true;
    private final Map<String, Set<WsSession>> authenticatedSessions = new ConcurrentHashMap<>();
    private volatile boolean endpointsRegistered = false;
    WsServerContainer ( ServletContext servletContext ) {
        this.servletContext = servletContext;
        setInstanceManager ( ( InstanceManager ) servletContext.getAttribute ( InstanceManager.class.getName() ) );
        String value = servletContext.getInitParameter (
                           Constants.BINARY_BUFFER_SIZE_SERVLET_CONTEXT_INIT_PARAM );
        if ( value != null ) {
            setDefaultMaxBinaryMessageBufferSize ( Integer.parseInt ( value ) );
        }
        value = servletContext.getInitParameter (
                    Constants.TEXT_BUFFER_SIZE_SERVLET_CONTEXT_INIT_PARAM );
        if ( value != null ) {
            setDefaultMaxTextMessageBufferSize ( Integer.parseInt ( value ) );
        }
        value = servletContext.getInitParameter (
                    Constants.ENFORCE_NO_ADD_AFTER_HANDSHAKE_CONTEXT_INIT_PARAM );
        if ( value != null ) {
            setEnforceNoAddAfterHandshake ( Boolean.parseBoolean ( value ) );
        }
        FilterRegistration.Dynamic fr = servletContext.addFilter (
                                            "Tomcat WebSocket (JSR356) Filter", new WsFilter() );
        fr.setAsyncSupported ( true );
        EnumSet<DispatcherType> types = EnumSet.of ( DispatcherType.REQUEST,
                                        DispatcherType.FORWARD );
        fr.addMappingForUrlPatterns ( types, true, "/*" );
    }
    @Override
    public void addEndpoint ( ServerEndpointConfig sec )
    throws DeploymentException {
        if ( enforceNoAddAfterHandshake && !addAllowed ) {
            throw new DeploymentException (
                sm.getString ( "serverContainer.addNotAllowed" ) );
        }
        if ( servletContext == null ) {
            throw new DeploymentException (
                sm.getString ( "serverContainer.servletContextMissing" ) );
        }
        String path = sec.getPath();
        PojoMethodMapping methodMapping = new PojoMethodMapping ( sec.getEndpointClass(),
                sec.getDecoders(), path );
        if ( methodMapping.getOnClose() != null || methodMapping.getOnOpen() != null
                || methodMapping.getOnError() != null || methodMapping.hasMessageHandlers() ) {
            sec.getUserProperties().put ( org.apache.tomcat.websocket.pojo.Constants.POJO_METHOD_MAPPING_KEY,
                                          methodMapping );
        }
        UriTemplate uriTemplate = new UriTemplate ( path );
        if ( uriTemplate.hasParameters() ) {
            Integer key = Integer.valueOf ( uriTemplate.getSegmentCount() );
            SortedSet<TemplatePathMatch> templateMatches =
                configTemplateMatchMap.get ( key );
            if ( templateMatches == null ) {
                templateMatches = new TreeSet<> (
                    TemplatePathMatchComparator.getInstance() );
                configTemplateMatchMap.putIfAbsent ( key, templateMatches );
                templateMatches = configTemplateMatchMap.get ( key );
            }
            if ( !templateMatches.add ( new TemplatePathMatch ( sec, uriTemplate ) ) ) {
                throw new DeploymentException (
                    sm.getString ( "serverContainer.duplicatePaths", path,
                                   sec.getEndpointClass(),
                                   sec.getEndpointClass() ) );
            }
        } else {
            ServerEndpointConfig old = configExactMatchMap.put ( path, sec );
            if ( old != null ) {
                throw new DeploymentException (
                    sm.getString ( "serverContainer.duplicatePaths", path,
                                   old.getEndpointClass(),
                                   sec.getEndpointClass() ) );
            }
        }
        endpointsRegistered = true;
    }
    @Override
    public void addEndpoint ( Class<?> pojo ) throws DeploymentException {
        ServerEndpoint annotation = pojo.getAnnotation ( ServerEndpoint.class );
        if ( annotation == null ) {
            throw new DeploymentException (
                sm.getString ( "serverContainer.missingAnnotation",
                               pojo.getName() ) );
        }
        String path = annotation.value();
        validateEncoders ( annotation.encoders() );
        ServerEndpointConfig sec;
        Class<? extends Configurator> configuratorClazz =
            annotation.configurator();
        Configurator configurator = null;
        if ( !configuratorClazz.equals ( Configurator.class ) ) {
            try {
                configurator = annotation.configurator().newInstance();
            } catch ( InstantiationException | IllegalAccessException e ) {
                throw new DeploymentException ( sm.getString (
                                                    "serverContainer.configuratorFail",
                                                    annotation.configurator().getName(),
                                                    pojo.getClass().getName() ), e );
            }
        }
        sec = ServerEndpointConfig.Builder.create ( pojo, path ).
              decoders ( Arrays.asList ( annotation.decoders() ) ).
              encoders ( Arrays.asList ( annotation.encoders() ) ).
              subprotocols ( Arrays.asList ( annotation.subprotocols() ) ).
              configurator ( configurator ).
              build();
        addEndpoint ( sec );
    }
    boolean areEndpointsRegistered() {
        return endpointsRegistered;
    }
    public void doUpgrade ( HttpServletRequest request,
                            HttpServletResponse response, ServerEndpointConfig sec,
                            Map<String, String> pathParams )
    throws ServletException, IOException {
        UpgradeUtil.doUpgrade ( this, request, response, sec, pathParams );
    }
    public WsMappingResult findMapping ( String path ) {
        if ( addAllowed ) {
            addAllowed = false;
        }
        ServerEndpointConfig sec = configExactMatchMap.get ( path );
        if ( sec != null ) {
            return new WsMappingResult ( sec, Collections.<String, String>emptyMap() );
        }
        UriTemplate pathUriTemplate = null;
        try {
            pathUriTemplate = new UriTemplate ( path );
        } catch ( DeploymentException e ) {
            return null;
        }
        Integer key = Integer.valueOf ( pathUriTemplate.getSegmentCount() );
        SortedSet<TemplatePathMatch> templateMatches =
            configTemplateMatchMap.get ( key );
        if ( templateMatches == null ) {
            return null;
        }
        Map<String, String> pathParams = null;
        for ( TemplatePathMatch templateMatch : templateMatches ) {
            pathParams = templateMatch.getUriTemplate().match ( pathUriTemplate );
            if ( pathParams != null ) {
                sec = templateMatch.getConfig();
                break;
            }
        }
        if ( sec == null ) {
            return null;
        }
        return new WsMappingResult ( sec, pathParams );
    }
    public boolean isEnforceNoAddAfterHandshake() {
        return enforceNoAddAfterHandshake;
    }
    public void setEnforceNoAddAfterHandshake (
        boolean enforceNoAddAfterHandshake ) {
        this.enforceNoAddAfterHandshake = enforceNoAddAfterHandshake;
    }
    protected WsWriteTimeout getTimeout() {
        return wsWriteTimeout;
    }
    @Override
    protected void registerSession ( Endpoint endpoint, WsSession wsSession ) {
        super.registerSession ( endpoint, wsSession );
        if ( wsSession.isOpen() &&
                wsSession.getUserPrincipal() != null &&
                wsSession.getHttpSessionId() != null ) {
            registerAuthenticatedSession ( wsSession,
                                           wsSession.getHttpSessionId() );
        }
    }
    @Override
    protected void unregisterSession ( Endpoint endpoint, WsSession wsSession ) {
        if ( wsSession.getUserPrincipal() != null &&
                wsSession.getHttpSessionId() != null ) {
            unregisterAuthenticatedSession ( wsSession,
                                             wsSession.getHttpSessionId() );
        }
        super.unregisterSession ( endpoint, wsSession );
    }
    private void registerAuthenticatedSession ( WsSession wsSession,
            String httpSessionId ) {
        Set<WsSession> wsSessions = authenticatedSessions.get ( httpSessionId );
        if ( wsSessions == null ) {
            wsSessions = Collections.newSetFromMap (
                             new ConcurrentHashMap<WsSession, Boolean>() );
            authenticatedSessions.putIfAbsent ( httpSessionId, wsSessions );
            wsSessions = authenticatedSessions.get ( httpSessionId );
        }
        wsSessions.add ( wsSession );
    }
    private void unregisterAuthenticatedSession ( WsSession wsSession,
            String httpSessionId ) {
        Set<WsSession> wsSessions = authenticatedSessions.get ( httpSessionId );
        if ( wsSessions != null ) {
            wsSessions.remove ( wsSession );
        }
    }
    public void closeAuthenticatedSession ( String httpSessionId ) {
        Set<WsSession> wsSessions = authenticatedSessions.remove ( httpSessionId );
        if ( wsSessions != null && !wsSessions.isEmpty() ) {
            for ( WsSession wsSession : wsSessions ) {
                try {
                    wsSession.close ( AUTHENTICATED_HTTP_SESSION_CLOSED );
                } catch ( IOException e ) {
                }
            }
        }
    }
    private static void validateEncoders ( Class<? extends Encoder>[] encoders )
    throws DeploymentException {
        for ( Class<? extends Encoder> encoder : encoders ) {
            @SuppressWarnings ( "unused" )
            Encoder instance;
            try {
                encoder.newInstance();
            } catch ( InstantiationException | IllegalAccessException e ) {
                throw new DeploymentException ( sm.getString (
                                                    "serverContainer.encoderFail", encoder.getName() ), e );
            }
        }
    }
    private static class TemplatePathMatch {
        private final ServerEndpointConfig config;
        private final UriTemplate uriTemplate;
        public TemplatePathMatch ( ServerEndpointConfig config,
                                   UriTemplate uriTemplate ) {
            this.config = config;
            this.uriTemplate = uriTemplate;
        }
        public ServerEndpointConfig getConfig() {
            return config;
        }
        public UriTemplate getUriTemplate() {
            return uriTemplate;
        }
    }
    private static class TemplatePathMatchComparator
        implements Comparator<TemplatePathMatch> {
        private static final TemplatePathMatchComparator INSTANCE =
            new TemplatePathMatchComparator();
        public static TemplatePathMatchComparator getInstance() {
            return INSTANCE;
        }
        private TemplatePathMatchComparator() {
        }
        @Override
        public int compare ( TemplatePathMatch tpm1, TemplatePathMatch tpm2 ) {
            return tpm1.getUriTemplate().getNormalizedPath().compareTo (
                       tpm2.getUriTemplate().getNormalizedPath() );
        }
    }
}
